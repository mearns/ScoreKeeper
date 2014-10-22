package scorekeeper.configuration;

import java.io.File;
import java.util.List;

import com.typesafe.config.ConfigException;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scorekeeper.MetricsEnvironmentSetupMessage;
import scorekeeper.metrics.DatasourceMetrics;
import scorekeeper.metrics.JMXMetrics;
import scorekeeper.metrics.Site24x7Metrics;
import scorekeeper.util.SampleConfigurationWriter;

public class SystemConfigParser {
    private static final String PATH_TO_SYSPROPS = "config/system-props.conf";

    public MetricsEnvironmentSetupMessage newStartupMessage() {
        MetricsEnvironmentSetupMessage sm = null;
        File systemConf = new File(PATH_TO_SYSPROPS);
        try {
            Config systemCfg = ConfigFactory.parseFile(systemConf);

            sm = new MetricsEnvironmentSetupMessage();
            sm.setAppName(systemCfg.getString("app"));
            sm.setEnvName(systemCfg.getString("env"));
            sm.setHostName(systemCfg.getString("stats-server.host"));
            sm.setPort(systemCfg.getInt("stats-server.port"));

            initializeDatasources(systemCfg, sm);
            initializeJMX(systemCfg, sm);

        } catch (ConfigException.Missing missingConfig) {
            if (!systemConf.exists()) {
                System.out.println("No file " + PATH_TO_SYSPROPS + " could be found. A sample will be generated for you at " + PATH_TO_SYSPROPS + ".");
                SampleConfigurationWriter.writeSampleConfigAndDie();
            }
            throw missingConfig;
        }
        return sm;
    }

    protected static void initializeDatasources(Config test, MetricsEnvironmentSetupMessage sm) {
        List<? extends Config> dbs = test.getConfigList("dbs");
        for (Config db : dbs) {
            JtdsDataSource dataSource = new JtdsDataSource();
            dataSource.setServerName(db.getString("host"));
            dataSource.setDatabaseName(db.getString("db"));
            dataSource.setUser(db.getString("user"));
            dataSource.setPassword(db.getString("pass"));

            DatasourceMetrics dsMetrics = new DatasourceMetrics(db.getString("name"));
            dsMetrics.setDataSource(dataSource);
            sm.addDatasourceMetrics(dsMetrics);
        }
    }

    protected static void initializeJMX(Config test, MetricsEnvironmentSetupMessage sm) {
        List<? extends Config> jmxs = test.getConfigList("jmx");
        for (Config jmx : jmxs) {
            JMXMetrics jmxm = new JMXMetrics(jmx.getString("name"));

            jmxm.setHost(jmx.getString("host"));
            jmxm.setPort(jmx.getInt("port"));

            sm.addJMXMetrics(jmxm);
        }
    }
}
