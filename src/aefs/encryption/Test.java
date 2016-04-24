package aefs.encryption;

import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;

public class Test {
	public static void main(String[] args) throws Exception{
		
		SecureRandom r = new SecureRandom();
		byte[] key = new byte[32];
		byte[] iv = new byte[16];
		
		r.nextBytes(key);
		r.nextBytes(iv);
		
		File tempFile = File.createTempFile("prefix-", "-suffix");
		tempFile.deleteOnExit();
		
		FileOutputStream o = new FileOutputStream(tempFile);
		
		for(int i = 0; i < 1000000; i++){
			o.write('a');
		}
		
		o.close();
		
		StreamedFileEncryptor e = new StreamedFileEncryptor(tempFile.getAbsolutePath(), 4096,
				key, iv);
		
		int secnum = 0;
		long t = System.currentTimeMillis();
		while(e.hasNext()){
			EncryptedFileSector s = e.encryptNextSector();
			++secnum;
		}
		t = System.currentTimeMillis() - t;
		
		System.out.println(secnum+" sectors in "+t+"ms");
		e.close();
	}
}
