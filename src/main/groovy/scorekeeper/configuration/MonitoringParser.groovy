package scorekeeper.configuration

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import scorekeeper.Metric
import scorekeeper.MetricTypes
import scorekeeper.MetricsEnvironmentSetupMessage
import scorekeeper.metrics.DatasourceMetrics
import scorekeeper.metrics.JMXMetrics

public class MonitoringParser {
    public void initializeMetrics(MetricsEnvironmentSetupMessage sm, Collection<Config> collections) {
        for (Config collection : collections) {
            String dsName = collection.hasPath("ds") ?: null
            for (Config cl : collection.getConfigList("monitors")) {
                if (isDbMetric(cl)) {
                    addDbMetric(dsName, cl, sm)
                } else if (isJMXMetric(cl)) {
                    addJMXMetric(cl, sm)
                } else if (isSite24x7Metric(cl)) {
                    addSite24x7Metric(cl, sm)
                }
            }
        }
    }

    private static boolean isDbMetric(Config cl) {
        return !cl.hasPath("type") || MetricTypes.db.name().equals(cl.getString("type"))
    }

    private static void addDbMetric(String dsName, Config cl, MetricsEnvironmentSetupMessage sm) {
        DatasourceMetrics dsm = getDatasourceByName(sm, dsName)
        if (cl.hasPath("ds")) {
            String individualDatasource = cl.getString("ds")
            dsm = getDatasourceByName(sm, individualDatasource)
        }

        Metric m = new Metric(getMetricName(cl))
        m.setSql(cl.getString("sql"))
        setFrequency(cl, m)
        if (cl.hasPath("group")) {
            m.setGrouped(cl.getBoolean("group"))
        }
        dsm.addMetrics(m)
    }

    private static void setFrequency(Config cl, Metric m) {
        if (cl.hasPath("s")){
            m.setFrequencyMs(cl.getInt("s") * 1000)
        } else {
            m.setFrequencyMs(cl.getInt("ms"))//deprecated
        }
    }

    private static DatasourceMetrics getDatasourceByName(MetricsEnvironmentSetupMessage sm, String dsName) {
        for (DatasourceMetrics dsm : sm.getDatasourceMetrics()) {
            if (dsName.equals(dsm.getName())) {
                return dsm
            }
        }
        throw new IllegalStateException("No datasource named " + dsName + " specified in system-props.conf")
    }

    private static boolean isJMXMetric(Config cl) {
        return cl.hasPath("type") && MetricTypes.jmx.name().equals(cl.getString("type"))
    }

    private static void addJMXMetric(Config cl, MetricsEnvironmentSetupMessage sm) {
        JMXMetrics jmxm = getJMXMetricsByName(sm, cl.getString("ds"))

        Metric m = new Metric(getMetricName(cl))
        m.setObjectName(cl.getString("objectName"))
        m.setAttribute(cl.getString("attribute"))
        setFrequency(cl, m)

        jmxm.addMetrics(m)
    }

    private static JMXMetrics getJMXMetricsByName(MetricsEnvironmentSetupMessage sm, String dsName) {
        for (JMXMetrics jmxm : sm.getJMXMetrics()) {
            if (dsName.equals(jmxm.getName())) {
                return jmxm
            }
        }
        throw new IllegalStateException("No JMX address named ${dsName} specified in system-props.conf")
    }

    private static boolean isSite24x7Metric(Config cl) {
        return cl.hasPath("type") && MetricTypes.site24x7.name().equals(cl.getString("type"))
    }

    private static void addSite24x7Metric(Config cl, MetricsEnvironmentSetupMessage sm) {
        Metric m = new Metric(getMetricName(cl))
        setFrequency(cl, m)
        m.setUrl(cl.getString("url"))

        sm.addSite24x7Metrics(m)
    }

    private static List<String> getMetricName(Config cl) {
        try {
            return cl.getStringList("metricName")
        } catch (ConfigException ce) {
            return Collections.singletonList(cl.getString("metricName"))
        }
    }
}
