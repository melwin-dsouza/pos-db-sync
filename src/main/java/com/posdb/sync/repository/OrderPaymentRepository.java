package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderPayment;
import com.posdb.sync.entity.Restaurant;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class OrderPaymentRepository implements PanacheRepositoryBase<OrderPayment, Long> {

    public Optional<OrderPayment> findByRestaurantAndOrderPaymentId(Restaurant restaurant, Integer orderPaymentId) {
        return find("restaurant = ?1 and orderPaymentId = ?2", restaurant, orderPaymentId).firstResultOptional();
    }
}

