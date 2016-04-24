package aefs.nodes.handlers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Pattern;

import misc.io.ArrayUtils;
import misc.io.StreamUtils;
import misc.logging.SimpleLogger;
import aefs.encryption.EncryptedChannel;
import aefs.encryption.EncryptedChannel.ChannelRoutine;
import aefs.nodes.master.AEFSMasterWorker;
import aefs.protocols.authorization.MasterSessionToken;
import aefs.protocols.authorization.MasterSessionToken.TokenDetails;
import aefs.protocols.requests.ClientRequest;

public class ClientMasterGetRequestHandler extends MasterRequestHandler {
	
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
			public void run(){
				FileInputStream fIn = null;
				try{
					// TODO: DH key exchange, tickets, integrity verification
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
					
					SimpleLogger.info("Get request from "+clientAddr+":\n\tName:  "+name);
					
					// Verify name and policy
					if(! ACCEPTABLE_FNAME_PATTERN.matcher(name).matches()){
						dout.writeBoolean(false);
						StreamUtils.writeAdvertisedBytes(dout, "Invalid AEFS file name.".getBytes());
						SimpleLogger.info(clientAddr+" get rejected.  Invalid file name.");
						return;
					}
					
					fIn = new FileInputStream("aefs-store/"+name);
					DataInputStream dfin = new DataInputStream(fIn);
					
					String policy = new String(StreamUtils.readAdvertisedBytes(dfin));
					// write encrypted key
					this.out.write(StreamUtils.readAdvertisedBytes(dfin));
					
					// write file contents
					byte[] sector = new byte[4096];
					while(fIn.read(sector) == 4096){
						this.out.write(sector);
					}
				} catch(Exception e){
					
				} finally{
					try{
						fIn.close();
					} catch(Exception e){ }
				}
			}
		};
		
		EncryptedChannel.withAES256Channel(r, sessionKey, 
				sessionIV, null, worker.out, rng, null);
	}
	
}