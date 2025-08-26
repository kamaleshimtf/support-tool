package org.imtf.siron.supporttool.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.imtf.siron.supporttool.exception.InvalidPathException;
import org.imtf.siron.supporttool.exception.NotFoundException;
import org.imtf.siron.supporttool.exception.SecurityException;
import org.imtf.siron.supporttool.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof SecurityException) {
            logger.error("SecurityException: {}", exception.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Access denied: " + exception.getMessage()))
                    .build();
        } else if (exception instanceof InvalidPathException) {
            logger.error("InvalidPathException: {}", exception.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid path: " + exception.getMessage()))
                    .build();
        }
        else if (exception instanceof NotFoundException) {
            logger.error("NotFoundException: {}", exception.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(exception.getMessage()))
                    .build();
        }
        logger.error("Unhandled exception: {}", exception.getMessage(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Unexpected error: " + exception.getMessage()))
                .build();
    }
}
