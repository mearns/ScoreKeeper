package scorekeeper;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.UntypedActor;
import akka.japi.Function;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import scala.concurrent.duration.Duration;
import scorekeeper.jmx.JMXActorFactory;
import scorekeeper.metrics.DatasourceMetrics;
import scorekeeper.metrics.JMXMetrics;
import scorekeeper.site24x7.Site24x7ActorFactory;
import scorekeeper.sql.SQLActorFactory;

import java.sql.SQLException;

public class HeadScorekeepingActor extends UntypedActor {
    private MetricsEnvironmentSetupMessage startupMessage;

    private static SupervisorStrategy strategy =
            new OneForOneStrategy(10, Duration.create("1 minute"),
                    new Function<Throwable, Directive>() {
                        @Override
                        public Directive apply(Throwable t) {
//					if (t instanceof ArithmeticException) {
//						return SupervisorStrategy.resume();
//					} else if (t instanceof NullPointerException) {
//						return SupervisorStrategy.restart();
                            if (t instanceof DeadActorException) {
                                return SupervisorStrategy.stop();
                            } else {
                                return SupervisorStrategy.restart();
                            }
                        }
                    }, true
            );

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void onReceive(Object arg0) throws Exception {
        if (arg0 instanceof MetricsEnvironmentSetupMessage) {
            makeKids((MetricsEnvironmentSetupMessage) arg0);
        } else if (arg0 instanceof StartGameMessage) {
            startKids((StartGameMessage) arg0);
        } else {
            unhandled(arg0);
        }
    }

    protected void makeKids(MetricsEnvironmentSetupMessage smsg) throws SQLException {
        this.startupMessage = smsg;

        StatsDClient hub = new NonBlockingStatsDClient(smsg.getAppName() + "." + smsg.getEnvName(), smsg.getHostName(), smsg.getStatsDPort());

        for (DatasourceMetrics dsm : smsg.getDatasourceMetrics()) {
            new SQLActorFactory(smsg, hub, dsm.getDataSource(), getContext())
                    .makeMetricsForDatasource(dsm.getMetrics());
        }

        for (JMXMetrics jmxms : smsg.getJMXMetrics()) {
            new JMXActorFactory(smsg, hub, jmxms.connectToMBeanServer(), getContext())
                    .makeMetricsForDatasource(jmxms.getMetrics());
        }

        for (Metric site24x7Metrics : smsg.getSite24x7Metrics()) {
            new Site24x7ActorFactory(smsg, hub, getContext())
                    .makeMetricsForDatasource(site24x7Metrics);
        }
    }

    protected void startKids(StartGameMessage arg0) {
        for (ActorRef child : getContext().getChildren()) {
            child.tell("start", getSelf());
        }
    }
}
