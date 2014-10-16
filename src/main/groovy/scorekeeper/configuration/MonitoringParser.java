package scorekeeper.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import scorekeeper.Metric;
import scorekeeper.MetricTypes;
import scorekeeper.MetricsEnvironmentSetupMessage;
import scorekeeper.metrics.DatasourceMetrics;
import scorekeeper.metrics.JMXMetrics;
import scorekeeper.metrics.Site24x7Metrics;

public class MonitoringParser {
    public void initializeMetrics(MetricsEnvironmentSetupMessage sm, Collection<Config> collections) {
        for (Config collection : collections) {
            String dsName = collection.getString("ds");
            for (Config cl : collection.getConfigList("monitors")) {
                if (isDbMetric(cl)) {
                    addDbMetric(dsName, cl, sm);
                } else if (isJMXMetric(cl)) {
                    addJMXMetric(cl, sm);
                } else if (isSite24x7Metric(cl)) {
                    addSite24x7Metric(cl, sm);
                }
            }
        }
    }

    private static boolean isDbMetric(Config cl) {
        return !cl.hasPath("type") || MetricTypes.db.name().equals(cl.getString("type"));
    }

    private static void addDbMetric(String dsName, Config cl, MetricsEnvironmentSetupMessage sm) {
        DatasourceMetrics dsm = getDatasourceByName(sm, dsName);
        if (cl.hasPath("ds")) {
            String individualDatasource = cl.getString("ds");
            dsm = getDatasourceByName(sm, individualDatasource);
        }

        Metric m = new Metric(getMetricName(cl));
        m.setSql(cl.getString("sql"));
        m.setFrequencyMs(cl.getInt("ms"));
        if (cl.hasPath("group")) {
            m.setGrouped(cl.getBoolean("group"));
        }
        dsm.addMetrics(m);
    }

    private static DatasourceMetrics getDatasourceByName(MetricsEnvironmentSetupMessage sm, String dsName) {
        for (DatasourceMetrics dsm : sm.getDatasourceMetrics()) {
            if (dsName.equals(dsm.getName())) {
                return dsm;
            }
        }
        throw new IllegalStateException("No datasource named " + dsName + " specified in system-props.conf");
    }

    private static boolean isJMXMetric(Config cl) {
        return cl.hasPath("type") && MetricTypes.jmx.name().equals(cl.getString("type"));
    }

    private static void addJMXMetric(Config cl, MetricsEnvironmentSetupMessage sm) {
        JMXMetrics jmxm = getJMXMetricsByName(sm, cl.getString("ds"));

        Metric m = new Metric(getMetricName(cl));
        m.setObjectName(cl.getString("objectName"));
        m.setAttribute(cl.getString("attribute"));
        m.setFrequencyMs(cl.getInt("ms"));

        jmxm.addMetrics(m);
    }

    private static JMXMetrics getJMXMetricsByName(MetricsEnvironmentSetupMessage sm, String dsName) {
        for (JMXMetrics jmxm : sm.getJMXMetrics()) {
            if (dsName.equals(jmxm.getName())) {
                return jmxm;
            }
        }
        throw new IllegalStateException("No JMX address named " + dsName + " specified in system-props.conf");
    }

    private static boolean isSite24x7Metric(Config cl) {
        return cl.hasPath("type") && MetricTypes.site24x7.name().equals(cl.getString("type"));
    }

    private static void addSite24x7Metric(Config cl, MetricsEnvironmentSetupMessage sm) {
        Site24x7Metrics site24x7Metrics = getSite24x7MetricsByName(sm, cl.getString("ds"));

        Metric m = new Metric(getMetricName(cl));
        m.setFrequencyMs(cl.getInt("ms"));
        m.setQueryString(cl.getString("queryString"));

        site24x7Metrics.addMetrics(m);
    }

    private static Site24x7Metrics getSite24x7MetricsByName(MetricsEnvironmentSetupMessage sm, String siteName) {
        for (Site24x7Metrics siteMetric : sm.getSite24x7Metrics()) {
            if (siteName.equals(siteMetric.getName())) {
                return siteMetric;
            }
        }
        throw new IllegalStateException("No Site24x7 address named " + siteName + " specified in system-props.conf");
    }

    private static List<String> getMetricName(Config cl) {
        try {
            return cl.getStringList("metricName");
        } catch (ConfigException ce) {
            return Collections.singletonList(cl.getString("metricName"));
        }
    }
}
