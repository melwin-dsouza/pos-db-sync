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
    private OffsetDateTime paymentDateTime;

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

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;


}

