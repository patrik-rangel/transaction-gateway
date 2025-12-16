package br.com.patrik.antifraud.unit.application.controller;

import br.com.patrik.antifraud.application.controller.TransactionController;
import br.com.patrik.antifraud.domain.entity.Transaction;
import br.com.patrik.antifraud.domain.entity.TransactionResult;
import br.com.patrik.antifraud.domain.enums.TransactionResultStatus;
import br.com.patrik.antifraud.domain.service.TransactionService;
import br.com.patrik.antifraud.gateway.model.TransactionRequest;
import br.com.patrik.antifraud.gateway.model.TransactionResponse;
import br.com.patrik.antifraud.gateway.model.TransactionRequestLocation;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    TransactionService transactionService;

    @InjectMocks
    TransactionController controller;

    private TransactionRequest request;
    private UUID transactionId;

    @BeforeEach
    void setup() {
        transactionId = UUID.randomUUID();

        var location = new TransactionRequestLocation();
        location.setLatitude(-23.5);
        location.setLongitude(-46.6);

        request = new TransactionRequest();
        request.setAmount(1500);
        request.setCurrency("BRL");
        request.setUserId("user123");
        request.setTimestamp(new Date());
        request.setDeviceFingerprint("device-abc");
        request.setMcc("5834");
        request.setLocation(location);
    }

    @Test
    void shouldReturnAcceptedResponse_WhenTransactionIsProcessed() {
        var result = new TransactionResult(TransactionResultStatus.PENDING, "Transaction received");
        when(transactionService.processTransaction(any(Transaction.class))).thenReturn(result);

        Response response = controller.analyzeTransaction(transactionId, request);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());

        var entity = (TransactionResponse) response.getEntity();
        assertNotNull(entity);
        assertEquals(TransactionResponse.StatusEnum.PENDING, entity.getStatus());
        assertEquals("Transaction received", entity.getMessage());

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionService, times(1)).processTransaction(txCaptor.capture());

        var capturedTx = txCaptor.getValue();
        assertEquals(transactionId, capturedTx.transactionId());
        assertEquals("BRL", capturedTx.currency());
        assertEquals("user123", capturedTx.userId());
    }

    @Test
    void shouldHandleNullLocationGracefully() {
        request.setLocation(null);
        var result = new TransactionResult(TransactionResultStatus.PENDING, "OK");
        when(transactionService.processTransaction(any(Transaction.class))).thenReturn(result);

        Response response = controller.analyzeTransaction(transactionId, request);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
        verify(transactionService, times(1)).processTransaction(any(Transaction.class));
    }
}
