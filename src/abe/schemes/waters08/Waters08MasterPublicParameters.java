package abe.schemes.waters08;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pbc.curve.PBCTypeACurveGenerator;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import misc.io.StreamUtils;
import misc.numbers.BigIntegerUtilities;
import abe.ABEScheme.InvalidPublicParametersException;
import abe.EntityAttribute;
import abe.MasterPublicParameters;
import abe.MasterSecretKey;

/**
 * Master public parameters object for the default implementation of the Waters08
 * https://eprint.iacr.org/2008/290.pdf (section 3)
 * attribute-based encryption scheme within aefs.
 * 
 * This default implementation relies on random elliptic curve groups of order
 * r (where r is a Solinas prime), defined on top of a randomly chosen finite field
 * having prime modulo q such that r + 1 = q * (small value); the implementation
 * is also achieved using the JPBC Java port of the C-based PBC library.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class Waters08MasterPublicParameters extends MasterPublicParameters{

	private int systemBitLength; // bit-length of the system
	
	private String pairingParameters; // JPBC-formatted string containing pairing parameters
	
	private Pairing pairing; // JPBC entity containing core mechanisms to perform
							 // core group operations as well as pairings.
	
	private BigInteger groupOrder; // p (from the Waters08 scheme)
	private BigInteger safeModulus; // modulus having a large prime factor
	
	private Element generator; // g (from the Waters08 scheme)
	private Element pairing_alpha; // e(g,g)^{alpha} (from the Waters08 scheme)
	private Element generator_a; // g^{a} (from the Waters08 scheme)
	
	private Map<String, Waters08EntityAttribute> stringToAttributeMap;
	private Map<Integer, Waters08EntityAttribute> idToAttributeMap;
	
	private volatile int nextUnusedIdentifier = 0; // Next unused attribute identifier
												   // declared volatile and to be used within
												   // synchronized blocks to prevent race
												   // conditions when registering new
												   // attributes.
	
	public Waters08MasterPublicParameters(){
		stringToAttributeMap = new HashMap<String, Waters08EntityAttribute>();
		idToAttributeMap = new HashMap<Integer, Waters08EntityAttribute>();
	}
	
	public int getOperableBitLength(){
		return systemBitLength;
	}
	
	/**
	 * Returns g^{a} public parameter as described in the Waters08 paper.
	 */
	public Element getGPowA(){
		return generator_a;
	}
	
	/**
	 * Returns the e(g,g)^{alpha} public parameter as described in the Waters08 paper.
	 */
	public Element getPairingAlpha(){
		return pairing_alpha;
	}
	
	/**
	 * Returns the generator (g) public parameter as described in the Waters08
	 * paper.
	 */
	public Element getGenerator(){
		return generator;
	}
	
	/**
	 * Returns the pairing specified under these public parameters.
	 */
	public Pairing getPairing(){
		return pairing;
	}
	
	/**
	 * Returns a Field (JPBC) object encapsulating the group being used in this scheme.
	 */
	public Field getGroup(){
		return pairing.getG1();
	}
	
	/**
	 * Returns a Field (JPBC) object encapsulating the target group of the symmetric
	 * bilinear mapping for this scheme.
	 */
	public Field getMappingGroup(){
		return pairing.getGT();
	}
	
	@Override
	public MasterSecretKey initializeRandomly(int bits){
		
		this.systemBitLength = bits;
		
		// Generate random curve
		TypeACurveGenerator curveGenerator = new TypeACurveGenerator(bits, bits);
		PairingParameters pairingParameters = curveGenerator.generate(); 
		
		this.safeModulus = pairingParameters.getBigInteger("q").add(BigInteger.valueOf(2));
		BigInteger r = pairingParameters.getBigInteger("r");
		
		this.pairingParameters = pairingParameters.toString();
		
		SecureRandom secureRNG = new SecureRandom();
		this.pairing = PairingFactory.getPairing(pairingParameters);
		
		// Retrieve group order
		this.groupOrder = pairingParameters.getBigInteger("r");
		
		// Pick generator for the group
		this.generator = pairing.getG1().newRandomElement();
		
		// Generate parameters alpha, a
		BigInteger alpha = BigIntegerUtilities.random(secureRNG, this.groupOrder);
		BigInteger a = BigIntegerUtilities.random(secureRNG, this.groupOrder);
		
		// Compute e(g,g)^{alpha} and g^{a}
		this.pairing_alpha = pairing.pairing(generator, generator).pow(alpha);
		this.generator_a = generator.duplicate().pow(a);
		
		// Return MSK (g^{alpha})
		return new Waters08MasterSecretKey(generator.duplicate().pow(alpha));
	}
	
	/**
	 * Returns a modulus having large prime factors.
	 */
	public BigInteger getSafeModulus(){
		return this.safeModulus;
	}
	
	@Override
	public void initializeFromFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream(path);
		DataInputStream in = new DataInputStream(stream);
		
		byte[] data = new byte[(int) new File(path).length()];
		in.readFully(data);
		
		deserialize(data);
	}
	
	@Override
	public void writeToFile(String path) throws IOException {
		FileOutputStream stream = new FileOutputStream(path);
		DataOutputStream out = new DataOutputStream(stream);
		
		out.write(serialize());
	}
	
	@Override
	public synchronized void registerNewAttribute(String name){
		if(groupOrder == null){
			throw new IllegalStateException("Waters08MasterPublicParameters not initialized.");
		}
		
		if(stringToAttributeMap.get(name) != null){
			throw new IllegalStateException(name+" attribute already exists.");
		}
		
		Element h = pairing.getG1().newRandomElement();
		
		// Add and map the new attribute
		Waters08EntityAttribute attribute = new Waters08EntityAttribute(name,
				nextUnusedIdentifier, h);
		stringToAttributeMap.put(name, attribute);
		idToAttributeMap.put(nextUnusedIdentifier, attribute);
		
		// Increment the unused identifier counter
		nextUnusedIdentifier++;
	}
	
	@Override
	public Waters08EntityAttribute getAttribute(String attribute){
		return stringToAttributeMap.get(attribute);
	}
	
	@Override
	public Waters08EntityAttribute getAttribute(int identifier){
		return idToAttributeMap.get(identifier);
	}
	
	@Override
	public Iterator<Waters08EntityAttribute> getAttributeIterator(){
		return stringToAttributeMap.values().iterator();
	}
	
	@Override
	public byte[] serialize() throws IOException{
		// Serialize the object
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);

		StreamUtils.writeAdvertisedBytes(out, pairingParameters.getBytes());
		StreamUtils.writeAdvertisedBytes(out, generator.toCanonicalRepresentation());
		StreamUtils.writeAdvertisedBytes(out, pairing_alpha.toCanonicalRepresentation());
		StreamUtils.writeAdvertisedBytes(out, generator_a.toCanonicalRepresentation());
		StreamUtils.writeAdvertisedBytes(out, safeModulus.toByteArray());
		
		// Serialize and write each attribute
		Iterator<Waters08EntityAttribute> it = getAttributeIterator();
		while(it.hasNext()){
			Waters08EntityAttribute attribute = it.next();
			StreamUtils.writeAdvertisedBytes(out, attribute.serialize());
		}
		
		return stream.toByteArray();
	}
	
	@Override
	public void deserialize(byte[] serialized) throws IOException{
		ByteArrayInputStream stream = new ByteArrayInputStream(serialized);
		DataInputStream in = new DataInputStream(stream);
		
		this.pairingParameters = new String(StreamUtils.readAdvertisedBytes(in));
		
		
		// Write temporary file and use the pairing parameters to restore the pairing
		File propertiesFile = File.createTempFile("params_properties", ".tmp");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile));
 	    writer.write(this.pairingParameters);
 	    writer.close();
 	    
 	    // Restore the remainder of the parameters
 	    this.pairing = PairingFactory.getPairing(propertiesFile.getAbsolutePath());
 	    this.generator = pairing.getG1().newElementFromBytes(
 	    		StreamUtils.readAdvertisedBytes(in));
 	    this.pairing_alpha = pairing.getGT().newElementFromBytes(
 	    		StreamUtils.readAdvertisedBytes(in));
 	    this.generator_a = pairing.getG1().newElementFromBytes(
 	    		StreamUtils.readAdvertisedBytes(in));
 	    this.safeModulus = new BigInteger(StreamUtils.readAdvertisedBytes(in));
 	    this.groupOrder = pairing.getG1().getOrder();
 	    
 	    while(in.available() > 0){ // Deserialize and add each attribute
 	    	Waters08EntityAttribute a = new Waters08EntityAttribute("", -1, null);
 	    	try{
 	    		a.deserialize(this, StreamUtils.readAdvertisedBytes(in));
 	    	} catch(InvalidPublicParametersException e){ }
 	    	
 	    	stringToAttributeMap.put(a.getAttributeName(), a);
 	    	idToAttributeMap.put(a.getIdentifier(), a);
 	    	
 	    	if(a.getIdentifier() >= nextUnusedIdentifier){
 	    		nextUnusedIdentifier = a.getIdentifier() + 1;
 	    	}
 	    }
	}
	
	@Override
	public String toString(){
		// Prints the public parameters for debugging purposes
		StringBuilder b = new StringBuilder();
		b.append("group order: "+groupOrder.toString()+"\n");
		b.append("generator: "+generator.toString()+"\n");
		b.append("pairing_alpha: "+pairing_alpha.toString()+"\n");
		b.append("generator_a: "+generator_a.toString()+"\n");
		b.append("attributes: \n");
		
		Iterator<? extends EntityAttribute> it = getAttributeIterator();
		while(it.hasNext()){
			EntityAttribute attribute = it.next();
			b.append("   "+attribute.toString()+"\n");
		}
		
		return b.toString();
	}

}
