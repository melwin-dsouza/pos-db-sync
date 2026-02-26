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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private Date orderDateTime;

    @Column
    private Integer employeeId;

    @Column
    private Integer stationId;

    @Column(length = 50)
    private String orderTypeId;
    
    @Column
    @Enumerated(EnumType.STRING)
    private OrderTypeEnum orderType;
    
    @Column
    private Integer dineInTableId;

    @Column
    private Integer driverEmployeeId;

    @Column
    private Integer discountId;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountDue;

    @Column(precision = 12, scale = 2)
    private BigDecimal cashDiscountAmount;

    @Column
    private Integer cashDiscountApprovalEmpId;

    @Column(precision = 12, scale = 2)
    private BigDecimal subTotal;

    @Column
    private Integer guestNumber;

    @Column
    private Date editTimestamp;

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

