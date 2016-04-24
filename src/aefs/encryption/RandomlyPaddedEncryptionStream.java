package aefs.encryption;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * OutputStream which transparently encrypts using AES-256 and a passed
 * key and iv.
 */
public class RandomlyPaddedEncryptionStream extends OutputStream {
	
	private CipherOutputStream cOut;
	
	public RandomlyPaddedEncryptionStream(OutputStream out, byte[] key, byte[] iv,
			byte[] randPad) throws NoSuchAlgorithmException, NoSuchPaddingException,
				InvalidAlgorithmParameterException, InvalidKeyException, IOException {
		
		IvParameterSpec ivObj = new IvParameterSpec(iv);
        SecretKeySpec skeyObj = new SecretKeySpec(key, "AES");
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		try{
			cipher.init(Cipher.ENCRYPT_MODE, skeyObj, ivObj);
		} catch(Exception e){ e.printStackTrace(); }
		
		if(randPad != null){
	        cOut = new CipherOutputStream(new UnclosedOutputStream(out), cipher);
	        cOut.write(randPad);
		}
	}
	
	public void buffer() throws IOException {
		cOut.write(new byte[31]);
		cOut.write(new byte[]{1});
		cOut.flush();
	}
	
	@Override
	public void write(int b) throws IOException{
		cOut.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException{
		cOut.write(b);
	}
	
	public void flush(){
		try{
			cOut.flush();
		} catch(IOException e){ }
	}
	
	@Override
	public void close(){
		try{
			cOut.close();
		} catch(Exception e){ }
	}
}
