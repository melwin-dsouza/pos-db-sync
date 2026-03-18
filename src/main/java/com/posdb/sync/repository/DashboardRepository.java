package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderHeader;
import com.posdb.sync.repository.dto.DashboardDataDto;
import com.posdb.sync.repository.dto.DetailedReportDataDto;
import com.posdb.sync.repository.dto.MonthlyReportDataDto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DashboardRepository implements PanacheRepository<OrderHeader> {

    public List<DashboardDataDto> getDashboardData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        // Use the built-in EntityManager for custom queries
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.DashboardDataDto(" +
                        " oh.orderId, oh.orderDateTime, oh.orderType, oh.discountAmount, oh.vatAmount, oh.guestNumber, " +
                        " op.orderPaymentId, op.paymentDateTime, op.paymentMethod, op.amountPaid) " +
                        " FROM OrderHeader oh " +
                        " JOIN OrderPayment op ON op.orderId = oh.orderId" +
                        " WHERE oh.restaurant.id = :restaurantId " +
                        " AND oh.orderDateTime >= :startDate " +
                        " AND oh.orderDateTime <= :endDate", DashboardDataDto.class)
                .setParameter("restaurantId", restaurantId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<DetailedReportDataDto> getDailyDetailedReportData(UUID restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        // Fetch order headers with their payments and transactions
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.DetailedReportDataDto(" +
                " oh.orderId, oh.orderDateTime, oh.orderType, oh.guestNumber, " +
                " op.orderPaymentId, op.paymentMethod, op.amountPaid, " +
                " ot.orderTransactionId, ot.menuItemId, ot.quantity, ot.extendedPrice, ot.discountAmount, " +
                " mi.menuItemText) " +
                " FROM OrderHeader oh " +
                " LEFT JOIN OrderPayment op ON op.orderId = oh.orderId AND op.restaurant.id = :restaurantId " +
                " LEFT JOIN OrderTransaction ot ON ot.orderId = oh.orderId AND ot.restaurant.id = :restaurantId " +
                " LEFT JOIN MenuItem mi ON mi.menuItemId = ot.menuItemId AND mi.restaurant.id = :restaurantId " +
                " WHERE oh.restaurant.id = :restaurantId " +
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
                " WHERE oh.restaurant.id = :restaurantId " +
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
}
