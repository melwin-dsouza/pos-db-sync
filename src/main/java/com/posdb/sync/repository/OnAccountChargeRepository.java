package com.posdb.sync.repository;

import com.posdb.sync.entity.OnAccountCharge;
import com.posdb.sync.entity.Restaurant;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class OnAccountChargeRepository implements PanacheRepositoryBase<OnAccountCharge, Long> {

    public Optional<OnAccountCharge> findByRestaurantAndChargeId(Restaurant restaurant, Integer chargeId) {
        return find("restaurant = ?1 and orderChargeId = ?2", restaurant, chargeId).firstResultOptional();
    }
}

