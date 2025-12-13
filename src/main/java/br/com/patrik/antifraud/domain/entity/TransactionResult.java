package br.com.patrik.antifraud.domain.entity;

import br.com.patrik.antifraud.domain.enums.TransactionResultStatus;

public record TransactionResult(
        TransactionResultStatus status,
        String message
) {}
