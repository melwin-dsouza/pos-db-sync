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
@Table(name = "order_transactions")
@Getter
@Setter
@NoArgsConstructor
public class OrderTransaction extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "order_transaction_id", nullable = false)
    private Integer orderTransactionId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "menu_item_id")
    private Integer menuItemId;

    @Column(name = "menu_item_unit_price")
    private BigDecimal menuItemUnitPrice;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "extended_price")
    private BigDecimal extendedPrice;

    @Column(name = "discount_id")
    private Integer discountId;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "discount_basis")
    private String discountBasis;

    @Column(name = "discount_amount_used")
    private BigDecimal discountAmountUsed;

    @Column(name = "row_guid")
    private String rowGuid;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

}

