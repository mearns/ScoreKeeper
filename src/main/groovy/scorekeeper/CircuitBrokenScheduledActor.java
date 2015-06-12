package scorekeeper;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.UntypedActor;
import akka.pattern.CircuitBreaker;
import akka.pattern.CircuitBreakerOpenException;

public abstract class CircuitBrokenScheduledActor extends UntypedActor {
	private CircuitBreaker breaker;
	private FiniteDuration duration;
	private MetricsEnvironmentSetupMessage setupMessage;
	private static Logger log = LoggerFactory.getLogger(CircuitBrokenScheduledActor.class);

	protected CircuitBrokenScheduledActor(MetricsEnvironmentSetupMessage setupMessage, Metric metric){
		this.duration = Duration.create(metric.getFrequencyMs(), TimeUnit.MILLISECONDS);
		this.breaker = makeNewCB(duration.toMillis()*2);
		this.setupMessage = setupMessage;
	}

    private CircuitBreaker makeNewCB(long millis) {
        return new CircuitBreaker(getContext().dispatcher(), getContext().system().scheduler(),
                1, Duration.create(millis, "ms"), Duration.create(setupMessage.getTimeBetweenRetriesS(), "s"))
            .onOpen(makeOpenHandler())
            .onClose(makeCloseHandler());
    }

    protected Runnable makeOpenHandler() {
		return new OpenHandler();
	}

	protected Runnable makeCloseHandler() {
		return new CloseHandler();
	}

	private class CloseHandler implements Runnable {
		public void run(){
			log.info("Circuit reset (closed) for " + getContext().self().path().name());
		}
	}


	protected class OpenHandler implements Runnable {
		public void run(){
            long currentDur = duration.toMillis();
            long newDur = Math.min(currentDur * 2, setupMessage.getMaxQueryTimeoutS() * 1000);
			log.warn("Circuit broken (open) for " + getContext().self().path().name() + "; delaying to " + currentDur);

			duration = Duration.create(newDur, TimeUnit.MILLISECONDS);
            breaker = makeNewCB(newDur);
		}
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		if ("start".equals(arg0)){
			getSelf().tell("tick", getSelf());
		} else if ("tick".equals(arg0)){
			collectMetricsInsideBreaker();
			
			scheduleNext();
		} else {
			unhandled(arg0);
		}
	}

	private void collectMetricsInsideBreaker() throws Exception {
		try {
			breaker.callWithSyncCircuitBreaker(new Callable<Boolean>(){
				public Boolean call() throws Exception {
					doTheWholeMetricsThing();
					return true;
				}
			});
		} catch (Exception ex){
			if (ex instanceof DeadActorException || ex instanceof JMException){
				throw ex;
			} else if (ex instanceof CircuitBreakerOpenException){
			} else {
				log.error("Exception while collection metrice", ex);
			}
		}
	}
	
	protected abstract void doTheWholeMetricsThing() throws Exception;
	
	protected void scheduleNext() {
		getContext().system().scheduler().scheduleOnce(duration, getSelf(), "tick", getContext().dispatcher(), null);
	}
}
