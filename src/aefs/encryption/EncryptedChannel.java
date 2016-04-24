package aefs.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import misc.logging.SimpleLogger;

/**
 * Class providing abstract support for two-way encrypted communication.
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class EncryptedChannel {
	
	public static RandomlyPaddedEncryptionStream startAES256EncryptionChannel(OutputStream out,
			byte[] key, byte[] iv, byte[] pad) throws IOException{
		try{
			return new RandomlyPaddedEncryptionStream(out, key, iv, pad);
		} catch(Exception e){
			SimpleLogger.error("Unable to establish randomly padded encryption stream.");
			return null;
		}
	}
	
	public static RandomlyPaddedDecryptionStream startAES256DecryptionChannel(InputStream in,
			byte[] key, byte[] iv) throws IOException{
		try{
			return new RandomlyPaddedDecryptionStream(in, key, iv, false);
		} catch(Exception e){
			e.printStackTrace();
			SimpleLogger.error("Unable to establish randomly padded decryption stream.");
			return null;
		}
	}
	
	public static void withAES256Channel(ChannelRoutine routine, byte[] key, byte[] iv,
			InputStream inputStream, OutputStream outputStream, SecureRandom rng,
				byte[] encPad){
		RandomlyPaddedEncryptionStream out = null;
		RandomlyPaddedDecryptionStream in = null;
		
		if(encPad == null && rng != null){
			encPad = new byte[16];
			rng.nextBytes(encPad);
		}
		
		try{
			if(outputStream != null){
				out = new RandomlyPaddedEncryptionStream(outputStream, key,
					iv, encPad);
			} if(inputStream != null){
				in = new RandomlyPaddedDecryptionStream(inputStream, key, iv, true);
			}
			routine.setStreams(in,  out, key, iv);
			routine.run();
		} catch(Exception e){
			
		} finally{
			try{
				in.close();
			}  catch(Exception e){ }
			try{
				out.close();
			} catch(Exception e){ }
		}
	}
	
	/**
	 * Routine to be performed using a two-way encrypted channel.
	 */
	public static class ChannelRoutine implements Runnable {
		
		public RandomlyPaddedEncryptionStream out;
		public RandomlyPaddedDecryptionStream in;
		
		public byte[] key;
		public byte[] iv;
		
		/**
		 * Sets the streams over which this routine will be performed.
		 * @param in input stream
		 * @param out output stream
		 */
		public void setStreams(RandomlyPaddedDecryptionStream in, 
				RandomlyPaddedEncryptionStream out, byte[] key, byte[] iv){
			this.in = in;
			this.out = out;
			this.key = key;
			this.iv = iv;
		}
		
		public void run(){ }
	}
}
