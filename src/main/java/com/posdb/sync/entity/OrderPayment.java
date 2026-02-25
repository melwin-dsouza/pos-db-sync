package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_payments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"restaurant_id", "order_payment_id"})
})
public class OrderPayment extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "UUID")
    public UUID id;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "UUID")
    public UUID restaurantId;

    @Column(nullable = false)
    public Integer orderPaymentId;

    @Column(nullable = false)
    public OffsetDateTime paymentDateTime;

    @Column
    public Integer cashierId;

    @Column
    public Integer nonCashierEmployeeId;

    @Column(nullable = false)
    public Integer orderId;

    @Column(length = 50)
    public String paymentMethod;

    @Column(precision = 12, scale = 2)
    public BigDecimal amountTendered;

    @Column(precision = 12, scale = 2)
    public BigDecimal amountPaid;

    @Column(precision = 12, scale = 2)
    public BigDecimal employeeComp;

    @Column(length = 36)
    public String rowGuid;

    @Column(nullable = false)
    public OffsetDateTime createdAt;

    public OrderPayment() {
        this.createdAt = OffsetDateTime.now();
    }
}

