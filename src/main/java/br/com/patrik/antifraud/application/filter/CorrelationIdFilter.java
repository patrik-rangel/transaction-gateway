package br.com.patrik.antifraud.application.filter;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.jboss.logging.MDC;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

public class CorrelationIdFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @ServerRequestFilter(preMatching = true)
    public void filterRequest(ContainerRequestContext requestContext) {
        String correlationId = requestContext.getHeaderString(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UlidCreator.getUlid().toString();
        }

        MDC.put(MDC_KEY, correlationId);

        requestContext.setProperty(MDC_KEY, correlationId);
    }

    @ServerResponseFilter
    public void filterResponse(ContainerResponseContext responseContext, ContainerRequestContext requestContext) {
        Object correlationId = requestContext.getProperty(MDC_KEY);

        if (correlationId != null) {
            responseContext.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        }

        MDC.remove(MDC_KEY);
    }
}