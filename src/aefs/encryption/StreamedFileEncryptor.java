package aefs.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class StreamedFileEncryptor{
	
	private int sectorSize;
	private FileInputStream fileStream;
	
	private BigInteger currentSector;
	
	private long fileSize;
	private long currentByte;
	
	private Cipher encryptor;
	
	public StreamedFileEncryptor(String path, int sectorSizeBytes, byte[] key, byte[] iv) 
				throws FileEncryptionException {
		try{
			if(sectorSizeBytes % 16 != 0)
				throw new Exception("Sector size must be a multiple of 16.");
			
			this.sectorSize = sectorSizeBytes;
			this.fileStream = new FileInputStream(new File(path));
			
			this.fileSize = new File(path).length();
			this.currentByte = 0;
			currentSector = BigInteger.ZERO;
			
			SecretKeySpec keySpec = new SecretKeySpec(key, 0, key.length, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
			encryptor = Cipher.getInstance("AES/CBC/NoPadding");
			
			encryptor.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		} catch(Exception e){
			throw new FileEncryptionException(e);
		}
	}
	
	public EncryptedFileSector encryptNextSector() throws FileEncryptionException{
		try{
			EncryptedFileSector sector = new EncryptedFileSector(sectorSize, currentSector,
					BigInteger.ZERO);
			
			byte[] sectorBytes = sector.getBytes();
			
			for(int i = 16; i < sectorBytes.length && currentByte < fileSize; i++){
				sectorBytes[i] = (byte) fileStream.read();
				
				++currentByte;
			}
			
			currentSector = currentSector.add(BigInteger.ONE);
			
			sector.setEncryptedBytes(encryptor.doFinal(sectorBytes));
			
			return sector;
		} catch(Exception e){
			throw new FileEncryptionException(e);
		}
	}
	
	public boolean hasNext(){
		return currentByte < fileSize;
	}
	
	public long getFileSize(){
		return fileSize;
	}
	
	public void close() throws IOException{
		if(fileStream != null){
			fileStream.close();
		}
	}
}
