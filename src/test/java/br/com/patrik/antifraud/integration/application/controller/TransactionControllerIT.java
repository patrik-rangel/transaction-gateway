package br.com.patrik.antifraud.integration.application.controller;

import br.com.patrik.antifraud.application.controller.TransactionController;
import br.com.patrik.antifraud.domain.entity.TransactionVerifiedEvent;
import br.com.patrik.antifraud.gateway.model.TransactionRequest;
import br.com.patrik.antifraud.gateway.model.TransactionRequestLocation;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestHTTPEndpoint(TransactionController.class)
class TransactionControllerIT {

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    RedisDataSource redisDataSource;

    @BeforeEach
    void setup() {
        connector.sink("transactions-created").clear();
    }

    @Test
    void shouldProcessTransactionSuccessfully_HappyPath() {
        var transactionId = UUID.randomUUID();
        var request = new TransactionRequest();
        request.setAmount(1500);
        request.setCurrency("BRL");
        request.setUserId("user-integration-test");
        request.setDeviceFingerprint("fingerprint-123");
        request.setTimestamp(new Date());

        var location = new TransactionRequestLocation();
        location.setLatitude(-23.5);
        location.setLongitude(-46.6);
        request.setLocation(location);

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/" + transactionId)
                .then()
                .statusCode(202)
                .body("message", is("Transaction received for analysis"))
                .body("status", is("PENDING"));

        InMemorySink<TransactionVerifiedEvent> kafkaQueue = connector.sink("transactions-created");
        Assertions.assertEquals(1, kafkaQueue.received().size(), "Should have sent 1 message to Kafka");

        TransactionVerifiedEvent event = kafkaQueue.received().get(0).getPayload();
        Assertions.assertEquals(transactionId.toString(), event.transactionId());
        Assertions.assertEquals(1500.0, event.amount());

        boolean existsInRedis = redisDataSource.key().exists(transactionId.toString());
        Assertions.assertTrue(existsInRedis, "Transaction ID should be stored in Redis for idempotency");
    }

    @Test
    void shouldReturnConflict_WhenTransactionIsIdempotent() {
        var transactionId = UUID.randomUUID();
        var request = new TransactionRequest();
        request.setAmount(100);
        request.setCurrency("USD");

        given().contentType("application/json").body(request)
                .post("/" + transactionId)
                .then().statusCode(202);

        given().contentType("application/json").body(request)
                .when()
                .post("/" + transactionId)
                .then()
                // 3. Assert
                .statusCode(409) // Conflict
                .body("code", is("TRANSACTION_DUPLICATE"));

        InMemorySink<TransactionVerifiedEvent> kafkaQueue = connector.sink("transactions-created");
        Assertions.assertEquals(1, kafkaQueue.received().size(), "Should verify strictly 1 event sent");
    }

    @Test
    void shouldReturnBadRequest_WhenCurrencyNotSupported() {
        var transactionId = UUID.randomUUID();
        var request = new TransactionRequest();
        request.setAmount(100);
        request.setCurrency("XYZ");

        given().contentType("application/json").body(request)
                .when()
                .post("/" + transactionId)
                .then()
                .statusCode(400)
                .body("code", is("INVALID_CURRENCY"));

        Assertions.assertEquals(0, connector.sink("transactions-created").received().size());
    }
}