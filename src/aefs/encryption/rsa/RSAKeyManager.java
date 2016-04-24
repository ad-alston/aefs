package aefs.encryption.rsa;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashSet;
import java.util.Set;

import misc.io.StreamUtils;
import misc.logging.SimpleLogger;

/**
 * Class providing an interface for generation and use of RSA
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class RSAKeyManager {

	/**
	 * Returns a set of all keys found in a directory.
	 * @param directory path to directory from which to read files
	 * @return set of RSA keys
	 * @throws IOException if unable to read from directory
	 */
	public static Set<GenericRSAKey> readAllKeys(String directory) throws IOException {
		HashSet<GenericRSAKey> keys = new HashSet<GenericRSAKey>();
		
		for(File file : new File(directory).listFiles()){
			keys.add(readKeyFromFile(file));
		}
		
		return keys;
	}
	
	
	/**
	 * Reads an RSA key from a file.
	 * @param file file to read
	 * @return
	 * @throws IOException if unable to read from file
	 */
	public static GenericRSAKey readKeyFromFile(File file) throws IOException {
		FileInputStream fIn = new FileInputStream(file);
		try{
			DataInputStream in = new DataInputStream(fIn);
			byte[] mod = StreamUtils.readAdvertisedBytes(in);
			byte[] exp = StreamUtils.readAdvertisedBytes(in);
			
			return new GenericRSAKey(mod, exp);
		} finally{
			fIn.close();
		}
	}
	
	/**
	 * Generates an RSA keypair and saves them to files specified by the
	 * prefix. (Public key is written to {prefix}_public_rsa.key;
	 * private to {private}_private_rsa.key
	 * @param bits number of bits in which to generate the key
	 * @param prefix prefix to use to name the file
	 */
	public static void generateKeyPair(int bits, String prefix) throws
			NoSuchAlgorithmException, InvalidKeySpecException,
			IOException {
		
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(bits);
		
		KeyPair kp = kpg.genKeyPair();
	
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(),
		  RSAPublicKeySpec.class);
		RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
		  RSAPrivateKeySpec.class);

		// write to file
		saveToFile(prefix+"_public_rsa.key", pub.getModulus(),
		  pub.getPublicExponent());
		saveToFile(prefix+"_private_rsa.key", priv.getModulus(),
		  priv.getPrivateExponent());
	}
	
	/**
	 * Saves the components of an RSA key to a file.
	 * @param file to which to save the components
	 * @param mod mod of the key
	 * @param exp exponent
	 * @throws IOException if unable to write to file
	 */
	private static void saveToFile(String file, BigInteger mod, BigInteger exp)
			throws IOException {
		FileOutputStream fOut = new FileOutputStream(new File(file));
		try{
			DataOutputStream out = new DataOutputStream(fOut);
			byte[] modBytes = mod.toByteArray();
			byte[] expBytes = exp.toByteArray();
			
			StreamUtils.writeAdvertisedBytes(out, modBytes);
			StreamUtils.writeAdvertisedBytes(out, expBytes);
		} finally{
			fOut.close();
		}
	}
	
	/**
	 * Command-line interface for key generation.
	 * @param args
	 */
	public static void main(String[] args){
		SimpleLogger.setSource("RSAKeyManager");
		
		// check number of arguments
		if(args.length < 2){
			SimpleLogger.error("Usage: RSAKeyManager [num bits] [prefix]");
			System.exit(1);
		}
		
		try{
			generateKeyPair(Integer.parseInt(args[0]), args[1]);
			SimpleLogger.info("Keypair generated and written to file.");
		} catch(Exception e){
			SimpleLogger.error("Unable to generate keypair: "+e.getMessage());
		}
	}
}
