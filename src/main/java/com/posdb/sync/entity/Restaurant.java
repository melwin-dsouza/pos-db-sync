package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurant")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"apiKey", "userList"})
public class Restaurant extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "keyword")
    private String keyword;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(name= "status")
    private String status;

    @ManyToMany(mappedBy = "restaurants")
    private List<User> userList = new ArrayList<>();

    @Column(name = "last_sync_time")
    private Date lastSyncTime;

    @Column(name = "opening_time")
    public LocalTime openingTime; // Maps to TIME in DB

    @Column(name = "closing_time")
    public LocalTime closingTime; // Maps to TIME in DB

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.status = "ACTIVE";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
    }
}

