package aefs.nodes.handlers;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.NoSuchPaddingException;

import misc.logging.SimpleLogger;
import aefs.encryption.EncryptedChannel;
import aefs.encryption.EncryptedChannel.ChannelRoutine;
import aefs.encryption.RandomlyPaddedEncryptionStream;
import aefs.nodes.master.AEFSMasterNode;
import aefs.nodes.master.AEFSMasterWorker;
import aefs.protocols.ControlMessage;
import aefs.protocols.authorization.InvalidAttributeSetException;
import aefs.protocols.authorization.MasterSessionKey;
import aefs.protocols.authorization.MasterSessionToken;
import aefs.protocols.authorization.SessionKeyGenerationException;
import aefs.protocols.requests.ClientRequest;

public class ClientMasterSessionRequestHandler extends MasterRequestHandler {
	
	@Override
	public void handle(ClientRequest request, AEFSMasterWorker worker) throws IOException {
		ControlMessage body = request.getContent();
		
		String[] attributes = body.getValue("attributes").split(";");
		Long ttl = Long.parseLong(body.getValue("ttl"));
		
		// Reduce the TTL to the max TTL configured by the AEFS master
		ttl = ttl < AEFSMasterNode.MAX_TTL ? ttl : AEFSMasterNode.MAX_TTL;
		
		// Generate master session key
		SecureRandom rng = worker.secureBank.nextRNG();
		MasterSessionKey sessionKey = new MasterSessionKey(null, null);
		MasterSessionToken token = null;
		
		try{
			token = sessionKey.generate(worker.master.publicParameters,
					 worker.master.volatileAttributes, attributes, ttl, rng,
					 worker.master.privateTAKey, worker.master.publicTAKey);
		} catch(InvalidAttributeSetException e){
			// TODO: error codes to client
			SimpleLogger.error("Invalid attribute authorization set requested by "+
					worker.client.getInetAddress().toString());
			return;
		} catch(SessionKeyGenerationException e){
			SimpleLogger.error("Unable to generate master session key for "+
					worker.client.getInetAddress().toString()+": "+e.getMessage());
			return;
		}
		
		worker.out.write(sessionKey.serialize());
		
		final byte[] tokenBytes = token.serialize();
		
		ChannelRoutine r = new ChannelRoutine(){
			public void run(){
				try{
					this.out.write(tokenBytes);
				} catch(IOException e){
					SimpleLogger.error("Unable to send master session token to "+
							worker.client.getInetAddress().toString()+": "+e.getMessage());
				}
			}
		};
		
		EncryptedChannel.withAES256Channel(r, sessionKey.sessionKey, 
				sessionKey.sessionIV, null, worker.out, rng, null);
	}
	
}
