package com.posdb.sync.service;

import com.posdb.sync.dto.response.DailyOrderResponse;
import com.posdb.sync.dto.response.DashboardResponse;
import com.posdb.sync.dto.response.OrderTypeInfo;
import com.posdb.sync.dto.response.RestaurantInfo;
import com.posdb.sync.entity.Restaurant;
import com.posdb.sync.entity.User;
import com.posdb.sync.entity.enums.OrderTypeEnum;
import com.posdb.sync.exception.AppException;
import com.posdb.sync.repository.DashboardRepository;
import com.posdb.sync.repository.dto.DashboardDataDto;
import com.posdb.sync.utils.BusinessWindowUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class DashboardService {

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    DashboardRepository dashboardRepository;

    @Transactional
    public DashboardResponse getDashboardData(String restaurantId) {
        try {
            String userEmail = securityIdentity.getPrincipal().getName();
            log.info("Dashboard data report requested for user: {}", userEmail);
            User user = User.<User>find("email = ?1", userEmail)
                    .firstResultOptional().orElse(null);
            if (user == null) {
                log.warn("User not found for Get DashboardData request : {}", userEmail);
                throw new AppException("User not found", Response.Status.BAD_REQUEST);
            }

            DashboardResponse response = new DashboardResponse();

            Restaurant selectedRestaurant = extractSelectedRestaurant(restaurantId, userEmail, user, response);
            if(selectedRestaurant == null) {
                log.warn("No restaurant selected for DashboardData request for user: {}", userEmail);
                throw new AppException("No restaurant selected or associated with user", Response.Status.BAD_REQUEST);
            }
            UUID restaurantUuid = selectedRestaurant.getId();

            BusinessWindowUtil.BusinessWindow businessWindow = BusinessWindowUtil.getYesterdayWindow(
                    selectedRestaurant.getOpeningTime(), selectedRestaurant.getClosingTime());


            List<DashboardDataDto> dashboardData = dashboardRepository.getDashboardData(restaurantUuid, businessWindow.getStart(), businessWindow.getEnd());
            response.setDayTitle("Yesterday");
            response.setDate(businessWindow.getStart().toString());
            response.setTotalOrders(dashboardData.size());
            response.setTotalRevenue(dashboardData.stream().mapToDouble(d -> d.getAmountPaid().doubleValue()).sum());
            response.setAverageOrderValue(dashboardData.isEmpty() ? 0 : dashboardData.stream()
                    .mapToDouble(d -> d.getAmountPaid().doubleValue()).average().orElse(0));
            response.setNumberOfGuests(dashboardData.stream().mapToInt(DashboardDataDto::getGuestNumber).sum());

            List<OrderTypeInfo> orderTypeInfoList = new ArrayList<>();
            Map<OrderTypeEnum, List<DashboardDataDto>> typeListMap = dashboardData.stream()
                    .filter(d -> d.getOrderType() != null)
                    .collect(Collectors.groupingBy(DashboardDataDto::getOrderType));
            for (Map.Entry<OrderTypeEnum, List<DashboardDataDto>> entry : typeListMap.entrySet()) {
                log.info("Order type: {}, count: {}", entry.getKey(), entry.getValue().size());
                orderTypeInfoList.add(new OrderTypeInfo(entry.getKey(), entry.getValue().size(),
                        entry.getValue().stream().mapToDouble(d -> d.getAmountPaid().doubleValue()).sum()));
            }
            response.setOrderTypeInfoList(orderTypeInfoList);
            setRestaurantListInfo(user, response);
            log.info("Daily orders report generated successfully for restaurantId: {} for date: {} with {} orders",
                    restaurantId, response.getDate(), response.getTotalOrders());


            return response;
        } catch (Exception e) {
            log.error("Error generating daily orders report", e);
            throw new AppException("Failed to generate dashboard data", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Restaurant extractSelectedRestaurant(String restaurantId, String userEmail, User user, DashboardResponse response) {
        if ("ALL".equalsIgnoreCase(restaurantId)) {
            log.info("ALL restaurantId provided for user: {}", userEmail);
            throw new AppException("ALL restaurantId is not supported for DashboardData request", Response.Status.BAD_REQUEST);
        } else if (restaurantId == null || restaurantId.isEmpty()) {
            log.warn("Missing restaurantId parameter for DashboardData request");
            log.info("Attempting to set restaurant info based on user's primary restaurant for user: {}", userEmail);
            if (user.getPrimaryRestaurant() != null) {
                Restaurant restaurant = user.getPrimaryRestaurant();
                response.setRestaurantInfo(new RestaurantInfo(restaurant.getId().toString(), restaurant.getName(), restaurant.getAddress()));
                return user.getPrimaryRestaurant();
            }
        } else {
            log.info("RestaurantId parameter provided: {} for user: {}", restaurantId, userEmail);
            Restaurant restaurant = Restaurant.<Restaurant>find("id = ?1", UUID.fromString(restaurantId))
                    .firstResultOptional().orElse(null);
            if (restaurant == null) {
                log.warn("Restaurant not found for Get DashboardData request : {}", restaurantId);
                throw new AppException("Restaurant not found", Response.Status.BAD_REQUEST);
            }
            response.setRestaurantInfo(new RestaurantInfo(restaurant.getId().toString(), restaurant.getName(), restaurant.getAddress()));
            return restaurant;
        }
        return null;
    }

    private static void setRestaurantListInfo(User user, DashboardResponse response) {
        response.setAssociatedRestaurants(user.getRestaurants().stream()
                .map(r -> new RestaurantInfo(r.getId().toString(), r.getName(), r.getAddress()))
                .toList());
    }


    @Transactional
    public Response getDailyOrders(String restaurantId, String fromDate, String toDate) {
        try {
            log.info("Daily orders report requested for restaurantId: {}, from: {}, to: {}",
                    restaurantId, fromDate, toDate);
            if (fromDate == null || toDate == null) {
                log.warn("Missing date parameters for daily orders report");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"from and to parameters are required\"}")
                        .build();
            }

            OffsetDateTime startDate = OffsetDateTime.parse(fromDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime endDate = OffsetDateTime.parse(toDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            if (startDate.isAfter(endDate)) {
                log.warn("Invalid date range: from date is after to date");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"from date must be before to date\"}")
                        .build();
            }

            UUID restaurantUuid = UUID.fromString(restaurantId);

            // Query to get daily order counts grouped by order type
            String sql = "SELECT " +
                    "DATE(oh.order_date_time AT TIME ZONE 'UTC') as order_date, " +
                    "oh.order_type, " +
                    "COUNT(*) as order_count " +
                    "FROM order_headers oh " +
                    "WHERE oh.restaurant_id = :restaurantId " +
                    "AND oh.order_date_time >= :startDate " +
                    "AND oh.order_date_time <= :endDate " +
                    "GROUP BY DATE(oh.order_date_time AT TIME ZONE 'UTC'), oh.order_type " +
                    "ORDER BY DATE(oh.order_date_time AT TIME ZONE 'UTC') DESC";

            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter("restaurantId", restaurantUuid)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();

            // Group results by date
            Map<String, Map<String, Integer>> groupedByDate = new LinkedHashMap<>();

            for (Object[] row : results) {
                String date = row[0].toString();
                String orderType = (String) row[1];
                Integer count = ((Number) row[2]).intValue();

                groupedByDate.computeIfAbsent(date, k -> new HashMap<>())
                        .put(orderType != null ? orderType : "UNKNOWN", count);
            }

            // Build response
            List<DailyOrderResponse> response = new ArrayList<>();
            for (Map.Entry<String, Map<String, Integer>> entry : groupedByDate.entrySet()) {
                int totalOrders = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
                response.add(new DailyOrderResponse(entry.getKey(), totalOrders, entry.getValue()));
            }

            log.info("Daily orders report generated successfully for restaurantId: {} with {} days",
                    restaurantId, response.size());

            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid date format or restaurant ID: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Invalid date format or restaurant ID\"}")
                    .build();
        } catch (Exception e) {
            log.error("Error generating daily orders report", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to generate report\"}")
                    .build();
        }
    }

    @Transactional
    public Response getOrders(String fromDate, String toDate, Integer limit, Integer offset) {
        try {
            String restaurantId = securityIdentity.getPrincipal().getName();
            log.info("Orders list requested for restaurantId: {}, from: {}, to: {}, limit: {}, offset: {}", restaurantId, fromDate, toDate, limit, offset);

            if (fromDate == null || toDate == null) {
                log.warn("Missing date parameters for orders list");
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"code\": \"INVALID_INPUT\", \"message\": \"from and to parameters are required\"}").build();
            }

            OffsetDateTime startDate = OffsetDateTime.parse(fromDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime endDate = OffsetDateTime.parse(toDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            UUID restaurantUuid = UUID.fromString(restaurantId);

            // Query to get order details
            String sql = "SELECT oh.id, oh.order_id, oh.order_date_time, oh.order_type, oh.amount_due, oh.sub_total " + "FROM order_headers oh " + "WHERE oh.restaurant_id = :restaurantId " + "AND oh.order_date_time >= :startDate " + "AND oh.order_date_time <= :endDate " + "ORDER BY oh.order_date_time DESC " + "LIMIT :limit OFFSET :offset";

            @SuppressWarnings("unchecked") List<Object[]> results = entityManager.createNativeQuery(sql).setParameter("restaurantId", restaurantUuid).setParameter("startDate", startDate).setParameter("endDate", endDate).setParameter("limit", limit).setParameter("offset", offset).getResultList();

            List<Map<String, Object>> orders = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", row[0].toString());
                order.put("orderId", row[1]);
                order.put("orderDateTime", row[2].toString());
                order.put("orderType", row[3]);
                order.put("amountDue", row[4]);
                order.put("subTotal", row[5]);
                orders.add(order);
            }

            log.info("Orders list retrieved successfully for restaurantId: {} with {} orders", restaurantId, orders.size());

            return Response.ok(orders).build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid date format or restaurant ID: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Invalid date format or restaurant ID\"}").build();
        } catch (Exception e) {
            log.error("Error retrieving orders list", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to retrieve orders\"}").build();
        }
    }


}
