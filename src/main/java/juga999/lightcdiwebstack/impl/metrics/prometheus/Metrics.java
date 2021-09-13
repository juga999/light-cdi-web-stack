package juga999.lightcdiwebstack.impl.metrics.prometheus;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;

public class Metrics {

    static final Counter requests = Counter.build()
            .name("http_requests_total")
            .help("Total HTTP requests.")
            .register();

    static final Histogram httpDurationsHistogram = Histogram.build()
            .name("http_request_duration_seconds").help("HTTP request duration in seconds.")
            .labelNames("endpoint")
            .register();

    static final Gauge inProgressTransactions = Gauge.build()
            .name("inprogress_transactions").help("Count of in progress transactions.")
            .labelNames("method")
            .register();

    static {
        DefaultExports.initialize();
    }

    public static void incHttpRequestsCount() {
        requests.inc();
    }

    public static Histogram.Timer startHttpDuration(String name) {
        return httpDurationsHistogram.labels(name).startTimer();
    }

    public static void incInProgressTransactions(String method) {
        inProgressTransactions.labels(method).inc();
    }

    public static void decInProgressTransactions(String method) {
        inProgressTransactions.labels(method).dec();
    }
}
