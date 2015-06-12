package scorekeeper.sql;

import com.timgroup.statsd.StatsDClient;
import scorekeeper.CircuitBrokenScheduledActor;
import scorekeeper.DeadActorException;
import scorekeeper.Metric;
import scorekeeper.MetricsEnvironmentSetupMessage;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SQLMetricsActor extends CircuitBrokenScheduledActor {
	private DataSource sqlDataSource; 
	private PreparedStatement statement;
	protected StatsDClient statsClient;
	protected Metric metric;

	SQLMetricsActor(MetricsEnvironmentSetupMessage setupMessage, DataSource sqlDataSource, StatsDClient statsD, Metric metric) throws SQLException{
		super(setupMessage, metric);
		this.sqlDataSource = sqlDataSource;
		this.statsClient = statsD;
		this.metric = metric;
		
		if (metric.getFrequencyMs() <= 1000){
			throw new DeadActorException(metric.getActorName() + " specifies a frequency of " + metric.getFrequencyMs() + ", which is way too fast." );
		}
	}
	
	protected PreparedStatement getStatement() throws SQLException{

		//If we already have a statement, we will try to reuse it. But if it's closed, throw it away and get a new one.
		if(statement != null) {
			try {
				if (statement.isClosed()) {
					statement = null;
				}
			} catch (SQLException e) {
				try {
					statement.close();
				} catch (SQLException e2) {
					//Nothing to be done
				}
				statement = null;
			}
		}

		//Deliberately not an else if.
		if (statement == null) {
			statement = newPreparedStatement(metric.getSql());		
			validateStatement(statement, metric.getActorName());			
		}
		return statement;
	}

	protected ResultSet executeStatement() throws SQLException {
		PreparedStatement stmt = this.getStatement();
		try {
			return stmt.executeQuery();
		} catch(SQLException e) {
			//Exception could be network issue. In case it is, we want to throw away the
			// connection, make sure we don't try again with the same one next time.
			try {
				stmt.close();
			} catch(SQLException e2) {
				// nop
			}
			this.statement = null;
			throw e;
		}
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
