package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderHeader;
import com.posdb.sync.repository.dto.*;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

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


    public List<DailyRevenueBreakdownDto> getDailyRevenueBreakdown(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        // Use the built-in EntityManager for custom queries
        return getEntityManager()
                .createQuery("SELECT NEW com.posdb.sync.repository.dto.DailyRevenueBreakdownDto(" +
                        "COUNT(DISTINCT oh.orderId), SUM(oh.guestNumber), SUM(op.amountPaid), " +
                        "SUM(oh.discountAmount), oh.orderType, COUNT(DISTINCT oh.orderId), SUM(op.amountPaid)) " +
                        "FROM OrderHeader oh " +
                        "INNER JOIN OrderPayment op ON oh.orderId = op.orderId " +
                        "WHERE oh.restaurant.id = :restaurantId " +
                        "AND op.amountPaid > 0 " +
                        "AND op.restaurant.id = :restaurantId " +
                        "AND oh.orderDateTime >= :startDate " +
                        "AND oh.orderDateTime <= :endDate " +
                        "GROUP BY ROLLUP(oh.orderType) " +
                        "ORDER BY oh.orderType", DailyRevenueBreakdownDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }


    public List<DetailedReportDataDto> getDailyDetailedReportData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        // Fetch order headers with their payments OR inhouseOrders and transactions
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.DetailedReportDataDto(" +
                " oh.orderId, oh.orderDateTime, oh.orderType, oh.guestNumber, " +
                        " COALESCE(op.orderPaymentId, oac.orderChargeId), " +
                        " COALESCE(op.paymentMethod, CAST('ONLINE_ORDER' AS String)), " +
                        " COALESCE(op.amountPaid, oac.amountCharged), " +
                " ot.orderTransactionId, ot.menuItemId, ot.quantity, ot.extendedPrice, ot.discountAmount, " +
                " mi.menuItemText) " +
                " FROM OrderHeader oh " +
                " LEFT JOIN OrderPayment op ON op.orderId = oh.orderId AND op.restaurant.id = :restaurantId " +
                        " LEFT JOIN OnAccountCharge oac ON oac.orderId = oh.orderId AND oac.restaurant.id = :restaurantId AND op.orderId IS NULL " +
                " LEFT JOIN OrderTransaction ot ON ot.orderId = oh.orderId AND ot.restaurant.id = :restaurantId " +
                " LEFT JOIN MenuItem mi ON mi.menuItemId = ot.menuItemId AND mi.restaurant.id = :restaurantId " +
                " WHERE oh.restaurant.id = :restaurantId  AND ot.restaurant.id = :restaurantId AND ( oac.restaurant.id = :restaurantId OR op.restaurant.id = :restaurantId )" +
                " AND oh.orderDateTime >= :startDate " +
                " AND oh.orderDateTime <= :endDate " +
                " ORDER BY oh.orderId, ot.orderTransactionId", DetailedReportDataDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<MonthlyReportDataDto> getMonthlyReportData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        String query = "SELECT new com.posdb.sync.repository.dto.MonthlyReportDataDto(" +
                " oh.orderType, COUNT(DISTINCT oh.orderId), COALESCE(SUM(op.amountPaid), 0))" +
                " FROM OrderHeader oh " +
                " LEFT JOIN OrderPayment op ON op.orderId = oh.orderId" +
                " WHERE oh.restaurant.id = :restaurantId AND op.restaurant.id = :restaurantId" +
                " AND op.amountPaid > 0 " +
                " AND oh.orderDateTime >= :startDate " +
                " AND oh.orderDateTime <= :endDate " +
                " GROUP BY oh.orderType " +
                " ORDER BY COALESCE(SUM(op.amountPaid), 0) DESC";

        return getEntityManager()
                .createQuery(query, MonthlyReportDataDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<DailyChartDataDto> getDailyChartData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        String query = "SELECT new com.posdb.sync.repository.dto.DailyChartDataDto(" +
                " CAST(oh.orderDateTime AS LocalDate), COUNT(DISTINCT oh.orderId), SUM(COALESCE(op.amountPaid, 0) + COALESCE(oac.amountCharged, 0)))" +
                " FROM OrderHeader oh " +
                " LEFT JOIN OrderPayment op ON op.orderId = oh.orderId" +
                    " LEFT JOIN OnAccountCharge oac ON oac.orderId = oh.orderId AND op.orderId IS NULL " +
                " WHERE oh.restaurant.id = :restaurantId AND  ( oac.restaurant.id = :restaurantId OR op.restaurant.id = :restaurantId )" +
                " AND (op.amountPaid > 0 OR oac.amountCharged >0 )" +
                " AND oh.orderDateTime >= :startDate " +
                " AND oh.orderDateTime <= :endDate " +
                " GROUP BY CAST(oh.orderDateTime AS LocalDate) " +
                " ORDER BY CAST(oh.orderDateTime AS LocalDate) ASC";

        return getEntityManager()
                .createQuery(query, DailyChartDataDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<VoidOrderMetricsDto> getVoidOrderMetrics(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.VoidOrderMetricsDto(" +
                        "COUNT(*), COALESCE(SUM(voidAmount), 0)) " +
                        "FROM OrderVoidLog ovl " +
                        "INNER JOIN OrderHeader oh ON ovl.orderId = oh.orderId " +
                        "WHERE ovl.restaurant.id = :restaurantId " +
                        "AND oh.restaurant.id = :restaurantId " +
                        "AND oh.orderDateTime >= :startDate " +
                        "AND oh.orderDateTime <= :endDate " +
                        "GROUP BY ovl.orderId " +
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
                        " ovl.autoId, ovl.voidReason, ovl.voidAmount, " +
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
                        "COUNT(DISTINCT oac.orderId), COALESCE(SUM(oac.amountCharged), 0)) " +
                        "FROM OnAccountCharge oac " +
                        "INNER JOIN OrderHeader oh ON oac.orderId = oh.orderId " +
                        "WHERE oac.restaurant.id = :restaurantId " +
                        "AND oh.restaurant.id = :restaurantId " +
                        "AND oh.orderDateTime >= :startDate " +
                        "AND oh.orderDateTime <= :endDate", InhouseOrderMetricsDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getSingleResultOrNull();
    }
}
