package scorekeeper.metrics;

import scorekeeper.Metric;

import java.util.ArrayList;

public class Site24x7Metrics {
    private ArrayList<Metric> metrics;

    public Site24x7Metrics() {
        metrics = new ArrayList<Metric>();
    }
    public ArrayList<Metric> getMetrics() {
        return metrics;
    }

    public void addMetrics(Metric ... newMetrics){
        for (Metric metric: newMetrics){
            this.metrics.add(metric);
        }
    }
}
