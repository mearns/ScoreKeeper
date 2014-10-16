package scorekeeper.sql;

import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import scorekeeper.Metric;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.timgroup.statsd.StatsDClient;

public class SQLActorFactory {
	private StatsDClient client;
	private DataSource sqlDataSource;
	private ActorContext actorContext;

	public SQLActorFactory(StatsDClient client, DataSource dataSource, ActorContext actorCtx){
		this.client = client;
		this.sqlDataSource = dataSource;
		this.actorContext = actorCtx;
	}
	
	protected void makeScalarActor(Metric m) throws SQLException {
		Props props = Props.create(AutomaticSQLMetricsActor.class, sqlDataSource, client, m)
				.withDispatcher("sql-actor-dispatch");
		newActorFromProps(m.getActorName(), props);
	}
	
	protected void makeGroupedActor(Metric m) throws SQLException {
		Props props = Props.create(AutomaticGroupedSQLMetricsActor.class, sqlDataSource, client, m)
				.withDispatcher("sql-actor-dispatch");
		newActorFromProps(m.getActorName(), props);
	}

	protected void newActorFromProps(String metricName, Props props)
			throws SQLException {
		ActorRef ar = actorContext.actorOf(props, metricName);
	}
	
	public void makeMetricsForDatasource(Collection<Metric> collMet) throws SQLException {
		for (Metric m: collMet){
			if (m.isGrouped()){
				makeGroupedActor(m);
			} else {
				makeScalarActor(m);
			}
		}
	}
}
