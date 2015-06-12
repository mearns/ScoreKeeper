package scorekeeper.jmx;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import scala.Function0;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scala.runtime.BoxedUnit;
import scorekeeper.CircuitBrokenScheduledActor;
import scorekeeper.DeadActorException;
import scorekeeper.Metric;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.pattern.CircuitBreaker;
import akka.pattern.CircuitBreakerOpenException;

import com.timgroup.statsd.StatsDClient;
import scorekeeper.MetricsEnvironmentSetupMessage;

public class JMXPollingActor extends CircuitBrokenScheduledActor {
	private JMXServiceURL url; 
	private FiniteDuration duration;
	private Metric metric;
	private StatsDClient statsClient;
	private MBeanServerConnection mbcon;
	private ObjectName mbeanName;

	public JMXPollingActor(MetricsEnvironmentSetupMessage setupMessage, JMXServiceURL url, StatsDClient sdc, Metric metric) {
		super(setupMessage, metric);
		try {
			this.mbeanName = new ObjectName(metric.getObjectName());
		} catch (MalformedObjectNameException e) {
			throw new DeadActorException("Mbean " + metric.getObjectName() + " doesn't exist or is poortly constructed.", e);
		}
		this.url = url;
		this.statsClient = sdc;
		this.metric = metric;
		this.duration = Duration.create(metric.getFrequencyMs(), TimeUnit.MILLISECONDS);

	}
	
    private MBeanServerConnection makeMBConnection(JMXServiceURL url) throws IOException {
		return JMXConnectorFactory.connect(url, null).getMBeanServerConnection();
	}

	private void assureMBCon() throws IOException {
		if (mbcon == null){
			this.mbcon = makeMBConnection(url);
		}
	}

	protected void doTheWholeMetricsThing() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		assureMBCon();
		Number retVal = (Number) mbcon.getAttribute(mbeanName, metric.getAttribute());
		writeCounter(retVal.intValue(), metric.getMetricNames().get(0));
	}
	
	protected void writeCounter(int counter, String metricName) {
		statsClient.gauge(metricName, counter);

		System.out.print("x");
	}

	protected void scheduleNext() {
		getContext().system().scheduler().scheduleOnce(duration, getSelf(), "tick", getContext().dispatcher(), null);
	}

}
