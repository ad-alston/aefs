package aefs.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * OutputStream which transparently decrypts using AES-256 and a passed
 * key and iv.
 */
public class RandomlyPaddedDecryptionStream extends InputStream {

	private CipherInputStream cIn;
	public byte[] discard;
	
	
	public RandomlyPaddedDecryptionStream(InputStream in, byte[] key, byte[] iv, boolean
				skipRandom) 
			throws NoSuchAlgorithmException, NoSuchPaddingException,
				InvalidAlgorithmParameterException, InvalidKeyException, IOException {
		
		IvParameterSpec ivObj = new IvParameterSpec(iv);
        SecretKeySpec skeyObj = new SecretKeySpec(key, "AES");
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeyObj, ivObj);
        
        cIn = new CipherInputStream(new UnclosedInputStream(in), cipher);
        
        // read and discard the first 16 random bytes
        if(skipRandom){
	        discard = new byte[16];
	        cIn.read(discard);
        }
	}
	
	public void discardBuffer() throws IOException{
		byte[] d = new byte[1];
		do{
			cIn.read(d);
		} while(d[0] == 0);
	}
	
	public void discardBlock() throws IOException{
		cIn.read(new byte[16]);
	}
	
	@Override
	public int read() throws IOException {
		return cIn.read();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return cIn.read(b);
	}
	
	@Override
	public void close(){
		try{
			cIn.close();
		} catch(Exception e){ }
	}
}
