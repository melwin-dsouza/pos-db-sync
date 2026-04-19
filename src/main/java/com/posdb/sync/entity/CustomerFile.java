package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "customer_files")
@Getter
@Setter
@NoArgsConstructor
public class CustomerFile extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_notes")
    private String customerNotes;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "delivery_remarks")
    private String deliveryRemarks;

    @Column(name = "delivery_charge")
    private BigDecimal deliveryCharge;

    @Column(name = "delivery_comp")
    private BigDecimal deliveryComp;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "customer_since_date")
    private OffsetDateTime customerSinceDate;

    @Column(name = "total_spent")
    private BigDecimal totalSpent;

    @Column(name = "total_count")
    private Integer totalCount;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

