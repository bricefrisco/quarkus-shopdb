package com.ecocitycraft.shopdb.exceptions;

public interface ExceptionMessage {
    String INVALID_PAGE = "Page cannot be less than 1.";
    String INVALID_PAGE_SIZE = "Page size must be between 1 and 100.";

    String INVALID_SERVER = "Invalid server. Must be one of: main, main-north, main-east.";
    String INVALID_SORT_BY = "Invalid sort by. Must be one of: best-price, quantity, material, quantity-available";
    String INVALID_TRADE_TYPE = "Invalid trade type. Must be on of: buy, sell";

    String EMPTY_PLAYER_NAME = "Player name cannot be null or blank.";
    String EMPTY_REGION_NAME = "Region name cannot be null or blank.";
    String EMPTY_SERVER_NAME = "Server name cannot be null or blank.";

    String USER_NOT_FOUND = "User with name '%s' was not found.";
    String PLAYER_NOT_FOUND = "Player with name '%s' was not found.";
    String REGION_NOT_FOUND = "Region '%s' on server '%s' was not found.";

    String UNAUTHORIZED = "Unauthorized.";
}
