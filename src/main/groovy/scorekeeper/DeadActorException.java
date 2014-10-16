package scorekeeper;

import javax.management.MalformedObjectNameException;

public class DeadActorException /* implements Stackable */ extends RuntimeException  {
	private static final long serialVersionUID = -5472101763156682297L;
	
	public DeadActorException(String string) {
		super(string);
	}

	public DeadActorException(String string, Throwable t) {
		super(string, t);
	}
}
