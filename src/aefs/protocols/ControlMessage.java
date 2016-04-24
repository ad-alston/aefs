package aefs.protocols;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstraction for a request made by a client.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class ControlMessage implements Serializable {
	
	private Map<String, String> content;
	
	public ControlMessage(){
		this.content = new HashMap<String, String>();
	}
	
	/**
	 * Registers a key, value pair within the control message.
	 * @param parameter key
	 * @param value value
	 */
	public void registerParameter(String parameter, String value){
		content.put(parameter, value);
	}
	
	/**
	 * Returns the registered value for the passed parameter.
	 * @param parameter parameter of interest
	 */
	public String getValue(String parameter){
		return content.get(parameter);
	}
}
