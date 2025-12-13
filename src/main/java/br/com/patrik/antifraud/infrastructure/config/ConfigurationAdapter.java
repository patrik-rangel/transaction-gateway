package br.com.patrik.antifraud.infrastructure.config;

import br.com.patrik.antifraud.domain.gateway.ConfigurationGateway;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@ApplicationScoped
public class ConfigurationAdapter implements ConfigurationGateway {

    @ConfigProperty(name = "antifraud.business.allowed-currencies")
    Set<String> allowedCurrencies;

    @Override
    public Set<String> getAllowedCurrencies() {
        return allowedCurrencies;
    }
}