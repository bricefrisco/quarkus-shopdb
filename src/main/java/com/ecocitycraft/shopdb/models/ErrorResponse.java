package com.ecocitycraft.shopdb.models;

import java.sql.Timestamp;

public class ErrorResponse {
    private Timestamp timestamp;
    private int status;
    private String error;
    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(Timestamp timestamp, int status, String error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
