package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurant")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"apiKey", "userRestaurants"})
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

//    @ManyToMany(mappedBy = "restaurants")
//    private List<User> userList = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    private List<UserRestaurant> userRestaurants = new ArrayList<>();

    @Column(name = "last_sync_time")
    private OffsetDateTime lastSyncTime;

    @Column(name = "opening_time")
    public LocalTime openingTime; // Maps to TIME in DB

    @Column(name = "closing_time")
    public LocalTime closingTime; // Maps to TIME in DB

    @Column(name = "time_zone")
    private String timeZone;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.status = "ACTIVE";
    }

    // Helper method for backward compatibility
    public List<User> getUsers() {
        if (userRestaurants == null) {
            return new ArrayList<>();
        }
        return userRestaurants.stream()
                .map(UserRestaurant::getUser)
                .collect(java.util.stream.Collectors.toList());
    }

}

