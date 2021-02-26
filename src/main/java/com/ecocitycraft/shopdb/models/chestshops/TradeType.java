package com.ecocitycraft.shopdb.models.chestshops;

import com.ecocitycraft.shopdb.utils.ExceptionMessage;

import javax.ws.rs.BadRequestException;

public enum TradeType {
    BUY,
    SELL;

    public static TradeType fromString(String s) {
        switch(s) {
            case "buy":
                return BUY;
            case "sell":
                return SELL;
            default:
                throw new BadRequestException(ExceptionMessage.INVALID_TRADE_TYPE);
        }
    }
}
