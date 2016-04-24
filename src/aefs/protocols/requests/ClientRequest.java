package aefs.protocols.requests;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import aefs.client.AEFSClient;
import aefs.protocols.ControlMessage;
import aefs.protocols.authorization.MasterSessionKey;

/**
 * Abstraction for a client request.
 */
public abstract class ClientRequest{
	
	private ControlMessage content;

	public ClientRequest(){
		content = new ControlMessage();
	}
	
	public ClientRequest(String requestType){
		content = new ControlMessage();
		content.registerParameter("client-request-type", requestType);
	}
	
	public ControlMessage getContent(){
		return this.content;
	}
	
	protected void initializeFromArguments(String[] args) throws InvalidCommandException { }
	
	protected void setContent(ControlMessage content){
		this.content = content;
	}
	
	/**
	 * Performs the primary request action on the part of the client.
	 * @param fromClient client originating the request
	 * @param objOut object output stream
	 * @param objIn object input stream
	 * @param out output stream
	 * @param in input stream
	 */
	public abstract void doRequest(AEFSClient fromClient, OutputStream out, InputStream in) 
			throws IOException;
	
	/**
	 * Method to be called when a client issues a request.
	 * @param fromClient client issuing request
	 */
	public void initiateRequest(AEFSClient fromClient) throws MalformedRequestException{
		String address = fromClient.masterAddress;
		Integer port = fromClient.masterPort;
		
		// connect to the master
		Socket s = null;
		try{
			s = new Socket(address, port);
			OutputStream out = s.getOutputStream();
			InputStream in = s.getInputStream();
			
			// Send request body
			new ObjectOutputStream(out).writeObject(this.getContent());
			
			doRequest(fromClient, out, in);
		} catch(UnknownHostException e){
			throw new MalformedRequestException("Master hostname cannot be resolved.");
		} catch(IOException e){
			throw new MalformedRequestException("Cannot communicate with AEFS master.");
		} finally{
			try{
				s.close();
			} catch(Exception e){ }
		}
	}
	
}
