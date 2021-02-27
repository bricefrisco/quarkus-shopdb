package com.ecocitycraft.shopdb.services;

import com.ecocitycraft.shopdb.models.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.sql.Timestamp;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {
    Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public Response toResponse(Exception e) {
        LOGGER.warn("", e);

        ErrorResponse errorResponse = new ErrorResponse(
                new Timestamp(System.currentTimeMillis()),
                Response.Status.BAD_REQUEST.getStatusCode(),
                Response.Status.BAD_REQUEST.getReasonPhrase(),
                e.getMessage());

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
}
