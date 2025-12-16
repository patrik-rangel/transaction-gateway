package br.com.patrik.antifraud.domain.service;

import br.com.patrik.antifraud.domain.entity.Transaction;
import br.com.patrik.antifraud.domain.entity.TransactionResult;
import br.com.patrik.antifraud.domain.enums.TransactionResultStatus;
import br.com.patrik.antifraud.domain.exception.DuplicateTransactionException;
import br.com.patrik.antifraud.domain.exception.UnsupportedCurrencyException;
import br.com.patrik.antifraud.domain.gateway.CacheGateway;
import br.com.patrik.antifraud.domain.gateway.ConfigurationGateway;
import br.com.patrik.antifraud.domain.gateway.TransactionEventGateway;
import br.com.patrik.antifraud.domain.metrics.AntifraudMetrics;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.UUID;

@ApplicationScoped
public class TransactionService {
    private static final Logger log = Logger.getLogger(TransactionService.class);

    private final CacheGateway cacheGateway;
    private final TransactionEventGateway eventGateway;
    private final ConfigurationGateway configurationGateway;
    private final AntifraudMetrics metrics;

    @Inject
    public TransactionService(
            CacheGateway cacheGateway,
            TransactionEventGateway eventGateway,
            ConfigurationGateway configurationGateway,
            AntifraudMetrics metrics
    ) {
        this.cacheGateway = cacheGateway;
        this.eventGateway = eventGateway;
        this.configurationGateway = configurationGateway;
        this.metrics = metrics;
    }

    public TransactionResult processTransaction(Transaction transaction) {
        Timer.Sample sample = metrics.startTimer();
        boolean hasError = false;

        try {
            validateCurrency(transaction.currency());
            checkIdempotency(transaction.transactionId());

            log.infof("Starting processing for transaction %s", transaction.transactionId());

            eventGateway.sendVerifiedTransaction(transaction);

            metrics.incrementTransactionCount(transaction.currency(), "SUCCESS");

            return buildResponse(TransactionResultStatus.PENDING, "Transaction received for analysis");
        } catch (DuplicateTransactionException | UnsupportedCurrencyException e) {
            hasError = true;
            metrics.incrementTransactionCount(transaction.currency(), "BUSINESS_ERROR");
            throw e;
        } catch (Exception e) {
            hasError = true;
            metrics.incrementTransactionCount(transaction.currency(), "APPLICATION_ERROR");
            throw e;
        } finally {
            if (hasError) {
                metrics.stopTimer(sample, "ERROR");
            } else {
                metrics.stopTimer(sample, "SUCCESS");
            }
        }
    }

    private void validateCurrency(String currency) {
        if (!configurationGateway.getAllowedCurrencies().contains(currency.toUpperCase())) {
            log.warnf("Attempted transaction with unsupported currency: %s", currency);
            throw new UnsupportedCurrencyException(currency);
        }
    }

    private void checkIdempotency(UUID transactionId) {
        if (!cacheGateway.shouldProcess(transactionId)) {
            log.warnf("Transaction %s is already being processed", transactionId);
            throw new DuplicateTransactionException(transactionId.toString());
        }
    }

    private TransactionResult buildResponse(TransactionResultStatus status, String message) {
        return new TransactionResult(
                status,
                message
        );
    }
}