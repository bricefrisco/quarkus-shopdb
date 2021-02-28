package com.ecocitycraft.shopdb.exceptions;

public class SDBUnauthorizedException extends RuntimeException {
    public SDBUnauthorizedException(String message) {
        super(message);
    }
}