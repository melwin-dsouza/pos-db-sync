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
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
public class MenuItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "menu_item_id", nullable = false)
    private Integer menuItemId;

    @Column(name = "menu_item_text", nullable = false)
    private String menuItemText;

    @Column(name = "menu_category_id")
    private Integer menuCategoryId;

    @Column(name = "menu_group_id")
    private Integer menuGroupId;

    @Column(name = "display_index")
    private Integer displayIndex;

    @Column(name = "default_unit_price")
    private BigDecimal defaultUnitPrice;

    @Column(name = "menu_item_description")
    private String menuItemDescription;

    @Column(name = "menu_item_notification")
    private String menuItemNotification;

    @Column(name = "menu_item_in_active")
    private Boolean menuItemInActive;

    @Column(name = "menu_item_in_stock")
    private Boolean menuItemInStock;

    @Column(name = "menu_item_discountable")
    private Boolean menuItemDiscountable;

    @Column(name = "menu_item_type")
    private String menuItemType;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

