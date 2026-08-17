package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderVoidLog;
import com.posdb.sync.entity.Restaurant;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class OrderVoidLogRepository implements PanacheRepositoryBase<OrderVoidLog, Long> {

    public Optional<OrderVoidLog> findByRestaurantAndAutoId(Restaurant restaurant, Integer autoId) {
        return find("restaurant = ?1 and autoId = ?2", restaurant, autoId).firstResultOptional();
    }
}

