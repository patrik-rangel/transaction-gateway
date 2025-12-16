package br.com.patrik.antifraud.unit.domain.entity;

import br.com.patrik.antifraud.domain.entity.Transaction;
import br.com.patrik.antifraud.domain.exception.InvalidDataException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.Date;

class TransactionTest {

    @Test
    void shouldCreateTransaction_WhenDataIsValid() {
        var id = UUID.randomUUID();
        var now = new Date();

        var t = Transaction.create(
                id,
                100L,
                "BRL",
                "user123",
                now,
                "fingerprint-xyz",
                "5834",
                -23.5,
                -46.6
        );

        Assertions.assertNotNull(t);
        Assertions.assertEquals(id, t.transactionId());
        Assertions.assertEquals(100L, t.amount());
        Assertions.assertEquals("user123", t.userId());
        Assertions.assertNotNull(t.location());
        Assertions.assertEquals(-23.5, t.location().latitude());
        Assertions.assertEquals(-46.6, t.location().longitude());
    }

    @Test
    void shouldThrowException_WhenAmountIsNegative() {
        var id = UUID.randomUUID();

        Assertions.assertThrows(InvalidDataException.class, () ->
                Transaction.create(id,
                        -10L,
                        "BRL",
                        "user",
                        null,
                        "fp",
                        "5834",
                        null,
                        null
                )
        );
    }

    @Test
    void shouldThrowException_WhenAmountIsNull() {
        var id = UUID.randomUUID();

        Assertions.assertThrows(InvalidDataException.class, () ->
                Transaction.create(
                        id,
                        null,
                        "BRL",
                        "user",
                        null,
                        "fp",
                        "5834",
                        null,
                        null
                )
        );
    }

    @Test
    void shouldCreateTransactionWithNullLocation_WhenCoordinatesAreMissing() {
        var t = Transaction.create(
                UUID.randomUUID(),
                500L,
                "USD",
                "user",
                null,
                "fp",
                "5834",
                null,
                null
        );

        Assertions.assertNotNull(t);
        Assertions.assertNull(t.location(), "Location should be null when coordinates are missing");
    }

    @Test
    void shouldCreateTransactionWithNullLocation_WhenOnlyOneCoordinateIsPresent() {
        var t = Transaction.create(
                UUID.randomUUID(),
                500L,
                "USD",
                "user",
                null,
                "fp",
                "5834",
                -23.5,
                null
        );

        Assertions.assertNotNull(t);
        Assertions.assertNull(t.location(), "Location should be null if longitude is missing");
    }
}