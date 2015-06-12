package scorekeeper.jmx;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.timgroup.statsd.StatsDClient;
import scorekeeper.Metric;
import scorekeeper.MetricsEnvironmentSetupMessage;

import javax.management.remote.JMXServiceURL;
import java.sql.SQLException;
import java.util.Collection;

public class JMXActorFactory {
	private ActorContext actorCtx;
	private JMXServiceURL serviceURL;
	private StatsDClient client;
	private MetricsEnvironmentSetupMessage setupMessage;

	public JMXActorFactory(MetricsEnvironmentSetupMessage setupMessage, StatsDClient client, JMXServiceURL jmxServiceURL, ActorContext actorCtx){
		this.setupMessage = setupMessage;
		this.client = client;
		this.serviceURL = jmxServiceURL;
		this.actorCtx = actorCtx;
	}
	
	protected void makeScalarActor(Metric m) throws SQLException {
		Props props = Props.create(JMXPollingActor.class, setupMessage, serviceURL, client, m)
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
