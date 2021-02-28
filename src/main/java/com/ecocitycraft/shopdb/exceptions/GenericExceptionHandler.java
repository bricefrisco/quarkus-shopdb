package com.ecocitycraft.shopdb.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionHandler implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseGenerator.generate(e.getMessage(), Response.Status.BAD_REQUEST)).build();
    }
}
