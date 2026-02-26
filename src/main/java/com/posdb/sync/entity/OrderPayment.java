package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "order_payments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"restaurant_id", "order_payment_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class OrderPayment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private Integer orderPaymentId;

    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private Date paymentDateTime;

    @Column
    private Integer cashierId;

    @Column
    private Integer nonCashierEmployeeId;

    @Column(length = 50)
    private String paymentMethod;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountTendered;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(precision = 12, scale = 2)
    private BigDecimal employeeComp;

    @Column(length = 36)
    private String rowGuid;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date();
    }

}

