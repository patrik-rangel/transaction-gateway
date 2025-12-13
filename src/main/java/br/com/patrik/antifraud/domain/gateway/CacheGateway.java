package br.com.patrik.antifraud.domain.gateway;

import java.util.UUID;

public interface CacheGateway {
    boolean shouldProcess(UUID transactionId);
}
