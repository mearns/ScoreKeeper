package scorekeeper.site24x7;

import com.timgroup.statsd.StatsDClient;
import scorekeeper.CircuitBrokenScheduledActor;
import scorekeeper.Metric;

import java.awt.*;
import java.util.List;

public class Site24x7PollingActor extends CircuitBrokenScheduledActor {
    private final Site24x7Client site24x7Client;
    private final StatsDClient statsClient;
    private final Metric metric;

    public Site24x7PollingActor(String url, StatsDClient sdc, Metric metric) {
        super(metric);
        this.site24x7Client = new Site24x7Client(url, metric.getQueryString());
        this.statsClient = sdc;
        this.metric = metric;
    }

    @Override
    protected void doTheWholeMetricsThing() throws Exception {
        List<Site24x7Monitor> monitors = site24x7Client.getMonitors();
        for (Site24x7Monitor monitor : monitors) {
            String metricName = buildMetricName(monitor.getDisplayName());
            if (metric.getMetricNames().get(0).contains("is-up")) {
                if (monitor.getStatus().equals("Up")) {
                    writeCounter(1, metricName);
                } else {
                    writeCounter(0, metricName);
                }
            } else {
                int rspValue = extractNumericRSPValue(monitor.getRspValue());
                writeCounter(rspValue, metricName);
            }
        }
        System.out.print("7");
    }

    private void writeCounter(int counter, String metricName) {
        statsClient.gauge(metricName, counter);
    }

    private String buildMetricName(String displayName) {
        String allLowerWithDashes = displayName.toLowerCase().replaceAll("[^A-Za-z0-9]", "-");
        return metric.getMetricNames().get(0) + "." + allLowerWithDashes;
    }

    private int extractNumericRSPValue(String rspValue) {
        String hopeFullyANumber = rspValue.replaceAll("[^0-9]*", "");
        try {
            return Integer.parseInt(hopeFullyANumber);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
