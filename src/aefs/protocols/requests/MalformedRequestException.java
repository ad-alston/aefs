package aefs.protocols.requests;

/**
 * Exception to be thrown when a malformed request is encountered.
 */
public class MalformedRequestException extends Exception {
	public MalformedRequestException(String msg){ super(msg); }
}
