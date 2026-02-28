package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderHeader;
import com.posdb.sync.repository.dto.DashboardDataDto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DashboardRepository implements PanacheRepository<OrderHeader> {

    public List<DashboardDataDto> getDashboardData(UUID restaurantId, Date startDate, Date endDate) {
        // Use the built-in EntityManager for custom queries
        return getEntityManager()
                .createQuery("SELECT new com.posdb.sync.repository.dto.DashboardDataDto(" +
                        " COUNT(*) as order_count , oh.orderId, oh.orderDateTime, oh.orderType, oh.discountAmount, oh.vatAmount, oh.guestNumber,\" +\n" +
                        " op.orderPaymentId, op.paymentDateTime, op.paymentMethod, op.amountPaid) " +
                        " FROM order_headers oh " +
                        " JOIN order_payments op ON op.order_id = oh.id" +
                        " WHERE oh.restaurant_id = :restaurantId " +
                        " AND oh.order_date_time >= :startDate " +
                        " AND oh.order_date_time <= :endDate", DashboardDataDto.class)
                .getResultList();
    }
}
