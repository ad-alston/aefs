package aefs.protocols.requests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import misc.io.ArrayUtils;
import misc.logging.SimpleLogger;
import abe.schemes.waters08.Waters08ABEScheme;
import aefs.client.AEFSClient;
import aefs.encryption.EncryptedChannel;
import aefs.encryption.EncryptedChannel.ChannelRoutine;
import aefs.encryption.RandomlyPaddedDecryptionStream;
import aefs.protocols.ControlMessage;
import aefs.protocols.authorization.MasterSessionKey;
import aefs.protocols.authorization.MasterSessionToken;

public class ClientMasterSessionRequest extends ClientRequest {
	
	/**
	 * Constructs a master session request for a token authorizing
	 * a collection of attributes having a requested ttl.
	 * @param attributes attributes to be authorized
	 * @param ttl time to live for the token
	 */
	public ClientMasterSessionRequest(List<String> attributes, 
			long ttl){ 
		super("master-session");
		ControlMessage body = getContent();
		body.registerParameter("attributes",String.join(";", attributes));
		body.registerParameter("ttl", ""+ttl);
	}
	
	@Override
	protected void initializeFromArguments(String[] args) throws InvalidCommandException{
		if(args.length < 3){
			throw new InvalidCommandException("Cannot invoke master session from arguments.");
		}
	}
	
	@Override
	public void doRequest(AEFSClient fromClient, OutputStream out, InputStream in) throws IOException{	
			// Deserialize and decrypt master session key
			MasterSessionKey sessionKey = new MasterSessionKey(null, null);
			sessionKey.deserialize(in, fromClient.publicParams);
			
			Waters08ABEScheme s = new Waters08ABEScheme();
			try{
				s.setPublicParameters(fromClient.publicParams);
				byte[] o = s.decrypt(sessionKey.encrypted, fromClient.privateKey);
			
				// session key
				byte[] key = ArrayUtils.copyOfRange(o, 0, 32);
				byte[] iv = ArrayUtils.copyOfRange(o, 32, 48);
				
				fromClient.sessionKey = new MasterSessionKey(key, iv);
				// get token using randomly padded decryption stream
				
				final AEFSClient client = fromClient;
				ChannelRoutine r = new ChannelRoutine(){
					public void run(){
						try{
							MasterSessionToken sessionToken = new MasterSessionToken();
							sessionToken.deserialize(this.in, client.publicParams);
							client.sessionToken = sessionToken;
							
							// TODO: verify token (client)
						} catch(IOException e){
							SimpleLogger.error("Unable to receive session token.");
							client.sessionToken = null;
						}
					}
				};
				
				EncryptedChannel.withAES256Channel(r, fromClient.sessionKey.sessionKey, 
						fromClient.sessionKey.sessionIV, in, null, null, null);
				
			
			} catch(Exception e){ }
	}
	
}
