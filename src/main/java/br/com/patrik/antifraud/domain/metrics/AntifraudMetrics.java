package br.com.patrik.antifraud.domain.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AntifraudMetrics {

    private static final String METRIC_TRANSACTIONS_TOTAL = "antifraud_transactions_total";
    private static final String METRIC_ANALYSIS_DURATION = "antifraud_analysis_duration_seconds";

    private final MeterRegistry registry;

    @Inject
    public AntifraudMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void incrementTransactionCount(String currency, String status) {
        registry.counter(METRIC_TRANSACTIONS_TOTAL,
                Tags.of("currency", currency, "status", status)
        ).increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample, String status) {
        if (sample != null) {
            sample.stop(registry.timer(METRIC_ANALYSIS_DURATION,
                    Tags.of("status", status))
            );
        }
    }
}