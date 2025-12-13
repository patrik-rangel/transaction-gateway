package br.com.patrik.antifraud.domain.entity;

public record TransactionVerifiedEvent(
        String transactionId,
        double amount,
        String status
) {}
