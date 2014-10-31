package scorekeeper.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scorekeeper.CircuitBrokenScheduledActor;
import scorekeeper.DeadActorException;
import scorekeeper.Metric;

import com.timgroup.statsd.StatsDClient;

import akka.actor.UntypedActor;

public abstract class SQLMetricsActor extends CircuitBrokenScheduledActor {
	private DataSource sqlDataSource; 
	private PreparedStatement statement;
	protected StatsDClient statsClient;
	protected Metric metric;

	SQLMetricsActor(DataSource sqlDataSource, StatsDClient statsD, Metric metric) throws SQLException{
		super(metric);
		this.sqlDataSource = sqlDataSource;
		this.statsClient = statsD;
		this.metric = metric;
		
		if (metric.getFrequencyMs() <= 1000){
			throw new DeadActorException(metric.getActorName() + " specifies a frequency of " + metric.getFrequencyMs() + ", which is way too fast." );
		}
	}
	
	protected PreparedStatement getStatement() throws SQLException{
		if (statement == null){
			statement = newPreparedStatement(metric.getSql());		
			validateStatement(statement, metric.getActorName());			
		}
		return statement;
	}

	protected void validateStatement(PreparedStatement ps, String metricName) {
		ResultSet rs = NullResultSet.getInstance();
		try { 
			rs = ps.executeQuery();
		} catch (SQLException sqlex) {
			if (sqlex.getMessage().contains("deadlock victim")){
				throw new IllegalStateException("Possible deadlock on startup of " + metricName, sqlex);
			} else {
				throw new DeadActorException(metricName + " specifies malformed SQL" , sqlex);
			}
		} finally {
			try {
				rs.close();
			} catch (SQLException e) { }
		}
	}

	protected PreparedStatement newPreparedStatement(String sql) throws SQLException{
		Connection sqlConnection = sqlDataSource.getConnection();
		sqlConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		return sqlConnection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}
	
	protected Runnable makeOpenHandler() {
		return new OpenSQLHandler();
	}

	private class OpenSQLHandler extends OpenHandler{
		@Override
		public void run() {
			super.run();
			statement = null;
		}
	}
}
