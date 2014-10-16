package scorekeeper.metrics;

import scorekeeper.Metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXMetrics {
	private String name;
	private String host;
	private int port;
	private ArrayList<Metric> metrics;
	
	public JMXMetrics(String name) {
		this.name = name;
		metrics = new ArrayList<Metric>();
		
	}
	
	public String getName(){
		return name;
	}

	public Collection<Metric> getMetrics() {
		return metrics;
	}

	public void addMetrics(Metric ... newMetrics){
		for (Metric metric: newMetrics){
			this.metrics.add(metric);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public JMXServiceURL connectToMBeanServer() {
		try {
			return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
		} catch (IOException ce){
			throw new IllegalStateException("Can't connect to a JMX service at \"" + host + ":" + port + "\"", ce); 
		}
	}
}
