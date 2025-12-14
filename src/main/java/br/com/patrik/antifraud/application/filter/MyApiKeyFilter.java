package br.com.patrik.antifraud.application.filter;

import br.com.patrik.antifraud.gateway.model.ErrorResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

public class MyApiKeyFilter {

    @ConfigProperty(name = "antifraud.security.api-key")
    String expectedApiKey;

    @ServerRequestFilter(preMatching = true)
    public Response filterApiKey(ContainerRequestContext containerRequestContext) {
        String authHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.equals(expectedApiKey)) {

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse()
                            .code("UNAUTHORIZED")
                            .message("Invalid or missing API Key"))
                    .build();
        }
        return null;
    }
}