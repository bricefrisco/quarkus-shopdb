package com.ecocitycraft.shopdb.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionHandler implements ExceptionMapper<Exception> {
    Logger LOGGER = LoggerFactory.getLogger(GenericExceptionHandler.class);

    @Override
    public Response toResponse(Exception e) {
        LOGGER.error("Unexpected error occurred.", e);
        return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseGenerator.generate(e.getMessage(), Response.Status.BAD_REQUEST)).build();
    }
}
