package br.com.patrik.antifraud.domain.entity;

import br.com.patrik.antifraud.domain.exception.InvalidDataException;

import java.util.Date;
import java.util.UUID;

public record Transaction(
        UUID transactionId,
        Long amount,
        String currency,
        String userId,
        Date timestamp,
        String deviceFingerPrint,
        String mcc,
        Location location
) {

    public static Transaction create(
            UUID transactionId,
            Long amount,
            String currency,
            String userId,
            Date timestamp,
            String deviceFingerPrint,
            String mcc,
            Double latitude,
            Double longitude
    ) {

        if (amount == null || amount < 0) {
            throw new InvalidDataException("Transaction amount cannot be negative");
        }

        return new Transaction(
                transactionId,
                amount,
                currency,
                userId,
                timestamp,
                deviceFingerPrint,
                mcc,
                Location.create(latitude, longitude)
        );
    }
}