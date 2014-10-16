package scorekeeper.metrics;

import scorekeeper.Metric;

import java.util.ArrayList;

public class Site24x7Metrics {

    private String name;
    private String url;
    private String path;
    private String apiKey;
    private ArrayList<Metric> metrics;

    public Site24x7Metrics(String name) {
        this.name = name;
        metrics = new ArrayList<Metric>();

    }

    public String getName() {
        return name;
    }

    public ArrayList<Metric> getMetrics() {
        return metrics;
    }

    public void addMetrics(Metric ... newMetrics){
        for (Metric metric: newMetrics){
            this.metrics.add(metric);
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
