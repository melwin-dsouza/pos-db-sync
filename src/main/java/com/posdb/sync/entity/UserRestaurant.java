package com.posdb.sync.entity;

import com.posdb.sync.entity.enums.SubscriptionTypeEnum;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_restaurants")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "restaurant"})
public class UserRestaurant extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionTypeEnum subscriptionType;

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_expiry_date")
    private LocalDateTime subscriptionExpiryDate;

    @Column(name = "is_subscription_active")
    private Boolean isSubscriptionActive = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Helper method to check if subscription is currently valid
    public boolean isSubscriptionCurrentlyValid() {
        return isSubscriptionActive != null &&
               isSubscriptionActive &&
               subscriptionExpiryDate != null &&
               subscriptionExpiryDate.isAfter(LocalDateTime.now());
    }
}
