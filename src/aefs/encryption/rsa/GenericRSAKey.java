package aefs.encryption.rsa;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import misc.io.StreamUtils;

/**
 * Either a private or public RSAKey.
 */
public class GenericRSAKey {

	byte[] mod;
	byte[] exp;
	
	private PublicKey publicKeyForm;
	private PrivateKey privateKeyForm;
	
	public GenericRSAKey(){ }
	
	public GenericRSAKey(byte[] mod, byte[] exp){
		this.mod = mod;
		this.exp = exp;
	}
	
	/**
	 * Signs data under this key.
	 */
	public byte[] sign(byte[] toSign)
			throws NoSuchAlgorithmException, KeyException, SignatureException, InvalidKeySpecException{
		toPrivateKey();
		
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKeyForm);
        signature.update(toSign);
        
        return signature.sign();
	}
	
	/**
	 * Verifies a signature under this key.
	 */
	public boolean verify(byte[] data, byte[] sigBytes)
			throws NoSuchAlgorithmException, KeyException, SignatureException, InvalidKeySpecException {
		toPublicKey();
		
		Signature signature = Signature.getInstance("SHA256withRSA");		
        signature.initVerify(publicKeyForm);
        signature.update(data);
        return signature.verify(sigBytes);
	}

	/**
	 * Internally creates an RSA PublicKey object to be used for verifying signatures.
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	private void toPublicKey() throws InvalidKeySpecException, NoSuchAlgorithmException{
		if(publicKeyForm == null){
			RSAPublicKeySpec key = new RSAPublicKeySpec(new BigInteger(mod), new BigInteger(exp));
			publicKeyForm = KeyFactory.getInstance("RSA").generatePublic(key);
		}
	}
	
	/**
	 * Internally creates an RSA PrivateKey object to be used for signing.
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	private void toPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException{
		if(privateKeyForm == null){
			RSAPrivateKeySpec key = new RSAPrivateKeySpec(new BigInteger(mod), new BigInteger(exp));
			privateKeyForm = KeyFactory.getInstance("RSA").generatePrivate(key);
		}
	}
	
	/**
	 * Serializes this RSA key to a byte array.
	 */
	public byte[] serialize() throws IOException{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		
		StreamUtils.writeAdvertisedBytes(out, mod);
		StreamUtils.writeAdvertisedBytes(out, exp);
		
		return b.toByteArray();
	}
	
	/**
	 * Deserializes this RSA key from an input source.
	 */
	public void deserialize(InputStream source) throws IOException{
		DataInputStream in = new DataInputStream(source);
		this.mod = StreamUtils.readAdvertisedBytes(in);
		this.exp = StreamUtils.readAdvertisedBytes(in);
	}
	
	@Override
	public boolean equals(Object o){
		GenericRSAKey other = (GenericRSAKey) o;
		
		if(mod.length != other.mod.length || exp.length != other.exp.length)
			return false;
		
		for(int i = 0; i < mod.length; ++i){
			if(mod[i] != other.mod[i]) return false;
		}
		
		for(int i = 0; i < exp.length; ++i){
			if(exp[i] != other.exp[i]) return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode(){
		return new BigInteger(new BigInteger(mod).toString() + 
				new BigInteger(exp).toString()).intValue();
	}
	
}
