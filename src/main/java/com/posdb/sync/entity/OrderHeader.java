package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_headers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"restaurant_id", "order_id"})
})
public class OrderHeader extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "UUID")
    public UUID id;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "UUID")
    public UUID restaurantId;

    @Column(nullable = false)
    public Integer orderId;

    @Column(nullable = false)
    public OffsetDateTime orderDateTime;

    @Column
    public Integer employeeId;

    @Column
    public Integer stationId;

    @Column(length = 50)
    public String orderType;

    @Column
    public Integer dineInTableId;

    @Column
    public Integer driverEmployeeId;

    @Column
    public Integer discountId;

    @Column(precision = 12, scale = 2)
    public BigDecimal discountAmount;

    @Column(precision = 12, scale = 2)
    public BigDecimal amountDue;

    @Column(precision = 12, scale = 2)
    public BigDecimal cashDiscountAmount;

    @Column
    public Integer cashDiscountApprovalEmpId;

    @Column(precision = 12, scale = 2)
    public BigDecimal subTotal;

    @Column
    public Integer guestNumber;

    @Column
    public OffsetDateTime editTimestamp;

    @Column(length = 36)
    public String rowGuid;

    @Column(nullable = false)
    public OffsetDateTime createdAt;

    public OrderHeader() {
        this.createdAt = OffsetDateTime.now();
    }
}

