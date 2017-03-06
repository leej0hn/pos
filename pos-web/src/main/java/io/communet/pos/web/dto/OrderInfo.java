package io.communet.pos.web.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderInfo implements Serializable {
    private String orderTime;
    private float orderAmount;
    private int orderCount;

    public OrderInfo() {

    }

    public OrderInfo(String orderTime) {
        this.orderTime = orderTime;
        this.orderAmount = 0;
    }

    public void accumulativeAmount(float amount) {
        this.orderAmount += amount;
        this.orderCount++;
    }
}