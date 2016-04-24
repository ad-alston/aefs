package aefs.nodes.master;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import misc.logging.SimpleLogger;
import misc.random.SecureRandomBank;
import aefs.nodes.handlers.MasterRequestHandler;
import aefs.nodes.handlers.MasterRequestHandlerFactory;
import aefs.protocols.ControlMessage;
import aefs.protocols.requests.ClientRequest;
import aefs.protocols.requests.ClientRequestFactory;

/**
 * Implementation of the Runnable AEFSMasterWorker.  Given a client 
 * socket, responds to valid AEFS requests.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class AEFSMasterWorker implements Runnable {
	// slightly better parallelized generation of random numbers
	public static SecureRandomBank secureBank = new SecureRandomBank(10);
	
	public Socket client;
	
	public InputStream in;
	public OutputStream out;
	
	public AEFSMasterNode master;
	
	public AEFSMasterWorker(Socket client, AEFSMasterNode master){
		this.client = client;
		this.master = master;
		
		try{
			in = client.getInputStream();
			out = client.getOutputStream();
		} catch(Exception e){ }
	}
	
	@Override
	public void run(){
		String addr = client.getInetAddress().toString();
		try{
			SimpleLogger.info("Connection received from "+addr);
			ObjectInputStream objIn = new ObjectInputStream(in);
			
			// Receive and parse request
			ControlMessage message = (ControlMessage) objIn.readObject();
			ClientRequest request = ClientRequestFactory.requestFromMessage(message);
			
			// Handle the request
			MasterRequestHandler handler = MasterRequestHandlerFactory.getHandler(request);
			if(handler == null){
				SimpleLogger.error("Invalid request received from "+
						addr);
			} else{
				handler.handle(request,  this);
			}
		} catch(IOException | ClassNotFoundException e){
			SimpleLogger.error("Invalid request received from "+
					client.getInetAddress().toString()+".  Closing connection: "+
					e.getMessage());
		} finally{
			try{
				SimpleLogger.info("Closed connection to "+addr);
				client.close();
			} catch(IOException e){ }
		}
	}
}
