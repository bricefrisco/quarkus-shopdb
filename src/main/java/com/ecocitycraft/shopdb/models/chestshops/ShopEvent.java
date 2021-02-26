package com.ecocitycraft.shopdb.models.chestshops;

import java.math.BigDecimal;

public class ShopEvent {
    private String id; // Serialized value of X,Y,Z coordinates to uniquely identify this chest shop
    private EventType eventType;
    private String world;
    private Integer x;
    private Integer y;
    private Integer z;
    private String owner;
    private Integer quantity;
    private Integer count;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private String item;
    private Boolean full;

    public String getId() {
        return id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getWorld() {
        return world;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer getZ() {
        return z;
    }

    public String getOwner() {
        return owner;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getCount() {
        return count;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public String getItem() {
        return item;
    }

    public Boolean getFull() {
        return full;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public void setZ(Integer z) {
        this.z = z;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setFull(Boolean full) {
        this.full = full;
    }
}
