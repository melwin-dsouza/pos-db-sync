package com.posdb.sync.service;

import com.posdb.sync.dto.response.*;
import com.posdb.sync.entity.Restaurant;
import com.posdb.sync.entity.User;
import com.posdb.sync.entity.enums.OrderTypeEnum;
import com.posdb.sync.exception.AppException;
import com.posdb.sync.repository.DashboardRepository;
import com.posdb.sync.repository.dto.*;
import com.posdb.sync.utils.BusinessWindowUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
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

    @Deprecated
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

            LocalDate yesterday = LocalDate.now().minusDays(1); // Get yesterday's date
            BusinessWindowUtil.BusinessWindow businessWindow = BusinessWindowUtil.getBusinessWindow(
                    selectedRestaurant.getOpeningTime(), selectedRestaurant.getClosingTime(), yesterday, selectedRestaurant.getTimeZone());


            List<DashboardDataDto> dashboardData = dashboardRepository.getDashboardData(restaurantUuid, businessWindow.start(), businessWindow.end());
            response.setDayTitle("Yesterday");
            response.setDayOfWeek(yesterday.getDayOfWeek().name());
            response.setStartDateTime(businessWindow.start().toString());
            response.setEndDateTime(businessWindow.end().toString());
            response.setTotalOrders(dashboardData.size());
            response.setTotalRevenue(dashboardData.stream().filter(d -> d.getAmountPaid() != null)
                    .mapToDouble(d -> d.getAmountPaid().doubleValue()).sum());
            response.setAverageOrderValue(dashboardData.isEmpty() ? 0 : dashboardData.stream().filter(d -> d.getAmountPaid() != null)
                    .mapToDouble(d -> d.getAmountPaid().doubleValue()).average().orElse(0));
            response.setNumberOfGuests(dashboardData.stream().filter(d -> d.getGuestNumber() != null).mapToInt(DashboardDataDto::getGuestNumber).sum());

            List<OrderTypeInfo> orderTypeInfoList = new ArrayList<>();
            Map<OrderTypeEnum, List<DashboardDataDto>> typeListMap = dashboardData.stream()
                    .filter(d -> d.getOrderType() != null)
                    .collect(Collectors.groupingBy(DashboardDataDto::getOrderType));
            for (Map.Entry<OrderTypeEnum, List<DashboardDataDto>> entry : typeListMap.entrySet()) {
                log.info("Order type: {}, count: {}", entry.getKey(), entry.getValue().size());
                orderTypeInfoList.add(new OrderTypeInfo(entry.getKey(), entry.getValue().size(),
                        entry.getValue().stream().filter(d -> d.getAmountPaid() != null).mapToDouble(d -> d.getAmountPaid().doubleValue()).sum()));
            }
            response.setOrderTypeInfoList(orderTypeInfoList);
            setRestaurantListInfo(user, response);
            log.info("Daily orders report generated successfully for restaurantId: {} for date: {} with {} orders",
                    restaurantId, response.getStartDateTime(), response.getTotalOrders());


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


    public DashboardResponse getDashboardDataByDate(String restaurantId, LocalDate selectedDate) {
        try {
            String userEmail = securityIdentity.getPrincipal().getName();
            log.info("Dashboard data report requested for user: {} for date {}", userEmail, selectedDate);
            User user = User.<User>find("email = ?1", userEmail)
                    .firstResultOptional().orElse(null);
            if (user == null) {
                log.warn("User not found for Get DashboardData request : {} .", userEmail);
                throw new AppException("User not found", Response.Status.BAD_REQUEST);
            }

            DashboardResponse response = new DashboardResponse();

            Restaurant selectedRestaurant = extractSelectedRestaurant(restaurantId, userEmail, user, response);
            if(selectedRestaurant == null) {
                log.warn("No restaurant selected for DashboardData request for user: {} .", userEmail);
                throw new AppException("No restaurant selected or associated with user", Response.Status.BAD_REQUEST);
            }
            UUID restaurantUuid = selectedRestaurant.getId();

            BusinessWindowUtil.BusinessWindow businessWindow = BusinessWindowUtil.getBusinessWindow(
                    selectedRestaurant.getOpeningTime(), selectedRestaurant.getClosingTime(), selectedDate, selectedRestaurant.getTimeZone());


            List<DailyRevenueBreakdownDto> dashboardData = dashboardRepository.getDailyRevenueBreakdown(restaurantUuid, businessWindow.start(), businessWindow.end());
            response.setDayTitle(selectedDate.toString());
            response.setDayOfWeek(selectedDate.getDayOfWeek().name());
            response.setStartDateTime(businessWindow.start().toString());
            response.setEndDateTime(businessWindow.end().toString());

            // Get totals from the last row (where order_type is null - ROLLUP total row)
            DailyRevenueBreakdownDto totalRow = dashboardData.stream()
                    .filter(d -> d.getOrderType() == null)
                    .findFirst()
                    .orElse(null);

            if (totalRow != null) {
                response.setTotalOrders(totalRow.getTotalOrders() != null ? totalRow.getTotalOrders().intValue() : 0);
                response.setNumberOfGuests(totalRow.getTotalGuests() != null ? totalRow.getTotalGuests().intValue() : 0);
                response.setTotalRevenue(totalRow.getTotalRevenue() != null ? totalRow.getTotalRevenue().doubleValue() : 0);
                response.setTotalDiscounts(totalRow.getTotalDiscounts() != null ? totalRow.getTotalDiscounts().doubleValue() : 0);
                response.setAverageOrderValue(response.getTotalOrders() == 0 ? 0 : response.getTotalRevenue() / response.getTotalOrders());
            }

            // Build order type breakdown (exclude the null order_type row which is the grand total)
            List<OrderTypeInfo> orderTypeInfoList = new ArrayList<>();
            List<DailyRevenueBreakdownDto> typeData = dashboardData.stream()
                    .filter(d -> d.getOrderType() != null)
                    .toList();
            for (DailyRevenueBreakdownDto dto : typeData) {
                orderTypeInfoList.add(new OrderTypeInfo(
                        dto.getOrderType(),
                        dto.getOrdertypeOrderCount() != null ? dto.getOrdertypeOrderCount().intValue() : 0,
                        dto.getOrdertypeRevenue() != null ? dto.getOrdertypeRevenue().doubleValue() : 0
                ));
            }
            response.setOrderTypeInfoList(orderTypeInfoList);
            setRestaurantListInfo(user, response);
            log.info("Daily orders report generated successfully for restaurantId: {} for date: {} with {} orders .",
                    restaurantId, response.getStartDateTime(), response.getTotalOrders());

            return response;
        } catch (Exception e) {
            log.error("Error generating daily orders report", e);
            throw new AppException("Failed to generate dashboard data", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public DailyDetailedReportResponse getDailyDetailedReport(String restaurantId, LocalDate selectedDate) {
        try {
            String userEmail = securityIdentity.getPrincipal().getName();
            log.info("Daily detailed report requested for user: {} for date: {}", userEmail, selectedDate);

            User user = User.<User>find("email = ?1", userEmail)
                    .firstResultOptional().orElse(null);
            if (user == null) {
                log.warn("User not found for daily detailed report request: {}", userEmail);
                throw new AppException("User not found", Response.Status.BAD_REQUEST);
            }

            DashboardResponse tempResponse = new DashboardResponse();
            Restaurant selectedRestaurant = extractSelectedRestaurant(restaurantId, userEmail, user, tempResponse);
            if (selectedRestaurant == null) {
                log.warn("No restaurant selected for daily detailed report request for user: {}", userEmail);
                throw new AppException("No restaurant selected or associated with user", Response.Status.BAD_REQUEST);
            }
            UUID restaurantUuid = selectedRestaurant.getId();

            BusinessWindowUtil.BusinessWindow businessWindow = BusinessWindowUtil.getBusinessWindow(
                    selectedRestaurant.getOpeningTime(), selectedRestaurant.getClosingTime(),
                    selectedDate, selectedRestaurant.getTimeZone());

            List<DetailedReportDataDto> queryData = dashboardRepository.getDailyDetailedReportData(
                    restaurantUuid, businessWindow.start(), businessWindow.end());
            DailyDetailedReportResponse response = new DailyDetailedReportResponse();

            // Group data by orderId to build order details
            Map<Integer, List<DetailedReportDataDto>> orderMap = queryData.stream()
                    .collect(Collectors.groupingBy(DetailedReportDataDto::getOrderId));

            List<DetailedReportDataDto> distinctEntries = queryData.stream().filter(d -> d.getOrderPaymentId() != null)
                    .collect(Collectors.toMap(DetailedReportDataDto::getOrderPaymentId, d -> d,
                            (existing, replacement) -> existing // If ID matches, keep the first one found
            )).values().stream().toList();

            response.setTotalRevenue(distinctEntries.stream()
                    .filter(d -> d.getAmountPaid() != null)
                    .mapToDouble(d -> d.getAmountPaid().doubleValue())
                    .sum());
            List<OrderDetailDto> orderDetails = new ArrayList<>();
            for(Map.Entry<Integer, List<DetailedReportDataDto>> entry : orderMap.entrySet()) {
                log.info("Order ID: {}, number of items: {}", entry.getKey(), entry.getValue().size());
                OrderDetailDto orderDetail = new OrderDetailDto();
                orderDetail.setOrderNumber(entry.getKey());
                if(entry.getValue().get(0) != null) {
                    Map<Integer, DetailedReportDataDto> distinctPayments = entry.getValue().stream().filter(d -> d.getOrderPaymentId() != null)
                            .collect(Collectors.toMap(DetailedReportDataDto::getOrderPaymentId, d -> d,
                                    (existing, replacement) -> existing));
                    log.info("distinctPayments for Order ID {}: {}", entry.getKey(), distinctPayments.size());
                    orderDetail.setOrderTime(entry.getValue().get(0).getOrderDateTime());
                    orderDetail.setTotalAmount(distinctPayments.values().stream()
                            .filter(d -> d.getAmountPaid() != null)
                            .mapToDouble(d -> d.getAmountPaid().doubleValue())
                            .sum());
                    orderDetail.setPaymentMode(distinctPayments.values().stream()
                            .filter(d -> d.getPaymentMethod() != null)
                            .map(DetailedReportDataDto::getPaymentMethod)
                            .distinct()
                            .collect(Collectors.joining(", ")));
                    orderDetail.setGuests(entry.getValue().get(0).getGuestNumber());
                    orderDetail.setOrderType(entry.getValue().get(0).getOrderType() != null ? entry.getValue().get(0).getOrderType().name() : "UNKNOWN");
                }
                Map<Integer, DetailedReportDataDto> distinctTransactions = entry.getValue().stream().filter(d -> d.getOrderTransactionId() != null)
                        .collect(Collectors.toMap(DetailedReportDataDto::getOrderTransactionId, d -> d,
                                (existing, replacement) -> existing));
                List<OrderItemDetailDto> orderItems = new ArrayList<>();
                for (DetailedReportDataDto itemRow : distinctTransactions.values()) {
                        OrderItemDetailDto item = new OrderItemDetailDto();
                        item.setOrderItemName(itemRow.getMenuItemText() != null ? itemRow.getMenuItemText() : " - ");
                        item.setQuantity(itemRow.getQuantity());
                        item.setPrice(itemRow.getExtendedPrice());
                        item.setDiscountGiven(itemRow.getDiscountAmount());
                        orderItems.add(item);
                }
                orderDetail.setOrderItems(orderItems);
                if(orderDetail.getTotalAmount() == null || orderDetail.getTotalAmount() == 0) {
                    log.warn("Order ID {} has no payment records, skipping order detail", entry.getKey());
                }else {
                    orderDetails.add(orderDetail);
                }
            }
            orderDetails.sort(Comparator.comparing(OrderDetailDto::getOrderTime));
            response.setOrderList(orderDetails);
            response.setTotalOrders(orderDetails.size());

            // Calculate hourly breakdown
            List<HourlyReportDataDto> hourlyBreakdown = calculateHourlyBreakdown(queryData);
            response.setHourlyBreakdown(hourlyBreakdown);

            log.info("Daily detailed report generated successfully for restaurantId: {} for startTime: {} endTime:{} with {} orders",
                    restaurantId, businessWindow.start(),businessWindow.end(), orderMap.size());
            return response;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating daily detailed report", e);
            throw new AppException("Failed to generate daily detailed report", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public MonthlyReportResponse getMonthlyReport(String restaurantId, String monthStr) {
        try {
            String userEmail = securityIdentity.getPrincipal().getName();
            log.info("Monthly report requested for user: {} for month: {}", userEmail, monthStr);

            User user = User.<User>find("email = ?1", userEmail)
                    .firstResultOptional().orElse(null);
            if (user == null) {
                log.warn("User not found for monthly report request: {}", userEmail);
                throw new AppException("User not found", Response.Status.BAD_REQUEST);
            }

            DashboardResponse tempResponse = new DashboardResponse();
            Restaurant selectedRestaurant = extractSelectedRestaurant(restaurantId, userEmail, user, tempResponse);
            if (selectedRestaurant == null) {
                log.warn("No restaurant selected for monthly report request for user: {}", userEmail);
                throw new AppException("No restaurant selected or associated with user", Response.Status.BAD_REQUEST);
            }
            UUID restaurantUuid = selectedRestaurant.getId();

            // Parse month string (expected format: YYYY-MM)
            YearMonth yearMonth = YearMonth.parse(monthStr);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();

            // Get business windows for month start and end
            BusinessWindowUtil.BusinessWindow startWindow = BusinessWindowUtil.getBusinessWindow(
                    selectedRestaurant.getOpeningTime(), selectedRestaurant.getClosingTime(),
                    monthStart, selectedRestaurant.getTimeZone());

            BusinessWindowUtil.BusinessWindow endWindow = BusinessWindowUtil.getBusinessWindow(
                    selectedRestaurant.getOpeningTime(), selectedRestaurant.getClosingTime(),
                    monthEnd, selectedRestaurant.getTimeZone());

            OffsetDateTime monthStartDateTime = startWindow.start();
            OffsetDateTime monthEndDateTime = endWindow.end();

            // Fetch monthly data grouped by order type
            List<MonthlyReportDataDto> monthlyData = dashboardRepository.getMonthlyReportData(
                    restaurantUuid, monthStartDateTime, monthEndDateTime);

            // Calculate total revenue
            BigDecimal totalRevenue = monthlyData.stream()
                    .map(MonthlyReportDataDto::getSumOfAmountPaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Build breakdown by order type
            List<OrderTypeDto> orderTypeList = new ArrayList<>();
            for (MonthlyReportDataDto item : monthlyData) {
                OrderTypeDto typeDto = new OrderTypeDto();
                typeDto.setOrderType(item.getOrderType() != null ? item.getOrderType().name() : "UNKNOWN");
                typeDto.setNumberOfOrders(item.getNumberOfOrders().intValue());
                typeDto.setSumOfAmountPaid(item.getSumOfAmountPaid());

                // Calculate percentage of total revenue
                double percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                        ? (item.getSumOfAmountPaid().doubleValue() / totalRevenue.doubleValue()) * 100
                        : 0.0;
                typeDto.setPercentageOfTotalRevenue(Math.round(percentage * 100.0) / 100.0);

                orderTypeList.add(typeDto);
            }

            // Build response
            MonthlyReportResponse response = new MonthlyReportResponse();
            response.setMonthName(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            response.setTotalMonthlyRevenue(totalRevenue);
            response.setMonthStartDate(monthStart);
            response.setMonthEndDate(monthEnd);
            response.setByOrderTypeList(orderTypeList);

            log.info("Monthly report generated successfully for restaurantId: {} for month: {} with total revenue: {}",
                    restaurantId, monthStr, totalRevenue);

            return response;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating monthly report", e);
            throw new AppException("Failed to generate monthly report", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public DailyChartDataResponse getDailyChartDataForMonth(String restaurantId, String monthStr) {
        try {
            String userEmail = securityIdentity.getPrincipal().getName();
            log.info("Daily chart data requested for user: {} for month: {}", userEmail, monthStr);

            User user = User.<User>find("email = ?1", userEmail)
                    .firstResultOptional().orElse(null);
            if (user == null) {
                log.warn("User not found for daily chart data request: {}", userEmail);
                throw new AppException("User not found", Response.Status.BAD_REQUEST);
            }

            DashboardResponse tempResponse = new DashboardResponse();
            Restaurant selectedRestaurant = extractSelectedRestaurant(restaurantId, userEmail, user, tempResponse);
            if (selectedRestaurant == null) {
                log.warn("No restaurant selected for daily chart data request for user: {}", userEmail);
                throw new AppException("No restaurant selected or associated with user", Response.Status.BAD_REQUEST);
            }
            UUID restaurantUuid = selectedRestaurant.getId();

            // Parse month string (expected format: YYYY-MM)
            YearMonth yearMonth = YearMonth.parse(monthStr);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();

            // Get business windows for month start and end
            BusinessWindowUtil.BusinessWindow startWindow = BusinessWindowUtil.getBusinessWindow(
                    selectedRestaurant.getOpeningTime(), selectedRestaurant.getClosingTime(),
                    monthStart, selectedRestaurant.getTimeZone());

            BusinessWindowUtil.BusinessWindow endWindow = BusinessWindowUtil.getBusinessWindow(
                    selectedRestaurant.getOpeningTime(), selectedRestaurant.getClosingTime(),
                    monthEnd, selectedRestaurant.getTimeZone());

            OffsetDateTime monthStartDateTime = startWindow.start();
            OffsetDateTime monthEndDateTime = endWindow.end();

            // Fetch daily chart data grouped by date
            List<DailyChartDataDto> dailyData = dashboardRepository.getDailyChartData(
                    restaurantUuid, monthStartDateTime, monthEndDateTime);

            // Build response with day-by-day data
            DailyChartDataResponse response = new DailyChartDataResponse();
            response.setMonthName(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            response.setMonthStartDate(monthStart);
            response.setMonthEndDate(monthEnd);

            List<DailyChartDataResponse.DailyChartData> chartDataList = new ArrayList<>();
            for (DailyChartDataDto item : dailyData) {
                DailyChartDataResponse.DailyChartData chartData = new DailyChartDataResponse.DailyChartData();
                chartData.setDate(item.getDate());
                chartData.setWeekday(item.getDate().getDayOfWeek().name());
                chartData.setTotalOrders(item.getNumberOfOrders() != null ? item.getNumberOfOrders().intValue() : 0);
                chartData.setTotalRevenue(item.getSumOfAmountPaid() != null ? item.getSumOfAmountPaid() : BigDecimal.ZERO);
                chartDataList.add(chartData);
            }

            response.setDailyData(chartDataList);

            log.info("Daily chart data generated successfully for restaurantId: {} for month: {} with {} days of data",
                    restaurantId, monthStr, chartDataList.size());

            return response;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating daily chart data", e);
            throw new AppException("Failed to generate daily chart data", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Calculate hourly breakdown from detailed report data
     * Groups orders by hour (0-23) and aggregates revenue, discounts, guests, and order count
     */
    private List<HourlyReportDataDto> calculateHourlyBreakdown(List<DetailedReportDataDto> queryData) {
        log.debug("Calculating hourly breakdown for {} data records", queryData.size());

        // Group by hour of day
        Map<Integer, List<DetailedReportDataDto>> hourlyGroups = new TreeMap<>();

        for (DetailedReportDataDto data : queryData) {
            if (data.getOrderDateTime() != null) {
                int hour = data.getOrderDateTime().getHour();
                hourlyGroups.computeIfAbsent(hour, k -> new ArrayList<>()).add(data);
            }
        }

        // Convert to HourlyReportDataDto
        List<HourlyReportDataDto> hourlyBreakdown = new ArrayList<>();

        for (Map.Entry<Integer, List<DetailedReportDataDto>> entry : hourlyGroups.entrySet()) {
            int hour = entry.getKey();
            List<DetailedReportDataDto> hourData = entry.getValue();

            // Get distinct payments for this hour to avoid double counting
            List<DetailedReportDataDto> distinctPayments = hourData.stream()
                    .filter(d -> d.getOrderPaymentId() != null)
                    .collect(Collectors.toMap(DetailedReportDataDto::getOrderPaymentId, d -> d,
                            (existing, replacement) -> existing))
                    .values().stream().toList();

            // Get distinct orders for this hour
            int orderCount = (int) hourData.stream()
                    .map(DetailedReportDataDto::getOrderId)
                    .distinct()
                    .count();

            // Calculate total revenue for this hour
            BigDecimal totalRevenue = distinctPayments.stream()
                    .filter(d -> d.getAmountPaid() != null)
                    .map(DetailedReportDataDto::getAmountPaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate total discounts for this hour
            BigDecimal totalDiscounts = hourData.stream()
                    .filter(d -> d.getDiscountAmount() != null)
                    .map(DetailedReportDataDto::getDiscountAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate total guests for this hour
            int totalGuests = hourData.stream()
                    .filter(d -> d.getGuestNumber() != null)
                    .mapToInt(DetailedReportDataDto::getGuestNumber)
                    .sum();

            HourlyReportDataDto hourlyDto = new HourlyReportDataDto();
            hourlyDto.setHour(hour);
            hourlyDto.setOrderCount(orderCount);
            hourlyDto.setTotalRevenue(totalRevenue);
            hourlyDto.setTotalDiscounts(totalDiscounts);
            hourlyDto.setTotalGuests(totalGuests);

            hourlyBreakdown.add(hourlyDto);

            log.debug("Hour {}: {} orders, Revenue: {}, Discounts: {}, Guests: {}",
                    hour, orderCount, totalRevenue, totalDiscounts, totalGuests);
        }

        return hourlyBreakdown;
    }

}


