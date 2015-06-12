package scorekeeper.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import scorekeeper.Metric;

import com.timgroup.statsd.StatsDClient;
import scorekeeper.MetricsEnvironmentSetupMessage;

public class AutomaticSQLMetricsActor extends SQLMetricsActor {
	AutomaticSQLMetricsActor(MetricsEnvironmentSetupMessage setupMessage, DataSource sqlDataSource, StatsDClient statsD,
			Metric metric) throws SQLException {
		super(setupMessage, sqlDataSource, statsD, metric);
	}
	
	protected void doTheWholeMetricsThing() throws SQLException {
		ResultSet rs = getStatement().executeQuery();
		rs.next();
		
		int i = 0;
		for (String metricName: metric.getMetricNames()){
			double scalar = rs.getDouble(++i);
			writeCounter(scalar, metricName);
		}
		
		rs.close();
	}

	protected void writeCounter(double counter, String metricName) {
		statsClient.gauge(metricName, counter);
		System.out.print("s");
//		System.out.println(metricName + ":" + counter);
	}
}
