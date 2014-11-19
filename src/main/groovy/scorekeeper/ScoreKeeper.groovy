package scorekeeper

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scorekeeper.configuration.MonitoringParser
import scorekeeper.configuration.SystemConfigParser
import scorekeeper.util.SampleConfigurationWriter

public class ScoreKeeper {
	private static Logger log = LoggerFactory.getLogger(ScoreKeeper.class)
	private ActorSystem system
	private ActorRef headRef
	private static MonitoringParser mp = new MonitoringParser()
	private static SystemConfigParser scp = new SystemConfigParser()

	public static void main(String[] args) throws InterruptedException {
		File workingDir = new File(System.getProperty("user.dir"))

		assureDirs(workingDir)

		MetricsEnvironmentSetupMessage sm = scp.newStartupMessage()
		mp.initializeMetrics(sm, getMonitoringConfig(args))

		ScoreKeeper sk = new ScoreKeeper(sm)
		sk.keepScore()
		log.info("Keeping score! CTRL-C to stop.")
		while (true){
			Thread.sleep(100)
		}
	}

	static def assureDirs(File root) {
		File config = new File(root, "config")
		File configFile = new File(config, "system-props.conf")
		File logbackConfigFile = new File(config, "logback.xml")
		File logs = new File(root, "logs")
		File monitoringConfigs = new File(root, "monitoring-conf")

		if (!config.exists()) config.mkdir()
		if (!logs.exists()) logs.mkdir()
		if (!monitoringConfigs.exists()){
			monitoringConfigs.mkdir()
			SampleConfigurationWriter.writeSampleConfig("jmx-monitoring.conf", 		new File(monitoringConfigs, "jmx-monitoring.conf"))
			SampleConfigurationWriter.writeSampleConfig("site24x7-monitoring.conf", new File(monitoringConfigs, "site24x7-monitoring.conf"))
			SampleConfigurationWriter.writeSampleConfig("sql-monitoring.conf", 		new File(monitoringConfigs, "sql-monitoring.conf"))
		}
		if (!configFile.exists()){
			SampleConfigurationWriter.writeSampleConfig("sample.system-props.conf", configFile);
			log.info("No system-props.conf found. Making new system config from template. You will need to fill it in before ScoreKeeper can run.")
			System.exit(1)
		}
	}

	private static Collection<Config> getMonitoringConfig(String[] args) {
		if (args.length == 0){

		}
		ArrayList<Config> al = new ArrayList<Config>()
		for (String arg: args){
			File f = new File(arg)
			if (f.exists() && f.isFile()){
				al.add(ConfigFactory.parseFile(f))
			} else {
				log.warn(f.getName() + " specified but not present")
			}
		}
		return al
	}
	
	ScoreKeeper(MetricsEnvironmentSetupMessage sm){
		Config c = fetchActorSystemConfig()
		system = ActorSystem.create("ScoreKeeper", c)
        headRef = system.actorOf(Props.create(HeadScorekeepingActor.class))
        headRef.tell(sm, ActorRef.noSender())
	}

	static Config fetchActorSystemConfig() {
		Config c = ConfigFactory.parseString("""
akka {
  loggers = ["akka.event.slf4j.Slf4jLogger"]
  loglevel = "DEBUG"
  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
}

sql-actor-dispatch {
    type = Dispatcher
     executor = "thread-pool-executor"
     thread-pool-executor {
         core-pool-size-min = 2
         core-pool-size-factor = 2.0
         core-pool-size-max = 10
     }
     throughput = 1
}""")
		return c
	}
	
	private void keepScore() {
		headRef.tell(new StartGameMessage(), ActorRef.noSender())
	}
}
