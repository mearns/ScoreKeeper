package scorekeeper;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;

import scala.Function0;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scala.runtime.BoxedUnit;
import akka.actor.UntypedActor;
import akka.pattern.CircuitBreaker;
import akka.pattern.CircuitBreakerOpenException;

public abstract class CircuitBrokenScheduledActor extends UntypedActor {
	private CircuitBreaker breaker;
	private FiniteDuration duration;
	
	protected CircuitBrokenScheduledActor(Metric metric){
		this.duration = Duration.create(metric.getFrequencyMs(), TimeUnit.MILLISECONDS);
		this.breaker = new CircuitBreaker(getContext().dispatcher(), getContext().system().scheduler(),
				1, Duration.create(20, "s"), Duration.create(30, "s"))
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
			System.out.println("Circuit reset (closed) for " + getContext().self().path().name());
		}
	}


	private class OpenHandler implements Runnable {
		public void run(){
			System.out.println("Circuit broken (open) for " + getContext().self().path().name());
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
				ex.printStackTrace();
			}
		}
	}
	
	protected abstract void doTheWholeMetricsThing() throws Exception;
	
	protected void scheduleNext() {
		getContext().system().scheduler().scheduleOnce(duration, getSelf(), "tick", getContext().dispatcher(), null);
	}
}
