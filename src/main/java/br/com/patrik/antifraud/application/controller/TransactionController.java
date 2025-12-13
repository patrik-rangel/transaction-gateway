package br.com.patrik.antifraud.application.controller;

import br.com.patrik.antifraud.domain.entity.Transaction;
import br.com.patrik.antifraud.domain.entity.TransactionResult;
import br.com.patrik.antifraud.domain.service.TransactionService;
import br.com.patrik.antifraud.gateway.api.TransactionsApi;
import br.com.patrik.antifraud.gateway.model.TransactionRequest;
import br.com.patrik.antifraud.gateway.model.TransactionResponse;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/transactions/{transaction_id}")
public class TransactionController implements TransactionsApi {
    private final TransactionService transactionService;

    @Inject
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    @RunOnVirtualThread
    public Response analyzeTransaction(UUID transactionId, TransactionRequest req) {

        Double latitude = null;
        Double longitude = null;
        if (req.getLocation() != null) {
            latitude = req.getLocation().getLatitude();
            longitude = req.getLocation().getLongitude();
        }

        Transaction transaction = Transaction.create(
                transactionId,
                Long.valueOf(req.getAmount()),
                req.getCurrency(),
                req.getUserId(),
                req.getTimestamp(),
                req.getDeviceFingerprint(),
                latitude,
                longitude
        );

        TransactionResult result = transactionService.processTransaction(transaction);
        TransactionResponse response = new TransactionResponse();

        response.setStatus(TransactionResponse.StatusEnum.fromValue(result.status().name()));
        response.setMessage(result.message());

        return Response.accepted(response).build();
    }
}