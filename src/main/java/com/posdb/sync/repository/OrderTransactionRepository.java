package com.posdb.sync.repository;

import com.posdb.sync.entity.OrderTransaction;
import com.posdb.sync.entity.Restaurant;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class OrderTransactionRepository implements PanacheRepositoryBase<OrderTransaction, Long> {

    public Optional<OrderTransaction> findByRestaurantAndOrderTransactionId(Restaurant restaurant, Integer orderTransactionId) {
        return find("restaurant = ?1 and orderTransactionId = ?2", restaurant, orderTransactionId).firstResultOptional();
    }
}

