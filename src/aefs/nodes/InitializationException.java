package aefs.nodes;

public class InitializationException extends Exception {
	public InitializationException(Exception e){ super(e); }
	public InitializationException(String msg){ super(msg); }
}
