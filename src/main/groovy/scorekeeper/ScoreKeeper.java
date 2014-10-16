package scorekeeper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scorekeeper.configuration.MonitoringParser;
import scorekeeper.configuration.SystemConfigParser;

public class ScoreKeeper {
	private ActorSystem system;
	private ActorRef headRef;
	private static MonitoringParser mp = new MonitoringParser();
	private static SystemConfigParser scp = new SystemConfigParser();

	public static void main(String[] args) throws InterruptedException {	
		MetricsEnvironmentSetupMessage sm = scp.newStartupMessage();
		mp.initializeMetrics(sm, getMonitoringConfig(args));
		
		ScoreKeeper sk = new ScoreKeeper(sm);
		sk.keepScore();
		while (true){
			Thread.sleep(100);
		}
	}
	
	private static Collection<Config> getMonitoringConfig(String[] args) {
		ArrayList<Config> al = new ArrayList<Config>();
		for (String arg: args){
			File f = new File(arg);
			if (f.exists() && f.isFile()){
				al.add(ConfigFactory.parseFile(f));
			} else {
				System.out.println(f.getName() + " specified but not present");
			}
		}
		return al;
	}
	
	ScoreKeeper(MetricsEnvironmentSetupMessage sm){
		Config c = fetchActorSystemConfig();
		system = ActorSystem.create("ScoreKeeper", c);
        headRef = system.actorOf(Props.create(HeadScorekeepingActor.class));
        headRef.tell(sm, ActorRef.noSender());
	}

	protected Config fetchActorSystemConfig() {
		Config c = ConfigFactory.parseString(
			"sql-actor-dispatch {\n" +
				"type = Dispatcher\n" +
				 "executor = \"thread-pool-executor\"\n" +
				 "thread-pool-executor {\n" +
					 "core-pool-size-min = 2\n" +
					 "core-pool-size-factor = 2.0\n" +
					 "core-pool-size-max = 10\n" +
				 "}\n" +
				 "throughput = 1\n" +
			"}");
		return c;
	}
	
	private void keepScore() {
		headRef.tell(new StartGameMessage(), ActorRef.noSender());
	}
}
