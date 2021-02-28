package com.ecocitycraft.shopdb.models.bot;

public class BotPrices {
    private Double buyPrice;
    private Double sellPrice;
    private Double buyPriceEach;
    private Double sellPriceEach;

    public BotPrices() {
    }

    public BotPrices(Double buyPrice, Double sellPrice, Double buyPriceEach, Double sellPriceEach) {
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.buyPriceEach = buyPriceEach;
        this.sellPriceEach = sellPriceEach;
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

    @Override
    public String toString() {
        return "BotPrices{" +
                "buyPrice=" + buyPrice +
                ", sellPrice=" + sellPrice +
                ", buyPriceEach=" + buyPriceEach +
                ", sellPriceEach=" + sellPriceEach +
                '}';
    }
}
