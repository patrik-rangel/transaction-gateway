package br.com.patrik.antifraud.domain.exception;

public class InvalidDataException extends DomainException {

    public InvalidDataException(String message) {
        super("INVALID_DATA", message);
    }
}
