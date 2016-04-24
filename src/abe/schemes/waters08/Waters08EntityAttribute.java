package abe.schemes.waters08;

import it.unisa.dia.gas.jpbc.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import misc.io.StreamUtils;
import abe.ABEScheme.InvalidPublicParametersException;
import abe.EntityAttribute;
import abe.MasterPublicParameters;

/**
 * Attribute encapsulation for an attribute within the Waters08
 * [ https://eprint.iacr.org/2008/290.pdf (section 3) ] ABE scheme.
 * 
 * Consists of (a) a name for the attribute (b) an integer identifier
 * and (c) a random integer mod the size of the elliptic curve group
 * to be used as an exponent in the scheme (h_{i} in the Waters08 
 * specification)
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class Waters08EntityAttribute extends EntityAttribute {
	
	private String name; // attribute name
	private int identifier; // attribute identifier
	private Element member; // h_{i}
	
	/**
	 * Constructs a Waters08EntityAttribute object.
	 * 
	 * @param name attribute name
	 * @param identifier attribute identifier
	 * @param member group element to be associated with this attribute
	 */
	public Waters08EntityAttribute(String name, int identifier, Element member){
		this.name = name;
		this.identifier = identifier;
		this.member = member;
	}
	
	public Element geth(){
		return member;
	}
	
	@Override
	public String getAttributeName(){
		return name;
	}
	
	@Override
	public int getIdentifier(){
		return identifier;
	}
	
	@Override
	public byte[] serialize() throws IOException{
		// Serialize the object
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);

		StreamUtils.writeAdvertisedBytes(out, name.getBytes());
		out.writeInt(identifier);
		StreamUtils.writeAdvertisedBytes(out, member.toCanonicalRepresentation());
		
		return stream.toByteArray();
	}
	
	@Override
	public void deserialize(MasterPublicParameters parameters, byte[] serialized) 
			throws IOException, InvalidPublicParametersException{
		
		if(!(parameters instanceof Waters08MasterPublicParameters))
			throw new InvalidPublicParametersException(
					"Waters08MasterPublicParameters required.");
		
		Waters08MasterPublicParameters params = (Waters08MasterPublicParameters) parameters;
		
		ByteArrayInputStream stream = new ByteArrayInputStream(serialized);
		DataInputStream in = new DataInputStream(stream);
		
		this.name = new String(StreamUtils.readAdvertisedBytes(in));
		this.identifier = in.readInt();
		this.member = params.getGroup().newElementFromBytes(StreamUtils.readAdvertisedBytes(in));
	}
	
	@Override
	public String toString(){
		return name+" ("+identifier+") "+member.toString();
	}
}
