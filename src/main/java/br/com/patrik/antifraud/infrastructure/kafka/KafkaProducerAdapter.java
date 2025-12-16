package br.com.patrik.antifraud.infrastructure.kafka;

import br.com.patrik.antifraud.domain.entity.Transaction;
import br.com.patrik.antifraud.domain.gateway.TransactionEventGateway;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.nio.charset.StandardCharsets;
import java.util.List;

@ApplicationScoped
public class KafkaProducerAdapter implements TransactionEventGateway {
    private static final Logger log = Logger.getLogger(KafkaProducerAdapter.class);

    private final Emitter<Transaction> emitter;

    @Inject
    public KafkaProducerAdapter(@Channel("transactions-created") Emitter<Transaction> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void sendVerifiedTransaction(Transaction event) {
        try {
            String correlationId = (String) MDC.get("correlationId");

            if (correlationId == null) {
                correlationId = "UNKNOWN";
            }

            log.infof("Sending event to Kafka with Correlation-ID: %s", correlationId);

            var metadata = OutgoingKafkaRecordMetadata.builder()
                    .withHeaders(List.of(
                            new RecordHeader(
                                    "X-Correlation-ID",
                                    correlationId.getBytes(StandardCharsets.UTF_8)
                            )
                    ))
                    .build();

            Message<Transaction> message = Message.of(event)
                    .addMetadata(metadata);

            emitter.send(message);

        } catch (Exception e) {
            log.error("Error sending message to Kafka", e);
        }
    }
}
