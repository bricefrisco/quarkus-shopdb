package com.ecocitycraft.shopdb.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnauthorizedExceptionHandler implements ExceptionMapper<SDBUnauthorizedException>  {
    @Override
    public Response toResponse(SDBUnauthorizedException e) {
        return Response.status(Response.Status.UNAUTHORIZED).entity(ErrorResponseGenerator.generate(e.getMessage(), Response.Status.UNAUTHORIZED)).build();
    }
}
