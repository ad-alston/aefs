package aefs.nodes.handlers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Pattern;

import misc.io.ArrayUtils;
import misc.io.StreamUtils;
import misc.logging.SimpleLogger;
import abe.Ciphertext;
import abe.lsss.LSSSNode;
import abe.policy.AccessPolicyInterpreter;
import abe.policy.AccessPolicyNode;
import abe.policy.NoSuchAttributeException;
import abe.schemes.waters08.Waters08Ciphertext;
import aefs.encryption.EncryptedChannel;
import aefs.encryption.EncryptedChannel.ChannelRoutine;
import aefs.nodes.master.AEFSMasterWorker;
import aefs.protocols.authorization.MasterSessionToken;
import aefs.protocols.authorization.MasterSessionToken.TokenDetails;
import aefs.protocols.requests.ClientRequest;

public class ClientMasterPutRequestHandler extends MasterRequestHandler {
	
	public static final Pattern ACCEPTABLE_FNAME_PATTERN = 
			Pattern.compile("^[A-Za-z0-9\\.\\_]+$");
	
	@Override
	public void handle(ClientRequest request, AEFSMasterWorker worker) throws IOException {
		String clientAddr = worker.client.getInetAddress().toString();
		
		MasterSessionToken token = new MasterSessionToken();
		token.deserialize(worker.in, worker.master.publicParameters);
	
		// verify the token
		TokenDetails details = token.verifyFully(clientAddr, 
				worker.master.trustedTAKeys, worker.master.publicParameters, 
				worker.master.key);
		if(details == null){
			SimpleLogger.error("Token validation failed for put request from "+
					clientAddr+".  Closing connection.");
			return;
		}
		
		byte[] sessionKey = ArrayUtils.copyOfRange(details.keyMaterial, 0, 32);
		byte[] sessionIV = ArrayUtils.copyOfRange(details.keyMaterial, 32, 48);
		
		final SecureRandom rng = AEFSMasterWorker.secureBank.nextRNG();
		
		ChannelRoutine r = new ChannelRoutine(){
			FileOutputStream fOut = null;
			public void run(){
				try{
					// TODO: DH key exchange
					this.in = EncryptedChannel.startAES256DecryptionChannel(worker.in, 
							sessionKey, sessionIV);
					
					byte[] pad = new byte[16];
					rng.nextBytes(pad);
					this.out.write(pad);
					this.out.buffer();
					
					byte[] r = new byte[16];
					this.in.discardBlock(); 
					this.in.read(r);
					
					if(!Arrays.equals(pad, r)){
						SimpleLogger.error(clientAddr+" did not conform to random padding request.");
						return;
					}
					
					this.in.discardBuffer();
					DataInputStream din = new DataInputStream(this.in);
					DataOutputStream dout = new DataOutputStream(this.out);
					
					String name = new String(StreamUtils.readAdvertisedBytes(din));
					String policy = "("+new String(StreamUtils.readAdvertisedBytes(din))+")";
					
					SimpleLogger.info("Put request from "+clientAddr+":\n\tName:  "+name+"\n\tPolicy:  "
							+policy);
					
					// Verify name and policy
					if(! ACCEPTABLE_FNAME_PATTERN.matcher(name).matches()){
						dout.writeBoolean(false);
						StreamUtils.writeAdvertisedBytes(dout, "Invalid AEFS file name.".getBytes());
						SimpleLogger.info(clientAddr+" put rejected.  Invalid file name.");
						return;
					}
					
					try{
						AccessPolicyNode p = AccessPolicyInterpreter.parsePolicy(policy);
						LSSSNode n = p.toLSSSNode(worker.master.publicParameters);
					} catch(NoSuchAttributeException e){
						dout.writeBoolean(false);
						StreamUtils.writeAdvertisedBytes(dout, "Invalid access policy.".getBytes());
						SimpleLogger.info(clientAddr+" put rejected.  Invalid access policy.");
						return;
					}
					
					// TODO: verify name not taken
					
					dout.writeBoolean(true);
					this.out.buffer();
					
					// Read encrypted file
					this.in.discardBuffer();
					
					Ciphertext encryptedFileKey = new Waters08Ciphertext();
					encryptedFileKey.deserialize(this.in, worker.master.publicParameters);
					
					// make output dir if it doesn't exist
					if(! new File("aefs-store").exists()){
						new File("aefs-store").mkdir();
					}
					
					fOut = new FileOutputStream("aefs-store/"+name);
					DataOutputStream fdout = new DataOutputStream(fOut);
					
					StreamUtils.writeAdvertisedBytes(fdout, policy.getBytes());
					StreamUtils.writeAdvertisedBytes(fdout, encryptedFileKey.serialize());
					
					byte[] sector = new byte[4096];
					try{
						while(true){
							din.readFully(sector);
							fOut.write(sector);
						}
					} catch(Exception e){ }
					
					// TODO: hash and integrity verification
					
				} catch(Exception e){
					
				} finally{
					try{
						fOut.close();
					} catch(Exception e) { }
				}
			}
		};
		
		EncryptedChannel.withAES256Channel(r, sessionKey, 
				sessionIV, null, worker.out, rng, null);
	}
	
}
