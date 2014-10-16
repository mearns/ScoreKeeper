package scorekeeper.metrics;

import scorekeeper.Metric;

import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

public class DatasourceMetrics {
	private String name;
	private DataSource dataSource;
	private Collection<Metric> metrics;
	
	public DatasourceMetrics(String name) {
		this.name = name;
		metrics = new ArrayList<Metric>();
	}
	
	public String getName(){
		return name;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
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
