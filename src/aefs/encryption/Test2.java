package aefs.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

import aefs.nodes.master.AEFSMasterWorker;

public class Test2 {
	public static void main(String[] args) throws Exception{
		SecureRandom r = AEFSMasterWorker.secureBank.nextRNG();
		
		byte[] key = new byte[32];
		byte[] iv = new byte[16];
		
		r.nextBytes(key);
		r.nextBytes(iv);
		
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		RandomlyPaddedEncryptionStream o = new RandomlyPaddedEncryptionStream(bo, key, iv, r);
		
		o.write("012345678901234501234567890123450123456789012345".getBytes());
		o.write("12345".getBytes());
		o.close();
		
		RandomlyPaddedDecryptionStream i = new RandomlyPaddedDecryptionStream(
				new ByteArrayInputStream(bo.toByteArray()), key, iv);
		
		byte[] content = new byte[16];
		i.read(content);
		
		System.out.println(new String(content));
		i.read(content);
		System.out.println(new String(content));
	}
}
