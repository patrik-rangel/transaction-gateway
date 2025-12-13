package br.com.patrik.antifraud.domain.gateway;

import java.util.Set;

public interface ConfigurationGateway {
    Set<String> getAllowedCurrencies();
}
