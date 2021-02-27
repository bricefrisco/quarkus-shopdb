package com.ecocitycraft.shopdb.models.chestshops;

import com.ecocitycraft.shopdb.utils.ExceptionMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.BadRequestException;

public enum Server {
    @JsonProperty("main")
    MAIN,
    @JsonProperty("main-north")
    MAIN_NORTH,
    @JsonProperty("main-east")
    MAIN_EAST;

    public static Server fromString(String s) {
        switch(s) {
            case "main":
                return MAIN;
            case "main-north":
                return MAIN_NORTH;
            case "main-east":
                return MAIN_EAST;
            default:
                throw new BadRequestException(ExceptionMessage.INVALID_SERVER);
        }
    }

    public static String toString(Server server) {
        if (server == null) return "";
        switch(server) {
            case MAIN:
                return "MAIN";
            case MAIN_NORTH:
                return "MAIN_NORTH";
            case MAIN_EAST:
                return "MAIN_EAST";
            default:
                throw new BadRequestException(ExceptionMessage.INVALID_SERVER);
        }
    }
}
