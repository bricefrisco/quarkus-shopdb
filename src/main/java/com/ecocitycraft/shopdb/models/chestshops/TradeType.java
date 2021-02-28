package com.ecocitycraft.shopdb.models.chestshops;

import com.ecocitycraft.shopdb.exceptions.SDBIllegalArgumentException;
import com.ecocitycraft.shopdb.exceptions.ExceptionMessage;

public enum TradeType {
    BUY,
    SELL;

    public static TradeType fromString(String s) {
        switch (s) {
            case "buy":
                return BUY;
            case "sell":
                return SELL;
            default:
                throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_TRADE_TYPE);
        }
    }
}
