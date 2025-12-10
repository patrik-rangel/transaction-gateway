package br.com.patrik.antifraud.application.controller;

import br.com.patrik.antifraud.gateway.api.TransactionsApi;
import br.com.patrik.antifraud.gateway.model.TransactionRequest;
import br.com.patrik.antifraud.gateway.model.TransactionResponse;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/transactions/{transaction_id}")
public class TransactionController implements TransactionsApi {

    @Override
    public Response analyzeTransaction(UUID transactionId, TransactionRequest transactionRequest) {
        TransactionResponse res = new TransactionResponse();
        res.setStatus(TransactionResponse.StatusEnum.PENDING);
        res.setMessage("teste");

        return Response.accepted(res).build();
    }
}
