package br.com.patrik.antifraud.infrastructure.kafka;

import br.com.patrik.antifraud.domain.entity.TransactionVerifiedEvent;
import br.com.patrik.antifraud.domain.gateway.TransactionEventGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaProducerAdapter implements TransactionEventGateway {
    private static final Logger log = Logger.getLogger(KafkaProducerAdapter.class);

    private final Emitter<TransactionVerifiedEvent> emitter;

    @Inject
    public KafkaProducerAdapter(@Channel("transactions-created") Emitter<TransactionVerifiedEvent> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void sendVerifiedTransaction(TransactionVerifiedEvent event) {
        emitter.send(event);
        log.infov("Send transaction to verification. Transaction Id: %s", event.transactionId());
    }
}
