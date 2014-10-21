package scorekeeper

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import scorekeeper.configuration.MonitoringParser
import scorekeeper.configuration.SystemConfigParser

public class ScoreKeeper {
	private ActorSystem system
	private ActorRef headRef
	private static MonitoringParser mp = new MonitoringParser()
	private static SystemConfigParser scp = new SystemConfigParser()

	public static void main(String[] args) throws InterruptedException {	
		MetricsEnvironmentSetupMessage sm = scp.newStartupMessage()
		mp.initializeMetrics(sm, getMonitoringConfig(args))
		
		ScoreKeeper sk = new ScoreKeeper(sm)
		sk.keepScore()
		while (true){
			Thread.sleep(100)
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

	protected Config fetchActorSystemConfig() {
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
