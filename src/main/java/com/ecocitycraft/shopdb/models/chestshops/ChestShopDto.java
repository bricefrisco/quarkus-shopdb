package com.ecocitycraft.shopdb.models.chestshops;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChestShopDto {
    private Server server;
    private Location location;
    private String material;
    private ChestShopPlayerDto owner;
    private ChestShopRegionDto town;
    private Integer quantity;
    private Integer quantityAvailable;
    private Double buyPrice;
    private Double sellPrice;
    private Double buyPriceEach;
    private Double sellPriceEach;
    private Boolean isFull;
    private Boolean isBuySign;
    private Boolean isSellSign;

    public Server getServer() {
        return server;
    }

    public Location getLocation() {
        return location;
    }

    public String getMaterial() {
        return material;
    }

    public ChestShopPlayerDto getOwner() {
        if (owner == null) owner = new ChestShopPlayerDto();
        return owner;
    }

    public ChestShopRegionDto getTown() {
        return town;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public Double getSellPrice() {
        return sellPrice;
    }

    public Double getBuyPriceEach() {
        return buyPriceEach;
    }

    public Double getSellPriceEach() {
        return sellPriceEach;
    }

    public Boolean getFull() {
        return isFull;
    }

    public Boolean getBuySign() {
        return isBuySign;
    }

    public Boolean getSellSign() {
        return isSellSign;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setOwner(ChestShopPlayerDto owner) {
        this.owner = owner;
    }

    public void setTown(ChestShopRegionDto town) {
        this.town = town;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setBuyPriceEach(Double buyPriceEach) {
        this.buyPriceEach = buyPriceEach;
    }

    public void setSellPriceEach(Double sellPriceEach) {
        this.sellPriceEach = sellPriceEach;
    }

    public void setFull(Boolean full) {
        isFull = full;
    }

    public void setBuySign(Boolean buySign) {
        isBuySign = buySign;
    }

    public void setSellSign(Boolean sellSign) {
        isSellSign = sellSign;
    }
}
