package br.com.patrik.antifraud.infrastructure.redis;

import br.com.patrik.antifraud.domain.gateway.CacheGateway;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.UUID;

@ApplicationScoped
public class RedisCacheAdapter implements CacheGateway {
    @ConfigProperty(name = "antifraud.cache.ttl")
    long ttlSeconds;
    @ConfigProperty(name = "antifraud.cache.prefix")
    String keyPrefix;

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;

    public RedisCacheAdapter(RedisDataSource ds) {
        this.valueCommands = ds.value(String.class);
        this.keyCommands = ds.key();
    }

    @Override
    public boolean shouldProcess(UUID transactionId) {
        String key = keyPrefix + transactionId;

        boolean acquired = valueCommands.setnx(key, "PROCESSING");

        if (acquired) {
            keyCommands.expire(key, ttlSeconds);
        }

        return acquired;
    }
}
