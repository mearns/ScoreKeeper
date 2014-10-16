package scorekeeper;

import java.util.ArrayList;
import java.util.Collection;

import scorekeeper.metrics.DatasourceMetrics;
import scorekeeper.metrics.HornetQMetrics;
import scorekeeper.metrics.JMXMetrics;
import scorekeeper.metrics.Site24x7Metrics;

public class MetricsEnvironmentSetupMessage {
	private String envName;
	private String hostName;
	private int port;
	private String appName;
	private Collection<DatasourceMetrics> dsMetricPoints;
	private Collection<JMXMetrics> jmxMetricPoints;
	private Collection<HornetQMetrics> jmsMetricPoints;
    private Collection<Site24x7Metrics> site24x7MetricPoints;
	
	public MetricsEnvironmentSetupMessage(){
		dsMetricPoints = new ArrayList<DatasourceMetrics>();
		jmxMetricPoints = new ArrayList<JMXMetrics>();
		jmsMetricPoints = new ArrayList<HornetQMetrics>();
        site24x7MetricPoints = new ArrayList<Site24x7Metrics>();
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
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
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
	
	public void addHornetQMetrics(HornetQMetrics... newMetrics) {
        for (HornetQMetrics jmsm: newMetrics){
            jmsMetricPoints.add(jmsm);
        }
    }

    public Collection<HornetQMetrics> getHornetQMetrics() {
        return jmsMetricPoints;
    }

    public void addSite24x7Metrics(Site24x7Metrics... newMetrics) {
        for (Site24x7Metrics jmsm: newMetrics){
            site24x7MetricPoints.add(jmsm);
        }
    }

    public Collection<Site24x7Metrics> getSite24x7Metrics() {
        return site24x7MetricPoints;
    }
}
