package abe;

import it.unisa.dia.gas.jpbc.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import abe.lsss.ShareGeneratingMatrix;

/**
 * Abstraction of an attribute-based encryption scheme.  Provides an interface
 * for encryption, decryption, and key generation.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class ABEScheme {
	
	/**
	 * Encrypts variable-length plaintext according to the passed access structure.
	 * @param accessStructure access structure containing the access policy for decryption
	 * @param plaintext plaintext to encrypt
	 * @return ciphertext containing the encrypted plaintext
	 */
	public abstract Ciphertext encrypt(ShareGeneratingMatrix accessStructure,
			byte[] plaintext, Random rng);
	
	/**
	 * Decrypts a ciphertext of variable length.
	 * @return the plaintext for this ciphertext
	 * @throws InvalidCiphertextException if the auxiliary ciphertext is malformed
	 * @throws InvalidPrivateKeyException if the private key is malformed
	 */
	public abstract byte[] decrypt(Ciphertext ciphertext, PrivateKey key)
		throws InvalidCiphertextException, InvalidPrivateKeyException;
	
	/**
	 * Computes the auxiliary ciphertext for an access structure containing an access
	 * policy.
	 * @return AuxiliaryCiphertext object
	 */
	public abstract AuxiliaryCiphertext computeAuxiliaryCiphertext(
			ShareGeneratingMatrix accessStructure, Random rng);
	
	/**
	 * Given an auxiliary ciphertext containing the peripheral information to compute
	 * it, computes the multiplier used to hide messages.
	 * @param c auxiliary ciphertext
	 * @param sk private key
	 * @return multiplier element
	 * @throws InvalidCiphertextException if the auxiliary ciphertext is malformed
	 * @throws InvalidPrivateKeyException if the private key is malformed
	 */
	public abstract Element computeMultiplier(AuxiliaryCiphertext c, PrivateKey sk)
			throws InvalidCiphertextException, InvalidPrivateKeyException;
	
	/**
	 * Sets the public parameters for the scheme.
	 * @param parameters parameters to set
	 */
	public abstract void setPublicParameters(MasterPublicParameters parameters)
			throws InvalidPublicParametersException;
	
	/**
	 * Sets the master secret key for the scheme.  This allows for 
	 * @param key master secret key to use
	 */
	public abstract void setMasterSecretKey(MasterSecretKey key)
			throws InvalidMasterSecretKeyException;
	
	/**
	 * Returns the public parameters set for this scheme.
	 */
	public abstract MasterPublicParameters getPublicParameters();
	
	/**
	 * Returns the master secret key set for this scheme (or none if none has been
	 * set).
	 */
	public abstract MasterSecretKey getMasterSecretKey();
	
	/**
	 * Generates a private key for a party holding the passed list of attributes.
	 * @param attributes attributes held by the party
	 * @return PrivateKey 
	 */
	public PrivateKey generatePrivateKey(List<String> attributes) 
			throws InvalidAttributeException{
		if(getPublicParameters() == null)
			throw new IllegalStateException("Public parameters not set for this scheme.");
		
		ArrayList<EntityAttribute> attList = new ArrayList<EntityAttribute>();
		for(String attribute: attributes){
			EntityAttribute a = getPublicParameters().getAttribute(attribute);
			if(a == null){
				throw new InvalidAttributeException("Attribute "+attribute+" does not exist.");
			} else{
				attList.add(a);
			}
		}
		
		return generatePrivateKeyInternal(attList);
	}
	
	/**
	 * Generates a private key for a party holding the passed list of attributes.
	 * @param attributes attributes held by the party
	 * @return PrivateKey 
	 */
	public PrivateKey generatePrivateKeyFromIDs(List<Integer> attributes) 
			throws InvalidAttributeException{
		if(getPublicParameters() == null)
			throw new IllegalStateException("Public parameters not set for this scheme.");
		
		ArrayList<EntityAttribute> attList = new ArrayList<EntityAttribute>();
		for(Integer attribute: attributes){
			EntityAttribute a = getPublicParameters().getAttribute(attribute);
			if(a == null){
				throw new InvalidAttributeException(
						"Attribute id "+attribute+" does not exist.");
			} else{
				attList.add(a);
			}
		}
		
		return generatePrivateKeyInternal(attList);
	}
	
	/**
	 * Generates a private key for a party holding the passed list of attributes.
	 * @param attributes attributes held by the party
	 * @return PrivateKey 
	 */
	public abstract PrivateKey generatePrivateKeyInternal(List<? extends EntityAttribute>
			attributes) throws InvalidAttributeException;
	
	/**
	 * Exception to be thrown when an ABE scheme is provided with invalid public
	 * parameters.
	 */
	public static class InvalidPublicParametersException extends Exception{ 
		public InvalidPublicParametersException(String msg){
			super(msg);
		}
	}

	/**
	 * Exception to be thrown when an ABE scheme is provided with an invalid secret
	 * key.
	 */
	public static class InvalidMasterSecretKeyException extends Exception{ 
		public InvalidMasterSecretKeyException(String msg){
			super(msg);
		}
	}
	
	/**
	 * Exception to be thrown when a decryption routine is provided an invalid
	 * private key.
	 */
	public static class InvalidPrivateKeyException extends Exception{ 
		public InvalidPrivateKeyException(String msg){
			super(msg);
		}
	}
	
	/**
	 * Exception to be thrown when an operation is requested on an invalid attribute.
	 */
	public static class InvalidAttributeException extends Exception{ 
		public InvalidAttributeException(String msg){
			super(msg);
		}
	}
	
	/**
	 * Exception to be thrown when the ciphertext provided to a decryption
	 * routine is invalid.
	 */
	public static class InvalidCiphertextException extends Exception{ 
		public InvalidCiphertextException(String msg){
			super(msg);
		}
	}
}
