package com.ecocitycraft.shopdb.utils;

import com.ecocitycraft.shopdb.models.chestshops.Server;

public interface ExceptionMessage {
    String INVALID_PAGE = "Page cannot be less than 1.";
    String INVALID_PAGE_SIZE = "Page size must be between 1 and 100.";
    String INVALID_SERVER = "Invalid server. Must be one of: main, main-north, main-east, any.";
    String INVALID_SORT_BY = "Invalid sort by. Must be one of: best-price, quantity, material, quantity-available";
    String INVALID_TRADE_TYPE = "Invalid trade type. Must be on of: buy, sell";
    String INVALID_REGION = "Region '%s' on server '%s' does not exist.";
}
