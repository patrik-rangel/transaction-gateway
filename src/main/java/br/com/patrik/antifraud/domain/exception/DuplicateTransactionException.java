package br.com.patrik.antifraud.domain.exception;

public class DuplicateTransactionException extends DomainException {

    public DuplicateTransactionException(String transactionId) {
        super("TRANSACTION_DUPLICATE", "Transaction is already under analysis: " + transactionId);
    }
}