package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderHeader;
import com.posdb.sync.repository.dto.DashboardDataDto;
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
}
