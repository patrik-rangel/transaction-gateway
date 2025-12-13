package br.com.patrik.antifraud.domain.service;

import br.com.patrik.antifraud.domain.entity.Transaction;
import br.com.patrik.antifraud.domain.entity.TransactionResult;
import br.com.patrik.antifraud.domain.entity.TransactionVerifiedEvent;
import br.com.patrik.antifraud.domain.enums.TransactionResultStatus;
import br.com.patrik.antifraud.domain.exception.DuplicateTransactionException;
import br.com.patrik.antifraud.domain.exception.UnsupportedCurrencyException;
import br.com.patrik.antifraud.domain.gateway.CacheGateway;
import br.com.patrik.antifraud.domain.gateway.ConfigurationGateway;
import br.com.patrik.antifraud.domain.gateway.TransactionEventGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransactionService {
    private static final Logger log = Logger.getLogger(TransactionService.class);

    private final CacheGateway cacheGateway;
    private final TransactionEventGateway eventGateway;
    private final ConfigurationGateway configurationGateway;

    @Inject
    public TransactionService(
            CacheGateway cacheGateway,
            TransactionEventGateway eventGateway,
            ConfigurationGateway configurationGateway
    ) {
        this.cacheGateway = cacheGateway;
        this.eventGateway = eventGateway;
        this.configurationGateway = configurationGateway;
    }

    public TransactionResult processTransaction(Transaction transaction) {

        validateCurrency(transaction.currency());

        log.infof("Starting processing for transaction %s", transaction.transactionId());

        if (!cacheGateway.shouldProcess(transaction.transactionId())) {
            log.warnf("Transaction %s is already being processed", transaction.transactionId());
            throw new DuplicateTransactionException(transaction.transactionId().toString());
        }

        var event = new TransactionVerifiedEvent(
                transaction.transactionId().toString(),
                Double.valueOf(transaction.amount()),
                "PENDING_ANALYSIS"
        );

        eventGateway.sendVerifiedTransaction(event);

        return buildResponse(TransactionResultStatus.PENDING, "Transaction received for analysis");
    }

    private void validateCurrency(String currency) {
        if (!configurationGateway.getAllowedCurrencies().contains(currency.toUpperCase())) {
            log.warnf("Attempted transaction with unsupported currency: %s", currency);
            throw new UnsupportedCurrencyException(currency);
        }
    }

    private TransactionResult buildResponse(TransactionResultStatus status, String message) {
        return new TransactionResult(
                status,
                message
        );
    }
}