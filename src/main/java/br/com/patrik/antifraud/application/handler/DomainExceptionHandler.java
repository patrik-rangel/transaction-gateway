package br.com.patrik.antifraud.application.handler;

import br.com.patrik.antifraud.domain.exception.DomainException;
import br.com.patrik.antifraud.domain.exception.DuplicateTransactionException;
import br.com.patrik.antifraud.domain.exception.UnsupportedCurrencyException;
import br.com.patrik.antifraud.gateway.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainExceptionHandler implements ExceptionMapper<DomainException> {
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(DomainException exception) {
        return Response.status(mapStatus(exception))
                .entity(
                        new ErrorResponse().code(exception.getCode()).message(exception.getMessage())
                ).build();
    }

    private int mapStatus(DomainException ex) {
        return switch (ex) {
            case DuplicateTransactionException e -> Response.Status.CONFLICT.getStatusCode(); // 409
            case UnsupportedCurrencyException e  -> Response.Status.BAD_REQUEST.getStatusCode(); // 400
            default -> UNPROCESSABLE_ENTITY;
        };
    }
}