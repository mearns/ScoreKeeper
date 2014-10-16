package scorekeeper.jmx;

import java.sql.SQLException;
import java.util.Collection;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;
import javax.sql.DataSource;

import scorekeeper.Metric;
import scorekeeper.sql.AutomaticSQLMetricsActor;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.timgroup.statsd.StatsDClient;

public class JMXActorFactory {
	private ActorContext actorCtx;
	private JMXServiceURL serviceURL;
	private StatsDClient client;

	public JMXActorFactory(StatsDClient client, JMXServiceURL jmxServiceURL, ActorContext actorCtx){
		this.client = client;
		this.serviceURL = jmxServiceURL;
		this.actorCtx = actorCtx;
	}
	
	protected void makeScalarActor(Metric m) throws SQLException {
		Props props = Props.create(JMXPollingActor.class, serviceURL, client, m)
				.withDispatcher("sql-actor-dispatch");
		newActorFromProps(m.getActorName(), props);
	}
	
	protected void newActorFromProps(String metricName, Props props)
			throws SQLException {
		ActorRef ar = actorCtx.actorOf(props, metricName);
	}
	
	public void makeMetricsForDatasource(Collection<Metric> collMet) throws SQLException {
		for (Metric m: collMet){
			makeScalarActor(m);
		}
	}
}
