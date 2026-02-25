package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant")
public class Restaurant extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "UUID")
    public UUID id;

    @Column(nullable = false, length = 255)
    public String name;

    @Column(nullable = false, unique = true, length = 32)
    public String apiKey;

    @Column(nullable = false)
    public OffsetDateTime createdAt;

    @Column(nullable = false, length = 20)
    public String status;

    public Restaurant() {
        this.createdAt = OffsetDateTime.now();
        this.status = "ACTIVE";
    }
}

