package com.posdb.sync.entity.enums;


public enum OrderStatusEnum {
    OPEN(1, "Open"),//Active order, not yet completed
    CLOSED(2, "Closed"),//Order completed, closed
    VOIDED(3, "Voided");//Order voided, not completed

    private final int intKey;
    private final String orderStatus;

    OrderStatusEnum(int intKey, String orderStatus) {
        this.intKey = intKey;
        this.orderStatus = orderStatus;
    }

    public int getIntKey() {
        return intKey;
    }


    public String getOrderStatus() {
        return orderStatus;
    }

    public static OrderStatusEnum getOrderStatusByValue(int value) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.intKey == value) {
                return status;
            }
        }
        return null;
    }

}


