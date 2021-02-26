package com.ecocitycraft.shopdb.models.chestshops;

import com.ecocitycraft.shopdb.utils.ExceptionMessage;

import javax.ws.rs.BadRequestException;

public enum SortBy {
    BEST_PRICE,
    QUANTITY,
    MATERIAL,
    QUANTITY_AVAILABLE;

    public static SortBy fromString(String s) {
        switch (s) {
            case "best-price":
                return BEST_PRICE;
            case "quantity":
                return QUANTITY;
            case "material":
                return MATERIAL;
            case "quantity-available":
                return QUANTITY_AVAILABLE;
            default:
                throw new BadRequestException(ExceptionMessage.INVALID_SORT_BY);
        }
    }
}
