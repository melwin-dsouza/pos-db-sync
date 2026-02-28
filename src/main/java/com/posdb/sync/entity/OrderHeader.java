package com.posdb.sync.entity;

import com.posdb.sync.entity.enums.OrderTypeEnum;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "order_headers")
@Getter
@Setter
@NoArgsConstructor
public class OrderHeader extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "order_date_time", nullable = false)
    private Date orderDateTime;

    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "station_id")
    private Integer stationId;

    @Column(name = "order_type_id")
    private String orderTypeId;

    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    private OrderTypeEnum orderType;

    @Column(name = "dine_in_table_id")
    private Integer dineInTableId;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "delivery_charge")
    private BigDecimal deliveryCharge;

    @Column(name = "driver_employee_id")
    private Integer driverEmployeeId;

    @Column(name = "discount_id")
    private Integer discountId;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "amount_due")
    private BigDecimal amountDue;

    @Column(name = "cash_discount_amount")
    private BigDecimal cashDiscountAmount;

    @Column(name = "cash_discount_approval_emp_id")
    private Integer cashDiscountApprovalEmpId;

    @Column(name = "sub_total")
    private BigDecimal subTotal;

    @Column(name = "vat_rate")
    private Double vatRate;

    @Column(name = "vat_amount")
    private BigDecimal vatAmount;

    @Column(name = "guest_number")
    private Integer guestNumber;

    @Column(name = "edit_timestamp")
    private Date editTimestamp;

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

