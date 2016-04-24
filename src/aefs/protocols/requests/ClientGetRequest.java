package aefs.protocols.requests;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import misc.io.ArrayUtils;
import misc.io.StreamUtils;
import misc.logging.SimpleLogger;
import abe.Ciphertext;
import abe.schemes.waters08.Waters08ABEScheme;
import abe.schemes.waters08.Waters08Ciphertext;
import aefs.client.AEFSClient;
import aefs.encryption.EncryptedChannel;
import aefs.encryption.EncryptedChannel.ChannelRoutine;

public class ClientGetRequest extends ClientRequest {
	
	private String destName;
	
	public ClientGetRequest(){ 
		super("get");
	}
	
	public ClientGetRequest(String destName){
		super("get");
		
		this.destName = destName;
	}
	
	@Override
	protected void initializeFromArguments(String[] args) throws InvalidCommandException{
		if(args.length < 2){
			throw new InvalidCommandException("Invalid put syntax.  "+
					"Usage: put <AEFS file name>");
		}
		
		this.destName = args[1];
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
					// TODO: DH key exchange, tickets, integrity verification
					
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
					
					// send name
					StreamUtils.writeAdvertisedBytes(dout, destName.getBytes());
					this.out.buffer();
					
					this.in.discardBuffer();
					
					// receive encrypted key
					Ciphertext encryptedKey = new Waters08Ciphertext();
					encryptedKey.deserialize(this.in, fromClient.publicParams);
					
					FileOutputStream fout = new FileOutputStream(destName);
					
					byte[] keyMaterial = null;
					try{
						Waters08ABEScheme s = new Waters08ABEScheme();
						s.setPublicParameters(fromClient.publicParams);
						keyMaterial = s.decrypt(encryptedKey, fromClient.privateKey);
						
						Cipher decryptor = Cipher.getInstance("AES/CBC/NoPadding");
						
						decryptor.init(Cipher.DECRYPT_MODE, new SecretKeySpec(
								ArrayUtils.copyOfRange(keyMaterial, 0, 32), 0, key.length, "AES"), 
								new IvParameterSpec(ArrayUtils.copyOfRange(keyMaterial, 32, 48)));
						
						byte[] sector = new byte[4096];
						try{
							while(true){
								din.readFully(sector);
								byte[] decrypted = decryptor.doFinal(sector);
								byte[] data = ArrayUtils.copyOfRange(decrypted, 16, 4096);
								fout.write(data);
							}
						} catch(Exception e){ }
						
					} catch(Exception e){
						SimpleLogger.error("Unable to decrypt file.");
						e.printStackTrace();
					} finally{
						try{
							fout.close();
						} catch(Exception e){ }
					}
					
				
				} catch(IOException e){
					SimpleLogger.error("Unable to negotiate put request.");
				}
			}
		};
		
		EncryptedChannel.withAES256Channel(r, fromClient.sessionKey.sessionKey, 
				fromClient.sessionKey.sessionIV, null, out, new SecureRandom(), null);
	}
	
}
