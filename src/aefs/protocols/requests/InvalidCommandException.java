package aefs.protocols.requests;

public class InvalidCommandException extends Exception {
	
	public InvalidCommandException(Exception e){
		super(e);
	}
	
	public InvalidCommandException(String msg){
		super(msg);
	}

}
