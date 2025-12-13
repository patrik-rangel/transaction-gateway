package br.com.patrik.antifraud.unit.domain.service;

import br.com.patrik.antifraud.domain.entity.Transaction;
import br.com.patrik.antifraud.domain.enums.TransactionResultStatus;
import br.com.patrik.antifraud.domain.exception.DuplicateTransactionException;
import br.com.patrik.antifraud.domain.exception.UnsupportedCurrencyException;
import br.com.patrik.antifraud.domain.gateway.CacheGateway;
import br.com.patrik.antifraud.domain.gateway.ConfigurationGateway;
import br.com.patrik.antifraud.domain.gateway.TransactionEventGateway;
import br.com.patrik.antifraud.domain.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    TransactionService service;

    @Mock
    CacheGateway cacheGateway;

    @Mock
    TransactionEventGateway eventGateway;

    @Mock
    ConfigurationGateway configGateway;

    @BeforeEach
    void setup() {

        Mockito.when(configGateway.getAllowedCurrencies()).thenReturn(Set.of("BRL", "USD"));

        this.service = new TransactionService(cacheGateway, eventGateway, configGateway);
    }

    @Test
    void shouldProcessTransactionSuccessfully() {
        var id = UUID.randomUUID();
        var transaction = Transaction.create(
                id,
                1000L,
                "BRL",
                "user1",
                null,
                "device1",
                -23.0,
                -46.0
        );

        Mockito.when(cacheGateway.shouldProcess(id)).thenReturn(true);

        var result = service.processTransaction(transaction);

        Assertions.assertEquals(TransactionResultStatus.PENDING, result.status());
        Mockito.verify(eventGateway, Mockito.times(1)).sendVerifiedTransaction(any());
    }

    @Test
    void shouldThrowException_WhenCurrencyIsNotAllowed() {
        var transaction = Transaction.create(
                UUID.randomUUID(),
                1000L,
                "JPY",
                "user1",
                null,
                "device1",
                null,
                null
        );

        Assertions.assertThrows(UnsupportedCurrencyException.class, () -> {
            service.processTransaction(transaction);
        });

        Mockito.verifyNoInteractions(cacheGateway);
        Mockito.verifyNoInteractions(eventGateway);
    }

    @Test
    void shouldThrowException_WhenTransactionIsDuplicate() {
        var id = UUID.randomUUID();
        var transaction = Transaction.create(id,
                1000L,
                "BRL",
                "user1",
                null,
                "device1",
                null,
                null
        );

        Mockito.when(cacheGateway.shouldProcess(id)).thenReturn(false);

        Assertions.assertThrows(DuplicateTransactionException.class, () -> {
            service.processTransaction(transaction);
        });

        Mockito.verify(eventGateway, Mockito.never()).sendVerifiedTransaction(any());
    }
}