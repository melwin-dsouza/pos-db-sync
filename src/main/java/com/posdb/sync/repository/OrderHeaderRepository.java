package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderHeader;
import com.posdb.sync.entity.Restaurant;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class OrderHeaderRepository implements PanacheRepositoryBase<OrderHeader, Long> {

    public Optional<OrderHeader> findByRestaurantAndOrderId(Restaurant restaurant, Integer orderId) {
        return find("restaurant = ?1 and orderId = ?2", restaurant, orderId).firstResultOptional();
    }
}

