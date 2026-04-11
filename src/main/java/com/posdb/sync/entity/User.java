package com.posdb.sync.entity;

import com.posdb.sync.entity.enums.UserRole;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"passwordHash", "userRestaurants"})
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", referencedColumnName = "id")
    private Restaurant primaryRestaurant;

//    @ManyToMany
//    @JoinTable(
//            name = "user_restaurants",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
//    )
//    private List<Restaurant> restaurants = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserRestaurant> userRestaurants = new ArrayList<>();

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.mustChangePassword = true;
    }

    // Helper methods for backward compatibility and subscription management

    /**
     * Get all restaurants associated with this user (for backward compatibility)
     * @return List of restaurants from userRestaurants relationship
     */
    public List<Restaurant> getRestaurants() {
        if (userRestaurants == null) {
            return new ArrayList<>();
        }
        return userRestaurants.stream()
                .map(UserRestaurant::getRestaurant)
                .toList();
    }

    /**
     * Get restaurants with active subscriptions only
     * @return List of restaurants where user has valid subscription
     */
    public List<Restaurant> getRestaurantsWithActiveSubscription() {
        if (userRestaurants == null) {
            return new ArrayList<>();
        }
        return userRestaurants.stream()
                .filter(UserRestaurant::isSubscriptionCurrentlyValid)
                .map(UserRestaurant::getRestaurant)
                .toList();
    }

    public boolean hasValidSubscriptionForRestaurant(UUID restaurantId) {
        if (userRestaurants == null || restaurantId == null) {
            return false;
        }
        return userRestaurants.stream()
                .anyMatch(ur -> restaurantId.equals(ur.getRestaurant().getId()) &&
                               ur.isSubscriptionCurrentlyValid());
    }

    public UserRestaurant getUserRestaurantByRestaurantId(UUID restaurantId) {
        if (userRestaurants == null || restaurantId == null) {
            return null;
        }
        return userRestaurants.stream()
                .filter(ur -> restaurantId.equals(ur.getRestaurant().getId()))
                .findFirst()
                .orElse(null);
    }

}

