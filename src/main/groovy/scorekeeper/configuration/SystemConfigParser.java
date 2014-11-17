package scorekeeper.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;
import scorekeeper.MetricsEnvironmentSetupMessage;
import scorekeeper.metrics.DatasourceMetrics;
import scorekeeper.metrics.JMXMetrics;

import java.io.File;
import java.util.List;

public class SystemConfigParser {
    private static final String PATH_TO_SYSPROPS = "config/system-props.conf";

    public MetricsEnvironmentSetupMessage newStartupMessage() {
        File systemConf = new File(PATH_TO_SYSPROPS);

        Config systemCfg = ConfigFactory.parseFile(systemConf);

        MetricsEnvironmentSetupMessage sm = new MetricsEnvironmentSetupMessage();
        sm.setAppName(systemCfg.getString("app"));
        sm.setEnvName(systemCfg.getString("env"));
        sm.setHostName(systemCfg.getString("stats-server.host"));
        sm.setPort(systemCfg.getInt("stats-server.port"));

        initializeDatasources(systemCfg, sm);
        initializeJMX(systemCfg, sm);

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
