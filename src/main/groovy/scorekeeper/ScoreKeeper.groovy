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
		while (true){
			Thread.sleep(100)
		}
	}

	/*
	/config
	/config/system-props.conf
	/logs
	/lib/*`

	 */
	static def assureDirs(File root) {
		File config = new File(root, "config")
		File configFile = new File(config, "system-props.conf")
		File logs = new File(root, "logs")
		File libs = new File(root, "libs")

		if (!config.exists()) config.mkdir()
		if (!logs.exists()) logs.mkdir()
		if (!libs.exists()) libs.mkdir()
		if (!configFile.exists()){
			log.info("No system-props.conf found. Making new system config from template.")
			SampleConfigurationWriter.writeSampleConfigAndDie();
		}
	}

	private static Collection<Config> getMonitoringConfig(String[] args) {
		ArrayList<Config> al = new ArrayList<Config>()
		for (String arg: args){
			File f = new File(arg)
			if (f.exists() && f.isFile()){
				al.add(ConfigFactory.parseFile(f))
			} else {
				System.out.println(f.getName() + " specified but not present")
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
