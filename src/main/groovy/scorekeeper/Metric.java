package scorekeeper;

import java.util.List;

public class Metric {
    private boolean grouped;
    private List<String> metricName;
    private String sql;
    private int frequencyMs;
    private MetricTypes metricTypes = MetricTypes.db;
    private String objectName;
    private String attribute;
    private String url;

    public Metric(List<String> list) {
        this.metricName = list;
    }

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    public List<String> getMetricNames() {
        return metricName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getFrequencyMs() {
        return frequencyMs;
    }

    public void setFrequencyMs(int frequencyMs) {
        this.frequencyMs = frequencyMs;
    }

    public String getActorName() {
        return metricName.get(0);
    }

    public MetricTypes getMetricTypes() {
        return metricTypes;
    }

    public void setMetricTypes(MetricTypes metricTypes) {
        this.metricTypes = metricTypes;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
