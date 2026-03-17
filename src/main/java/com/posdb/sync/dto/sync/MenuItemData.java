package com.posdb.sync.dto.sync;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemData {
    private Integer menuItemId;

    private String menuItemText;

    private Integer menuCategoryId;

    private Integer menuGroupId;

    private Integer displayIndex;

    private BigDecimal defaultUnitPrice;

    private String menuItemDescription;

    private String menuItemNotification;

    private Boolean menuItemInActive;

    private Boolean menuItemInStock;

    private Boolean menuItemDiscountable;

    private String menuItemType;
}

