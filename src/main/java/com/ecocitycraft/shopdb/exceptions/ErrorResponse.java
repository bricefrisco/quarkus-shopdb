package com.ecocitycraft.shopdb.exceptions;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.sql.Timestamp;

@RegisterForReflection
public class ErrorResponse {
    public Timestamp timestamp;
    public int status;
    public String error;
    public String message;

    public ErrorResponse() {
    }

    public ErrorResponse(Timestamp timestamp, int status, String error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
