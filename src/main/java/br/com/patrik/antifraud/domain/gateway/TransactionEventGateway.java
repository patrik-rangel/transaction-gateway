package br.com.patrik.antifraud.domain.gateway;

import br.com.patrik.antifraud.domain.entity.Transaction;

public interface TransactionEventGateway {
    void sendVerifiedTransaction(Transaction event);
}