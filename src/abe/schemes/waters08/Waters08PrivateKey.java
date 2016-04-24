package abe.schemes.waters08;

import it.unisa.dia.gas.jpbc.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import misc.io.StreamUtils;
import abe.ABEScheme.InvalidPublicParametersException;
import abe.MasterPublicParameters;
import abe.PrivateKey;

/**
 * Private key object for the default implementation of the Waters08
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
public class Waters08PrivateKey extends PrivateKey {
	
	private Element K;
	private Element L;
	
	private Map<Integer, Element> attributeKeys; // map from attribute ID to attribute
												 // key (h_{x})^{t}
	
	public Waters08PrivateKey(){
		attributeKeys = new HashMap<Integer, Element>();
	}
	
	/**
	 * Constructs the base of a Waters08PrivateKey using the parameters
	 * k and l.
	 * @param k g^{alpha}*g^{a*t}
	 * @param l g^{t}
	 */
	public Waters08PrivateKey(Element k, Element l){
		K = k;
		L = l;
		
		attributeKeys = new HashMap<Integer, Element>();
	}
	
	/**
	 * Returns the K parameter of the private key.
	 */
	public Element getK(){
		return K;
	}
	
	/**
	 * Returns the L parameter of hte private key.
	 * @return
	 */
	public Element getL(){
		return L;
	}
	
	/**
	 * Returns the attribute key associated with the given attribute ID.
	 * @param attributeID id of the sought attribute ID
	 */
	public Element getAttributeKey(int attributeID){
		return attributeKeys.get(attributeID);
	}
	
	@Override
	public Collection<Integer> getAttributeIDs(){
		return attributeKeys.keySet();
	}
	
	/**
	 * Maps an attribute ID to a key for that attribute.
	 * @param attributeID id of attribute to add
	 * @param key key for that attribute
	 */
	public void addAttributeKey(int attributeID, Element key){
		attributeKeys.put(attributeID, key);
	}
	
	@Override
	public void initializeFromFile(MasterPublicParameters parameters,
			String path) throws IOException, InvalidPublicParametersException {
		
		if(!(parameters instanceof Waters08MasterPublicParameters))
			throw new InvalidPublicParametersException("Waters08MasterPublicParameters "+
					"required.");
		
		Waters08MasterPublicParameters params = (Waters08MasterPublicParameters) parameters;
		
		FileInputStream stream = new FileInputStream(path);
		DataInputStream in = new DataInputStream(stream);
		
		byte[] data = new byte[(int) new File(path).length()];
		in.readFully(data);
		
		deserialize(params, data);
	}
	
	@Override
	public void writeToFile(String path) throws IOException {
		FileOutputStream stream = new FileOutputStream(path);
		DataOutputStream out = new DataOutputStream(stream);
		
		out.write(serialize());
	}
	
	@Override
	public byte[] serialize() throws IOException{
		// Serialize the object
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);
		
		StreamUtils.writeAdvertisedBytes(out, K.toCanonicalRepresentation());
		StreamUtils.writeAdvertisedBytes(out, L.toCanonicalRepresentation());
		
		for(Entry<Integer, Element> entry : attributeKeys.entrySet()){
			out.writeInt(entry.getKey());
			StreamUtils.writeAdvertisedBytes(out, entry.getValue().toCanonicalRepresentation());
		}
		
		return stream.toByteArray();
	}
	
	@Override
	public void deserialize(MasterPublicParameters parameters, byte[] serialized)
			throws IOException, InvalidPublicParametersException{
		
		if(! (parameters instanceof Waters08MasterPublicParameters) ){
			throw new InvalidPublicParametersException("Waters08 master public parameters "+
					"required.");
		}
		
		Waters08MasterPublicParameters params = (Waters08MasterPublicParameters) parameters;
		
		ByteArrayInputStream stream = new ByteArrayInputStream(serialized);
		DataInputStream in = new DataInputStream(stream);
		
		K = params.getGroup().newElementFromBytes(StreamUtils.readAdvertisedBytes(in));
		L = params.getGroup().newElementFromBytes(StreamUtils.readAdvertisedBytes(in));
		
		while(in.available() > 0){ // Deserialize and add each attribute
 	    	int id = in.readInt();
 	    	Element attributeKey = params.getGroup().
 	    			newElementFromBytes(StreamUtils.readAdvertisedBytes(in));
 	    	attributeKeys.put(id, attributeKey);
 	    }
		
	}
	
	@Override
	public String toString(){
		
		StringBuilder b = new StringBuilder();
		b.append("K: "+K.toString()+"\n");
		b.append("L: "+L.toString()+"\n");
		
		for(Entry<Integer, Element> entry : attributeKeys.entrySet()){
			b.append("   "+entry.getKey()+": "+entry.getValue().toString()+"\n");
		}
	
		return b.toString();
	}
	
}
