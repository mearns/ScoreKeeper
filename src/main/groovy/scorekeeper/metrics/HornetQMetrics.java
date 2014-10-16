package scorekeeper.metrics;

import scorekeeper.Metric;

import java.util.ArrayList;
import java.util.Collection;

public class HornetQMetrics {
	private String name;
	private ArrayList<Metric> metrics;
	private String jndiHost;
	
	public HornetQMetrics(String name) {
		this.name = name;
		metrics = new ArrayList<Metric>();
	}

	public String getJndiHost() {
		return jndiHost;
	}

	public void setJndiHost(String jndiHost) {
		this.jndiHost = jndiHost;
	}
	
	public Collection<Metric> getMetrics() {
		return metrics;
	}

	public void addMetrics(Metric ... newMetrics){
		for (Metric metric: newMetrics){
			this.metrics.add(metric);
		}
	}

}
