package com.ecocitycraft.shopdb.models.bot;

public class BotQuantity {
    private Integer quantity;
    private Integer count;

    public BotQuantity() {
    }

    public BotQuantity(Integer quantity, Integer count) {
        this.quantity = quantity;
        this.count = count;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getCount() {
        return count;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
