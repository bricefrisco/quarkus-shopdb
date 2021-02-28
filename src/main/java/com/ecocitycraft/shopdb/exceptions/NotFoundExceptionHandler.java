package com.ecocitycraft.shopdb.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<SDBNotFoundException> {
    @Override
    public Response toResponse(SDBNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseGenerator.generate(e.getMessage(), Response.Status.NOT_FOUND)).build();
    }
}
