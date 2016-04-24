package aefs.protocols.requests;

import java.util.HashMap;

import aefs.protocols.ControlMessage;

public class ClientRequestFactory {
	
	private static HashMap<String, Class> requestClassMap = 
			new HashMap<String, Class>();
	
	private static Class putType = 
			registerRequestType("put", ClientPutRequest.class);
	private static Class getType = 
			registerRequestType("get", ClientGetRequest.class);
	private static Class masterSessionType = 
			registerRequestType("master-session", ClientPutRequest.class);
	
	/**
	 * Internally registers a client request type.
	 * @param requestType request-type value that should map to an instance
	 * of the passed class
	 * @param typeClass type of the client request type
	 */
	private static Class registerRequestType(String requestType, Class typeClass){
		
		requestClassMap.put(requestType, typeClass);
		
		return typeClass;
		
	}
	
	/**
	 * Takes a request string as taken from the client, parses it, 
	 * and returns a ClientRequest object if the request is valid and
	 * properly formatted.
	 * @param requestString string as taken from user
	 * @throws InvalidcommandException if the command string is invalid
	 */
	public static ClientRequest requestFromCommandString(String requestString)
			throws InvalidCommandException {
		String[] args = requestString.split(" ");
		
		ClientRequest request = requestFromCommand(args[0]);
		if(request != null){
			request.initializeFromArguments(args);
			
			return request;
		} else{
			throw new InvalidCommandException("Command specified by '"+args[0]+"' "+
					"is invalid.");
		}
	}
	
	/**
	 * Given a control message, returns a ClientRequest object defined by
	 * the control message body.
	 * @param body control message which holds the request content
	 * @return ClientRequest object if the body holds a legitimate client request, 
	 * else null
	 */
	public static ClientRequest requestFromMessage(ControlMessage body){
		String requestType = body.getValue("client-request-type");
		
		ClientRequest request = requestFromCommand(requestType);
		
		if(request != null) request.setContent(body);
		
		return request;
	}
	
	/**
	 * Given a control message, returns a ClientRequest object defined by
	 * the control message body.
	 * @param body control message which holds the request content
	 * @return ClientRequest object if the body holds a legitimate client request, 
	 * else null
	 */
	private static ClientRequest requestFromCommand(String command){
		Class requestClass = requestClassMap.get(command);
		
		try{
			if(requestClass != null){
				ClientRequest request = (ClientRequest) requestClass.newInstance();
				
				return request;
			}
		} catch(IllegalAccessException | InstantiationException e){ }
		
		return null;
	}
	
}
