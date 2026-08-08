package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderHeader;
import com.posdb.sync.entity.enums.OrderTypeEnum;
import com.posdb.sync.repository.dto.*;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DashboardRepository implements PanacheRepository<OrderHeader> {

    @Deprecated
    public List<DashboardDataDto> getDashboardData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        // Use the built-in EntityManager for custom queries
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.DashboardDataDto(" +
                        " oh.orderId, oh.orderDateTime, oh.orderType, oh.discountAmount, oh.vatAmount, oh.guestNumber, " +
                        " op.orderPaymentId, op.paymentDateTime, op.paymentMethod, op.amountPaid) " +
                        " FROM OrderHeader oh " +
                        " JOIN OrderPayment op ON op.orderId = oh.orderId" +
                        " WHERE oh.restaurant.id = :restaurantId " +
                        " AND op.restaurant.id = :restaurantId " +
                        " AND oh.orderDateTime >= :startDate " +
                        " AND oh.orderDateTime <= :endDate", DashboardDataDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }


//    public List<DailyRevenueBreakdownDto> getDailyRevenueBreakdown(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
//        // Use the built-in EntityManager for custom queries
//        return getEntityManager()
//                .createQuery("SELECT NEW com.posdb.sync.repository.dto.DailyRevenueBreakdownDto(" +
//                        "COUNT(DISTINCT oh.orderId), SUM(oh.guestNumber), SUM(oh.amountDue),  SUM(oh.subTotal)," +
//                        "SUM(oh.discountAmountUsed), oh.orderType) " +
//                        "FROM OrderHeader oh " +
//                        "WHERE oh.restaurant.id = :restaurantId " +
//                        "AND oh.orderDateTime >= :startDate " +
//                        "AND oh.orderDateTime <= :endDate " +
//                        "GROUP BY ROLLUP(oh.orderType) " +
//                        "ORDER BY oh.orderType", DailyRevenueBreakdownDto.class)
//                .setParameter("restaurantId", restaurantId)
//                .setParameter("startDate", startDate)
//                .setParameter("endDate", endDate)
//                .getResultList();
//    }

    public List<DailyRevenueBreakdownDto> getDailyRevenueBreakdown(
            UUID restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate) {

        String sql = """
        SELECT
            COUNT(DISTINCT oh.order_id) AS order_count,
            COALESCE(SUM(oh.guest_number), 0) AS guests,
            COALESCE(SUM(op.amount_paid), 0)
                + COALESCE(SUM(oac.amount_charged), 0)
                - COALESCE(SUM(op.employee_comp), 0)
                - COALESCE(SUM(oac.employee_comp), 0) AS amount_due,
            COALESCE(SUM(oh.sub_total), 0) AS sub_total,
            COALESCE(SUM(oh.discount_amount_used), 0) AS discount,
            oh.order_type
        FROM order_headers oh
        LEFT JOIN (
            SELECT order_id,
                SUM(amount_paid) AS amount_paid,
                SUM(employee_comp) AS employee_comp
            FROM order_payments
            WHERE restaurant_id = :restaurantId
            GROUP BY order_id
        ) op
            ON op.order_id = oh.order_id
        LEFT JOIN (
            SELECT
                order_id,
                SUM(amount_charged) AS amount_charged,
                SUM(employee_comp) AS employee_comp
            FROM on_account_charges
            WHERE restaurant_id = :restaurantId
            GROUP BY order_id
        ) oac
            ON oac.order_id = oh.order_id
        WHERE
            oh.restaurant_id = :restaurantId
            AND oh.order_date_time >= :startDate
            AND oh.order_date_time <= :endDate
        GROUP BY ROLLUP(oh.order_type)
        ORDER BY oh.order_type
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = getEntityManager()
                .createNativeQuery(sql)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();

        return rows.stream()
                .map(r -> new DailyRevenueBreakdownDto(
                        ((Number) r[0]).longValue(),          // order count
                        ((Number) r[1]).longValue(),          // guests
                        (BigDecimal) r[2],                    // amount due
                        (BigDecimal) r[3],                    // sub total
                        (BigDecimal) r[4],                    // discount
                        r[5] != null ? OrderTypeEnum.valueOf(r[5].toString()) : null
                ))
                .toList();
    }


    public List<DetailedReportDataDto> getDailyDetailedReportData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        // Fetch order headers with their payments OR inhouseOrders and transactions
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.DetailedReportDataDto(" +
                " oh.orderId, oh.orderDateTime, oh.orderType, oh.guestNumber, " +
                        " op.orderPaymentId, oac.orderChargeId, " +
                        " op.paymentMethod, " +
                        " COALESCE(op.amountPaid, 0), "+
                        " COALESCE(oac.amountCharged, 0), "+
                        " op.employeeComp, oac.employeeComp, "+
                " ot.orderTransactionId, ot.menuItemId, ot.quantity, ot.extendedPrice, ot.discountAmount, " +
                " mi.menuItemText) " +
                " FROM OrderHeader oh " +
                " LEFT JOIN OrderPayment op ON op.orderId = oh.orderId AND op.restaurant.id = :restaurantId " +
                        " LEFT JOIN OnAccountCharge oac ON oac.orderId = oh.orderId AND oac.restaurant.id = :restaurantId " +
                " LEFT JOIN OrderTransaction ot ON ot.orderId = oh.orderId AND ot.restaurant.id = :restaurantId " +
                " LEFT JOIN MenuItem mi ON mi.menuItemId = ot.menuItemId AND mi.restaurant.id = :restaurantId " +
                " WHERE oh.restaurant.id = :restaurantId  AND ot.restaurant.id = :restaurantId " +
                " AND oh.orderDateTime >= :startDate " +
                " AND oh.orderDateTime <= :endDate " +
                " ORDER BY oh.orderId, ot.orderTransactionId", DetailedReportDataDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

//    public List<MonthlyReportDataDto> getMonthlyReportData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
//        String query = "SELECT new com.posdb.sync.repository.dto.MonthlyReportDataDto(" +
//                " oh.orderType, COUNT(DISTINCT oh.orderId), COALESCE(SUM(oh.amountDue), 0))" +
//                " FROM OrderHeader oh " +
//                " WHERE oh.restaurant.id = :restaurantId " +
//                " AND oh.orderDateTime >= :startDate " +
//                " AND oh.orderDateTime <= :endDate " +
//                " GROUP BY oh.orderType " +
//                " ORDER BY COALESCE(SUM(oh.amountDue), 0) DESC";
//
//        return getEntityManager()
//                .createQuery(query, MonthlyReportDataDto.class)
//                .setParameter("restaurantId", restaurantId)
//                .setParameter("startDate", startDate)
//                .setParameter("endDate", endDate)
//                .getResultList();
//    }

    public List<MonthlyReportDataDto> getMonthlyReportData(
            UUID restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate) {

        String sql = """
    SELECT
        oh.order_type,
        COUNT(DISTINCT oh.order_id) AS order_count,
        COALESCE(SUM(op.amount_paid), 0)
            + COALESCE(SUM(oac.amount_charged), 0)
            - COALESCE(SUM(op.employee_comp), 0)
            - COALESCE(SUM(oac.employee_comp), 0) AS total_amount
    FROM order_headers oh
    LEFT JOIN (
        SELECT order_id,
            SUM(amount_paid) AS amount_paid,
            SUM(employee_comp) AS employee_comp
        FROM order_payments
        WHERE restaurant_id = :restaurantId
        GROUP BY order_id
    ) op
        ON op.order_id = oh.order_id
    LEFT JOIN (
        SELECT
            order_id,
            SUM(amount_charged) AS amount_charged,
            SUM(employee_comp) AS employee_comp
        FROM on_account_charges
        WHERE restaurant_id = :restaurantId
        GROUP BY order_id
    ) oac
        ON oac.order_id = oh.order_id
    WHERE
        oh.restaurant_id = :restaurantId
        AND oh.order_date_time >= :startDate
        AND oh.order_date_time <= :endDate
    GROUP BY oh.order_type
    -- Order by the alias sum directly for clean syntax
    ORDER BY total_amount DESC
    """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = getEntityManager()
                .createNativeQuery(sql)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();

        return rows.stream()
                .map(r -> new MonthlyReportDataDto(
                        r[0] != null ? OrderTypeEnum.valueOf(r[0].toString()) : null, // order type
                        ((Number) r[1]).longValue(),                                  // order count
                        (BigDecimal) r[2]                                             // total amount
                ))
                .toList();
    }


//    public List<DailyChartDataDto> getDailyChartData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
//        String query = "SELECT new com.posdb.sync.repository.dto.DailyChartDataDto(" +
//                " CAST(oh.orderDateTime AS LocalDate), COUNT(DISTINCT oh.orderId), " +
////                "SUM(COALESCE(op.amountPaid, 0) + COALESCE(oac.amountCharged, 0)))" +
//                "SUM(" +
//                "  (SELECT COALESCE(SUM(op.amountPaid - COALESCE(op.employeeComp, 0)), 0) " +
//                "   FROM OrderPayment op WHERE op.orderId = oh.orderId AND op.restaurant.id = :restaurantId AND op.amountPaid > 0) + " +
//                "  (SELECT COALESCE(SUM(oac.amountCharged - COALESCE(oac.employeeComp, 0)), 0) " +
//                "   FROM OnAccountCharge oac WHERE oac.orderId = oh.orderId AND oac.restaurant.id = :restaurantId AND oac.amountCharged > 0)" +
//                ") AS totalAmount ) " +
//                " FROM OrderHeader oh " +
//                " LEFT JOIN OrderPayment op ON op.orderId = oh.orderId" +
//                    " LEFT JOIN OnAccountCharge oac ON oac.orderId = oh.orderId AND op.orderId IS NULL " +
//                " WHERE oh.restaurant.id = :restaurantId AND  ( oac.restaurant.id = :restaurantId OR op.restaurant.id = :restaurantId )" +
//                " AND (op.amountPaid > 0 OR oac.amountCharged >0 )" +
//                " AND oh.orderDateTime >= :startDate " +
//                " AND oh.orderDateTime <= :endDate " +
//                " GROUP BY CAST(oh.orderDateTime AS LocalDate) " +
//                " ORDER BY CAST(oh.orderDateTime AS LocalDate) ASC";
//
//        return getEntityManager()
//                .createQuery(query, DailyChartDataDto.class)
//                .setParameter("restaurantId", restaurantId)
//                .setParameter("startDate", startDate)
//                .setParameter("endDate", endDate)
//                .getResultList();
//    }
        public List<DailyChartDataDto> getDailyChartData(
                UUID restaurantId,
                OffsetDateTime startDate,
                OffsetDateTime endDate) {

            String sql = """
            SELECT
                CAST(oh.order_date_time AS DATE) AS order_date,
                COUNT(DISTINCT oh.order_id) AS order_count,
                COALESCE(SUM(op.amount_paid), 0)
                    + COALESCE(SUM(oac.amount_charged), 0)
                    - COALESCE(SUM(op.employee_comp), 0)
                    - COALESCE(SUM(oac.employee_comp), 0) AS total_amount
            FROM order_headers oh
            LEFT JOIN (
                SELECT order_id,
                    SUM(amount_paid) AS amount_paid,
                    SUM(employee_comp) AS employee_comp
                FROM order_payments
                WHERE restaurant_id = :restaurantId
                GROUP BY order_id
            ) op
                ON op.order_id = oh.order_id
            LEFT JOIN (
                SELECT
                    order_id,
                    SUM(amount_charged) AS amount_charged,
                    SUM(employee_comp) AS employee_comp
                FROM on_account_charges
                WHERE restaurant_id = :restaurantId
                GROUP BY order_id
            ) oac
                ON oac.order_id = oh.order_id
            WHERE
                oh.restaurant_id = :restaurantId
                AND oh.order_date_time >= :startDate
                AND oh.order_date_time <= :endDate
            GROUP BY CAST(oh.order_date_time AS DATE)
            ORDER BY order_date ASC
            """;

            @SuppressWarnings("unchecked")
            List<Object[]> rows = getEntityManager()
                    .createNativeQuery(sql)
                    .setParameter("restaurantId", restaurantId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();

            return rows.stream()
                    .map(r -> new DailyChartDataDto(
                            r[0] != null ? java.sql.Date.valueOf(r[0].toString()).toLocalDate() : null, // order date
                            ((Number) r[1]).longValue(),                                                // order count
                            (BigDecimal) r[2]                                                           // total amount
                    ))
                    .toList();
        }

//    public List<VoidOrderMetricsDto> getVoidOrderMetrics(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
//        return getEntityManager()
//                .createQuery("SELECT new com.posdb.sync.repository.dto.VoidOrderMetricsDto(" +
//                        "COUNT(*), COALESCE(SUM(v.voidAmount), 0)) " +
//                        "FROM (SELECT DISTINCT ON (ovl.orderTransactionId) " +
//                        "ovl.orderId, ovl.voidAmount " +
//                        "FROM OrderVoidLog ovl " +
//                        "INNER JOIN OrderHeader oh ON ovl.orderId = oh.orderId " +
//                        "WHERE ovl.restaurant.id = :restaurantId " +
//                        "AND oh.restaurant.id = :restaurantId " +
//                        "AND oh.orderDateTime >= :startDate " +
//                        "AND oh.orderDateTime <= :endDate ) v " +
//                        "GROUP BY v.orderId " +
//                        "HAVING SUM(v.voidAmount) > 0", VoidOrderMetricsDto.class)
//                .setParameter("restaurantId", restaurantId)
//                .setParameter("startDate", startDate)
//                .setParameter("endDate", endDate)
//                .getResultList();
//    }
public List<VoidOrderMetricsDto> getVoidOrderMetrics(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
    return getEntityManager()
            .createQuery("SELECT new com.posdb.sync.repository.dto.VoidOrderMetricsDto(" +
                    "COUNT(DISTINCT ovl.orderTransactionId), " +
                    "COALESCE(SUM(ovl.voidAmount), 0)) " +
                    "FROM OrderHeader oh " +
                    "JOIN OrderVoidLog ovl ON oh.orderId = ovl.orderId " +
                    "WHERE oh.restaurant.id = :restaurantId " +
                    "AND ovl.restaurant.id = :restaurantId " +
                    "AND oh.orderDateTime >= :startDate " +
                    "AND oh.orderDateTime <= :endDate " +
                    "AND ovl.id = (SELECT MIN(subOvl.id) FROM OrderVoidLog subOvl WHERE subOvl.orderTransactionId = ovl.orderTransactionId) " +
                    "GROUP BY oh.orderId " +
                    "HAVING SUM(ovl.voidAmount) > 0", VoidOrderMetricsDto.class)
            .setParameter("restaurantId", restaurantId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .getResultList();
}


    public List<DetailedReportDataDto> getVoidOrderList(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.DetailedReportDataDto(" +
                        " oh.orderId, oh.orderDateTime, oh.orderType, oh.guestNumber, " +
                        " ovl.autoId, ovl.autoId, ovl.voidReason, ovl.voidAmount, COALESCE(ovl.voidAmount * 0, 0), COALESCE(ovl.voidAmount * 0, 0),COALESCE(ovl.voidAmount * 0, 0), " +
                        " ot.orderTransactionId, ot.menuItemId, ot.quantity, ot.extendedPrice, ot.discountAmount, " +
                        " mi.menuItemText) " +
                        " FROM OrderHeader oh " +
                        " LEFT JOIN OrderVoidLog ovl ON ovl.orderId = oh.orderId AND ovl.restaurant.id = :restaurantId " +
                        " LEFT JOIN OrderTransaction ot ON ot.orderId = ovl.orderId AND (ovl.orderTransactionId IS NULL OR ot.orderTransactionId = ovl.orderTransactionId) AND ot.restaurant.id = :restaurantId " +
                        " LEFT JOIN MenuItem mi ON mi.menuItemId = ot.menuItemId AND mi.restaurant.id = :restaurantId " +
                        " WHERE oh.restaurant.id = :restaurantId AND ovl.restaurant.id = :restaurantId AND ot.restaurant.id = :restaurantId " +
                        " AND oh.orderDateTime >= :startDate " +
                        " AND oh.orderDateTime <= :endDate " +
                        " ORDER BY oh.orderId, ot.orderTransactionId", DetailedReportDataDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public InhouseOrderMetricsDto getInhouseOrderMetrics(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.InhouseOrderMetricsDto(" +
                        "COUNT(DISTINCT oac.orderId), COALESCE(SUM(oh.amountDue), 0)) " +
                        "FROM OnAccountCharge oac " +
                        "INNER JOIN OrderHeader oh ON oac.orderId = oh.orderId " +
                        "INNER JOIN CustomerFile cf ON oac.customerId = cf.customerId " +
                        "WHERE oac.restaurant.id = :restaurantId " +
                        "AND cf.restaurant.id = :restaurantId " +
                        "AND oh.restaurant.id = :restaurantId " +
                        "AND oh.orderDateTime >= :startDate " +
                        "AND oh.orderDateTime <= :endDate " +
                        "AND LOWER(cf.customerName) IN ('careem', 'noon', 'talabat','deliveroo','smiles','keeta')", InhouseOrderMetricsDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getSingleResultOrNull();
    }
}
