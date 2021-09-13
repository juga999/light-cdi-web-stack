package juga999.lightcdiwebstack.impl.metrics.prometheus.endpoint;

import io.prometheus.client.CollectorRegistry;
import juga999.lightcdiwebstack.impl.metrics.prometheus.exporter.TextFormat;
import juga999.lightcdiwebstack.meta.http.AppEndpoint;
import juga999.lightcdiwebstack.meta.http.Context;
import juga999.lightcdiwebstack.meta.http.Get;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

public class MetricsEndpoint extends AppEndpoint {

    @Get("/metrics")
    public Object getMetrics(Context context) throws Exception {
        CollectorRegistry registry = CollectorRegistry.defaultRegistry;
        StringWriter stringWriter = new StringWriter();
        TextFormat.write004(stringWriter, registry.filteredMetricFamilySamples(parseQuery(context.getQueryString())));
        context.setContentType(TextFormat.CONTENT_TYPE_004);
        String data = stringWriter.toString();
        return data;
    }

    protected static Set<String> parseQuery(String query) throws IOException {
        Set<String> names = new HashSet<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
                    names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return names;
    }
}
