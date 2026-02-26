package com.posdb.sync.entity.enums;


public enum OrderTypeEnum {
    DINING(1, "Dining"),
    TAKEAWAY(2, "Takeaway"),
    DRIVETHROUGH(3, "Drive Through"),
    DELIVERY(4, "Delivery");

    private final int intKey;
    private final String orderType;

    OrderTypeEnum(int intKey, String orderType) {
        this.intKey = intKey;
        this.orderType = orderType;
    }

    public int getIntKey() {
        return intKey;
    }


    public String getOrderType() {
        return orderType;
    }

    public static String getOrderTypeByValue(int value) {
        for (OrderTypeEnum type : OrderTypeEnum.values()) {
            if (type.intKey == value) {
                return type.orderType;
            }
        }
        return null;
    }

}


