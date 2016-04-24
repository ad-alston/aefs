package aefs.encryption;

import java.math.BigInteger;

import javax.crypto.Cipher;

import misc.io.ArrayUtils;

public class EncryptedFileSector {
	
	private byte content[];
	
	public EncryptedFileSector(int sectorSizeBytes, BigInteger sectorNumber, 
			BigInteger sectorRevision){
		
		this.content = new byte[sectorSizeBytes];
		
		byte[] sectorNumberBytes = sectorNumber.toByteArray();
		byte[] revisionBytes = sectorRevision.toByteArray();
		
		ArrayUtils.copySubarray(sectorNumberBytes, this.content, 
				sectorNumberBytes.length - 6, sectorNumberBytes.length, 0);
		ArrayUtils.copySubarray(revisionBytes, this.content,
				revisionBytes.length - 10, revisionBytes.length, 0);
	}
	
	public byte[] getBytes(){
		return this.content;
	}
	
	public void setEncryptedBytes(byte[] encrypted){
		this.content = encrypted;
	}
}
