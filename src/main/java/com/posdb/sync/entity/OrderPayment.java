package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "order_payments")
@Getter
@Setter
@NoArgsConstructor
public class OrderPayment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "order_payment_id", nullable = false)
    private Integer orderPaymentId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "payment_date_time")
    private Date paymentDateTime;

    @Column(name = "cashier_id")
    private Integer cashierId;

    @Column(name = "non_cashier_employee_id")
    private Integer nonCashierEmployeeId;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "amount_tendered")
    private BigDecimal amountTendered;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "employee_comp")
    private BigDecimal employeeComp;

    @Column(name = "row_guid")
    private String rowGuid;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
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

