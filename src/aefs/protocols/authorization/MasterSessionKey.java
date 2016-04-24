package aefs.protocols.authorization;

import it.unisa.dia.gas.jpbc.Field;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;

import misc.io.ArrayUtils;
import abe.ABEScheme.InvalidPublicParametersException;
import abe.Ciphertext;
import abe.MasterPublicParameters;
import abe.lsss.LSSSAndNode;
import abe.lsss.LSSSLeafNode;
import abe.lsss.LSSSNode;
import abe.lsss.ShareGeneratingMatrix;
import abe.schemes.waters08.Waters08ABEScheme;
import abe.schemes.waters08.Waters08Ciphertext;
import abe.schemes.waters08.Waters08MasterPublicParameters;
import aefs.encryption.rsa.GenericRSAKey;
import aefs.nodes.master.AEFSMasterNode;

/**
 * Master session key to be used throughout an active AEFS session.
 * @author Aubrey Alston (ada2145)
 */
public class MasterSessionKey implements Serializable {

	public Ciphertext encrypted;
	
	public volatile byte[] sessionKey = null;
	public volatile byte[] sessionIV = null;
	
	public MasterSessionKey(byte[] key, byte[] iv){ 
		sessionKey = key;
		sessionIV = iv;
	}
	
	/**
	 * Generates a master session key and a master session token.
	 * @param publicParams system public parameters
	 * @param volatileAttributes system volatile attributes
	 * @param attributes attributes to authorize
	 * @param ttl time to live for the session key and token
	 * @param rng random number generator to use for key generation
	 * @param signingKey key to use to sign the generated server token
	 * @throws InvalidAttributeSetException
	 */
	public MasterSessionToken generate(MasterPublicParameters publicParams, 
			Set<String> volatileAttributes, String[] attributes, long ttl,
					SecureRandom rng, GenericRSAKey signingKey, GenericRSAKey verificationKey)
				throws InvalidAttributeSetException, SessionKeyGenerationException {
		Waters08MasterPublicParameters wparams = 
				(Waters08MasterPublicParameters) publicParams;
		
		// Verify that the list of attributes contains at least one volatile
		// attribute.
		boolean hasVolatile = false;
		
		for(String attribute : attributes){
			if(volatileAttributes.contains(attribute)){
				hasVolatile = true;
				break;
			}
		}
		if(! hasVolatile ){
			throw new InvalidAttributeSetException("No volatile attributes present.");
		}
		
		int[] attributeIDs = new int[attributes.length];
		int a = 0;
		
		// Reduce the TTL to the max TTL configured by the AEFS master
		ttl = ttl < AEFSMasterNode.MAX_TTL ? ttl : AEFSMasterNode.MAX_TTL;
		
		Waters08MasterPublicParameters p = (Waters08MasterPublicParameters) publicParams;
		byte[] keyMaterial = generateRandom(64, p.getMappingGroup(), rng);
		
		sessionKey = ArrayUtils.copyOfRange(keyMaterial, 0, 32);
		sessionIV = ArrayUtils.copyOfRange(keyMaterial, 32, 48);
		
		// Construct policy tree for client session key
		int identifier = publicParams.getAttribute(attributes[0]).getIdentifier();
		LSSSNode policy = new LSSSLeafNode(identifier);
		attributeIDs[a++] = identifier;
		for(int i = 1; i < attributes.length; i++){
			identifier = publicParams.getAttribute(attributes[i]).getIdentifier();
			attributeIDs[a++] = identifier;
			policy = new LSSSAndNode(policy, 
					new LSSSLeafNode(identifier));
		}
		ShareGeneratingMatrix m = policy.getMatrix();
		
		// Encrypt the generated key
		Waters08ABEScheme scheme = new Waters08ABEScheme();
		try{
			scheme.setPublicParameters(publicParams);
		} catch(InvalidPublicParametersException e){
			throw new InvalidAttributeSetException("Public parameters malformed.");
		}
		
		encrypted = scheme.encrypt(m, keyMaterial, rng);
		
		// Generate server token
		policy = new LSSSLeafNode(publicParams.getAttribute("server").getIdentifier());
		m = policy.getMatrix();
		byte[] serverKeyMaterial = generateRandom(64, p.getMappingGroup(), rng);
		
		Ciphertext encryptedServerKey = scheme.encrypt(m, serverKeyMaterial, rng);
		MasterSessionToken token = null;
		try{
			token = new MasterSessionToken(keyMaterial, serverKeyMaterial, encryptedServerKey,
					signingKey, verificationKey,  System.currentTimeMillis() + ttl,
					attributeIDs, rng);
		} catch(Exception e){
			throw new SessionKeyGenerationException("Unable to generate session token.");
		}
		
		return token;
	}
	
	/**
	 * Serializes this master session key to a byte array.
	 */
	public byte[] serialize() throws IOException {
		return encrypted.serialize();
	}
	
	/**
	 * Deserializes this master session key given a context and a source.
	 */
	public void deserialize(InputStream source, MasterPublicParameters context) 
			throws IOException {
		encrypted = new Waters08Ciphertext();
		encrypted.deserialize(source, context);
	}
	
	/**
	 * Generates up to numBytes of randomness within the context of the passed
	 * supported field.
	 * @param numBytes
	 * @param context
	 * @param rng
	 * @return
	 */
	public static byte[] generateRandom(int numBytes, Field context, Random rng){
		byte[] random = new byte[numBytes];
		rng.nextBytes(random);
		byte[] o = context.
				newElement(new BigInteger(random)).toBigInteger().
				toByteArray();
		return o;
	}
}
