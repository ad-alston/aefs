package aefs.protocols.requests;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import misc.io.ArrayUtils;
import misc.io.StreamUtils;
import misc.logging.SimpleLogger;
import abe.ABEScheme.InvalidPublicParametersException;
import abe.Ciphertext;
import abe.lsss.LSSSNode;
import abe.lsss.ShareGeneratingMatrix;
import abe.policy.AccessPolicyInterpreter;
import abe.policy.AccessPolicyNode;
import abe.policy.NoSuchAttributeException;
import abe.schemes.waters08.Waters08ABEScheme;
import abe.schemes.waters08.Waters08MasterPublicParameters;
import aefs.client.AEFSClient;
import aefs.encryption.EncryptedChannel;
import aefs.encryption.EncryptedChannel.ChannelRoutine;
import aefs.encryption.FileEncryptionException;
import aefs.encryption.StreamedFileEncryptor;
import aefs.protocols.authorization.MasterSessionKey;

public class ClientPutRequest extends ClientRequest {
	
	private String destName;
	private String localPath;
	private String accessPolicy;
	
	public ClientPutRequest(){ 
		super("put");
	}
	
	public ClientPutRequest(String destName, String localPath, String accessPolicy){
		super("put");
		
		this.destName = destName;
		this.localPath = localPath;
		this.accessPolicy = accessPolicy;
	}
	
	@Override
	protected void initializeFromArguments(String[] args) throws InvalidCommandException{
		if(args.length < 4){
			throw new InvalidCommandException("Invalid put syntax.  "+
					"Usage: put <AEFS file name> <local path> <attribute access policy>");
		}
		
		this.destName = args[1];
		this.localPath = args[2];
		
		this.accessPolicy = "";
		for(int i = 3; i < args.length; ++i) {
			this.accessPolicy += args[i]+(i < args.length - 1 ? " " : "");
		}
		
		// check to see if file exists
		if(!new File(this.localPath).exists()){
			throw new InvalidCommandException("Specified file does not exist.");
		}
		
	}
	
	@Override
	public void doRequest(AEFSClient fromClient, OutputStream out, InputStream in){
		// send token 
		try{
			out.write(fromClient.sessionToken.serialize());
		} catch(IOException e){
			SimpleLogger.error("Unable to send session token to AEFS server.");
			return;
		}
		
		final OutputStream eOut = out;
		final InputStream eIn = in;
		ChannelRoutine r = new ChannelRoutine(){
			public void run(){
				try{
					// TODO: DH key exchange
					
					// receive expected pad
					this.in = EncryptedChannel.startAES256DecryptionChannel(eIn, 
							fromClient.sessionKey.sessionKey, 
							fromClient.sessionKey.sessionIV);
					
					byte[] pad = new byte[16];
					this.in.discardBlock(); this.in.read(pad); //random, pad
					
					// write the requested pad
					this.out.write(pad); this.out.buffer();
					
					DataOutputStream dout = new DataOutputStream(this.out);
					DataInputStream din = new DataInputStream(this.in);
					
					// send name and access policy
					StreamUtils.writeAdvertisedBytes(dout, destName.getBytes());
					StreamUtils.writeAdvertisedBytes(dout, accessPolicy.getBytes());
					
					this.out.buffer();
					
					this.in.discardBuffer(); // discard buffer
					// receive response
					if(!din.readBoolean()){
						String response = new String(StreamUtils.readAdvertisedBytes(din));
						SimpleLogger.error("Put request rejected by server.  Message from server: \n\t"+
								response);
					}
					
					// Choose key material
					SecureRandom rng = new SecureRandom();
					byte[] keyMaterial = MasterSessionKey.generateRandom(64, ((Waters08MasterPublicParameters)
							fromClient.publicParams).getMappingGroup(), rng);
					
					// Encrypt key under CP-ABE
					AccessPolicyNode p = AccessPolicyInterpreter.parsePolicy(accessPolicy);
					LSSSNode n = null;
					try{
						n = p.toLSSSNode(fromClient.publicParams);
					} catch(NoSuchAttributeException e){
						SimpleLogger.error("Access policy contains invalid attributes.");
						return;
					}
					
					ShareGeneratingMatrix m = n.getMatrix();
					
					// Encrypt the generated key
					Waters08ABEScheme scheme = new Waters08ABEScheme();
					try{
						scheme.setPublicParameters(fromClient.publicParams);
					} catch(InvalidPublicParametersException e){
						SimpleLogger.error("Unable to initialize ABE scheme given public parameters.");
						return;
					}
					
					// Send encrypted file key
					Ciphertext encryptedFileKey = scheme.encrypt(m, keyMaterial, rng);
					this.out.write(encryptedFileKey.serialize());
					
					StreamedFileEncryptor fEnc = null;
					try{
						// send encrypted file
						fEnc = new StreamedFileEncryptor(localPath, 4096, 
								ArrayUtils.copyOfRange(keyMaterial, 0, 32),
								ArrayUtils.copyOfRange(keyMaterial, 32, 48));
						while(fEnc.hasNext()){
							this.out.write(fEnc.encryptNextSector().getBytes());
						}
					} catch(FileEncryptionException e){
						SimpleLogger.error("Unable to send encrypted file.");
						return;
					} finally{
						fEnc.close();
					}
					
					// TODO integrity check
				} catch(IOException e){
					SimpleLogger.error("Unable to negotiate put request.");
				}
			}
		};
		
		EncryptedChannel.withAES256Channel(r, fromClient.sessionKey.sessionKey, 
				fromClient.sessionKey.sessionIV, null, out, new SecureRandom(), null);
	}
	
}
