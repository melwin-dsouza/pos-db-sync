package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"restaurant_id", "email"})
})
public class User extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "UUID")
    public UUID id;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "UUID")
    public UUID restaurantId;

    @Column(nullable = false, length = 255)
    public String email;

    @Column(nullable = false, length = 255)
    public String passwordHash;

    @Column(nullable = false, length = 20)
    public String role;

    @Column(nullable = false)
    public Boolean mustChangePassword;

    @Column(nullable = false)
    public OffsetDateTime createdAt;

    @Column(nullable = false)
    public OffsetDateTime updatedAt;

    public User() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        this.mustChangePassword = true;
        this.role = "MANAGER";
    }
}

