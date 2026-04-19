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
@Table(name = "on_account_charges")
@Getter
@Setter
@NoArgsConstructor
public class OnAccountCharge extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "order_charge_id", nullable = false)
    private Integer orderChargeId;

    @Column(name = "charge_date_time")
    private OffsetDateTime chargeDateTime;

    @Column(name = "cashier_id")
    private Integer cashierId;

    @Column(name = "non_cashier_employee_id")
    private Integer nonCashierEmployeeId;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "amount_charged")
    private BigDecimal amountCharged;

    @Column(name = "order_charge_payment_id")
    private Integer orderChargePaymentId;

    @Column(name = "employee_comp")
    private BigDecimal employeeComp;

    @Column(name = "charge_due_date")
    private OffsetDateTime chargeDueDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

