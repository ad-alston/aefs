package aefs.protocols.requests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import aefs.protocols.ControlMessage;

public class Test {
	
	public static void main(String[] args) throws Exception{
		ControlMessage message = new ControlMessage();
		message.registerParameter("client-request-type", "put");
		
		ClientRequest req = ClientRequestFactory.requestFromMessage(message);
	}

}
