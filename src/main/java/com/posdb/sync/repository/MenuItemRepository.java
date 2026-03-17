package com.posdb.sync.repository;

import com.posdb.sync.entity.MenuItem;
import com.posdb.sync.entity.Restaurant;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class MenuItemRepository implements PanacheRepositoryBase<MenuItem, Long> {

    public Optional<MenuItem> findByRestaurantAndMenuItemId(Restaurant restaurant, Integer menuItemId) {
        return find("restaurant = ?1 and menuItemId = ?2", restaurant, menuItemId).firstResultOptional();
    }
}

