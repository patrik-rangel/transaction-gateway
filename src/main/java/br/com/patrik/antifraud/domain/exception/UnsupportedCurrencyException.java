package br.com.patrik.antifraud.domain.exception;

public class UnsupportedCurrencyException extends DomainException {

    public UnsupportedCurrencyException(String currency) {
        super("INVALID_CURRENCY", "Currency not supported: " + currency);
    }
}