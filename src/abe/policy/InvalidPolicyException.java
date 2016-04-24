package abe.policy;

/**
 * Exception to be thrown when an invalid access policy is encountered.
 */
public class InvalidPolicyException extends Exception{
	public InvalidPolicyException(String message){
		super(message);
	}
}
