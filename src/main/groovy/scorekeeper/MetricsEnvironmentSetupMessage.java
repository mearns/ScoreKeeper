package scorekeeper;

import java.util.ArrayList;
import java.util.Collection;

import scorekeeper.metrics.DatasourceMetrics;
import scorekeeper.metrics.JMXMetrics;

public class MetricsEnvironmentSetupMessage {
	private String envName;
	private String hostName;
	private int statsDPort = 8125;
	private int graphiteTCPPort = 2300;
	private String appName;
	private Collection<DatasourceMetrics> dsMetricPoints;
	private Collection<JMXMetrics> jmxMetricPoints;
    private Collection<Metric> site24x7MetricPoints;
	private int defaultPollingIntervalMS = 30 * 1000;

	private int maxQueryTimeoutS = 120;

	private int timeBetweenRetriesS = 30;

	public MetricsEnvironmentSetupMessage(){
		dsMetricPoints = new ArrayList<>();
		jmxMetricPoints = new ArrayList<>();
        site24x7MetricPoints = new ArrayList<>();
	}
	
	public String getEnvName() {
		return envName;
	}
	public void setEnvName(String envName) {
		this.envName = envName;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getStatsDPort() {
		return statsDPort;
	}
	public void setStatsDPort(int statsDPort) {
		this.statsDPort = statsDPort;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName){
		this.appName = appName;
	}
	
	public void addDatasourceMetrics(DatasourceMetrics ... newMetrics){
		for (DatasourceMetrics dsm: newMetrics){
			dsMetricPoints.add(dsm);
		}
	}

	/**
	 * The maximum timeout for queries, in seconds. Default is 120s (2 minutes).
	 */
	public int getMaxQueryTimeoutS() {
		return maxQueryTimeoutS;
	}
	public void setMaxQueryTimeoutS(int maxQueryTimeoutS) {
		this.maxQueryTimeoutS = maxQueryTimeoutS;
	}

	public int getTimeBetweenRetriesS() {
		return timeBetweenRetriesS;
	}
	public void setTimeBetweenRetriesS(int timeBetweenRetriesS) {
		this.timeBetweenRetriesS = timeBetweenRetriesS;
	}

	public Collection<DatasourceMetrics> getDatasourceMetrics() {
		return dsMetricPoints;
	}

	public void addJMXMetrics(JMXMetrics ... newMetrics) {
		for (JMXMetrics jmxm: newMetrics){
			jmxMetricPoints.add(jmxm);
		}
	}
	
	public Collection<JMXMetrics> getJMXMetrics() {
		return jmxMetricPoints;
	}

    public void addSite24x7Metrics(Collection newMetrics) {
        site24x7MetricPoints.addAll(newMetrics);
    }

    public Collection<Metric> getSite24x7Metrics() {
        return site24x7MetricPoints;
    }

	public int getGraphiteTCPPort() {
		return graphiteTCPPort;
	}

	public void setGraphiteTCPPort(int graphiteTCPPort) {
		this.graphiteTCPPort = graphiteTCPPort;
	}

	public int getDefaultPollingIntervalMS() {
		return defaultPollingIntervalMS;
	}

	public void setDefaultPollingIntervalMS(int defaultPollingIntervalMS) {
		this.defaultPollingIntervalMS = defaultPollingIntervalMS;
	}
}
