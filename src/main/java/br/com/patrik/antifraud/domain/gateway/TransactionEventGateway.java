package br.com.patrik.antifraud.domain.gateway;

import br.com.patrik.antifraud.domain.entity.TransactionVerifiedEvent;

public interface TransactionEventGateway {
    void sendVerifiedTransaction(TransactionVerifiedEvent event);
}