package scorekeeper.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import scorekeeper.Metric;

import com.timgroup.statsd.StatsDClient;

public class AutomaticGroupedSQLMetricsActor extends SQLMetricsActor {
	AutomaticGroupedSQLMetricsActor(DataSource sqlDataSource,
			StatsDClient statsD, Metric metric) throws SQLException {
		super(sqlDataSource, statsD, metric);
	}

	protected void doTheWholeMetricsThing() throws SQLException {
		ResultSet rs = getStatement().executeQuery();
		while (rs.next()){
			handleRow(rs);
		}
		rs.close();		
	}

	protected void handleRow(ResultSet rs) throws SQLException {
		String grouping = rs.getString(1);
		
		int i = 1;
		for (String metricName: metric.getMetricNames()){
			double scalar = rs.getDouble(++i);
			writeCounter(grouping, scalar, metricName);
		}
	}


	protected void writeCounter(String grouping, double counter, String metricName) {
		statsClient.gauge(metricName + "." + grouping, counter);
		System.out.print("g");
		//System.out.println(metricName + "." + grouping + ":" + counter);
	}
}
