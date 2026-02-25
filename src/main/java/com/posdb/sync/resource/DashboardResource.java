package com.posdb.sync.resource;

import com.posdb.sync.dto.DailyOrderResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class DashboardResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardResource.class);

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Path("/daily")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getDailyOrders(
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) {
        try {
            String restaurantId = securityIdentity.getPrincipal().getName();
            LOGGER.info("Daily orders report requested for restaurantId: {}, from: {}, to: {}",
                    restaurantId, fromDate, toDate);

            if (fromDate == null || toDate == null) {
                LOGGER.warn("Missing date parameters for daily orders report");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"from and to parameters are required\"}")
                        .build();
            }

            OffsetDateTime startDate = OffsetDateTime.parse(fromDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime endDate = OffsetDateTime.parse(toDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            if (startDate.isAfter(endDate)) {
                LOGGER.warn("Invalid date range: from date is after to date");
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

            LOGGER.info("Daily orders report generated successfully for restaurantId: {} with {} days",
                    restaurantId, response.size());

            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid date format or restaurant ID: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Invalid date format or restaurant ID\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error generating daily orders report", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to generate report\"}")
                    .build();
        }
    }

    @GET
    @Path("/orders")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getOrders(
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("limit") @DefaultValue("100") Integer limit,
            @QueryParam("offset") @DefaultValue("0") Integer offset) {
        try {
            String restaurantId = securityIdentity.getPrincipal().getName();
            LOGGER.info("Orders list requested for restaurantId: {}, from: {}, to: {}, limit: {}, offset: {}",
                    restaurantId, fromDate, toDate, limit, offset);

            if (fromDate == null || toDate == null) {
                LOGGER.warn("Missing date parameters for orders list");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"from and to parameters are required\"}")
                        .build();
            }

            OffsetDateTime startDate = OffsetDateTime.parse(fromDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime endDate = OffsetDateTime.parse(toDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            UUID restaurantUuid = UUID.fromString(restaurantId);

            // Query to get order details
            String sql = "SELECT oh.id, oh.order_id, oh.order_date_time, oh.order_type, oh.amount_due, oh.sub_total " +
                    "FROM order_headers oh " +
                    "WHERE oh.restaurant_id = :restaurantId " +
                    "AND oh.order_date_time >= :startDate " +
                    "AND oh.order_date_time <= :endDate " +
                    "ORDER BY oh.order_date_time DESC " +
                    "LIMIT :limit OFFSET :offset";

            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter("restaurantId", restaurantUuid)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setParameter("limit", limit)
                    .setParameter("offset", offset)
                    .getResultList();

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

            LOGGER.info("Orders list retrieved successfully for restaurantId: {} with {} orders",
                    restaurantId, orders.size());

            return Response.ok(orders).build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid date format or restaurant ID: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Invalid date format or restaurant ID\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error retrieving orders list", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to retrieve orders\"}")
                    .build();
        }
    }
}

