package aefs.nodes.handlers;

import java.util.HashMap;

import aefs.protocols.requests.ClientPutRequest;
import aefs.protocols.requests.ClientRequest;

/**
 * Responsible for returning correct handler given a supported
 * client request type.
 *
 */
public class MasterRequestHandlerFactory {
	
	private static HashMap<String, Class> requestClassMap = 
			new HashMap<String, Class>();
	
	private static Class sessionHandlerType = 
			registerRequestType("master-session", ClientMasterSessionRequestHandler.class);
	private static Class putHandlerType = 
			registerRequestType("put", ClientMasterPutRequestHandler.class);
	private static Class getHandlerType = 
			registerRequestType("get", ClientMasterGetRequestHandler.class);
	
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
	 * Given a client request, returns a valid handler.
	 * @param request
	 * @return
	 */
	public static MasterRequestHandler getHandler(ClientRequest request){
		Class c = requestClassMap.get(request.getContent().getValue("client-request-type"));
		if(c == null)  return null;
		
		try{
			return (MasterRequestHandler) c.newInstance();
		} catch(Exception e){ return null; }
	}
}
