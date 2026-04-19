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
@Table(name = "order_void_logs")
@Getter
@Setter
@NoArgsConstructor
public class OrderVoidLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "order_transaction_id")
    private Integer orderTransactionId;

    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "void_reason")
    private String voidReason;

    @Column(name = "void_date_time")
    private OffsetDateTime voidDateTime;

    @Column(name = "void_for_item_reduction")
    private Boolean voidForItemReduction;

    @Column(name = "void_amount")
    private BigDecimal voidAmount;

    @Column(name = "auto_id")
    private Integer autoId;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

