package abe.schemes.waters08;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import misc.io.StreamUtils;
import abe.AuxiliaryCiphertext;
import abe.MasterPublicParameters;
import abe.lsss.ShareGeneratingMatrix;

/**
 * Auxiliary ciphertext content for the Waters08 ABE scheme.
 * 
 * @author Aubrey (ada2145@columbia.edu)
 */
public class Waters08AuxiliaryCiphertext extends AuxiliaryCiphertext implements Serializable{
	
	protected ShareGeneratingMatrix accessStructure;
	protected Element CPrime; // C'
	protected Waters08AuxiliaryPair[] pairs; // (C_i, D_i)
	
	private volatile Element multiplierElement = null;
	
	private int length;
	
	public Waters08AuxiliaryCiphertext(){ }
	
	public Waters08AuxiliaryCiphertext(ShareGeneratingMatrix accessStructure,
			Element CPrime, Waters08AuxiliaryPair[] pairs){
		this.accessStructure = accessStructure;
		this.CPrime = CPrime;
		this.pairs = pairs;
	}
	
	/**
	 * Returns the length of the plaintext to which this auxiliary ciphertext
	 * corresponds.
	 */
	public int getPlaintextLength(){
		return length;
	}
	
	/**
	 * Sets the length of the plaintext to which this auxiliary ciphertext
	 * corresponds.
	 */
	public void setPlaintextLength(int length){
		this.length = length;
	}
	
	public Element getMultiplierElement(){
		return multiplierElement;
	}
	
	@Override
	public void setMultiplierElement(Element mul){
		multiplierElement = mul;
	}
	
	@Override
	public void clearMultiplierElement(){
		if(multiplierElement != null)
			multiplierElement.setToOne();
		multiplierElement = null;
	}
	
	/**
	 * Returns the access structure (share generating matrix) associated 
	 * with this ciphertext.
	 */
	public ShareGeneratingMatrix getAccessStructure(){
		return accessStructure;
	}
	
	/**
	 * Returns the list (C_1, D_1) . . . (C_l, D_l) for this ciphertext.
	 */
	public Waters08AuxiliaryPair[] getPairs(){
		return pairs;
	}
	
	/**
	 * Returns C' for this ciphertext.
	 * @return
	 */
	public Element getCPrime(){
		return CPrime;
	}
	
	@Override
	public byte[] serialize() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		
		out.write(accessStructure.serialize());
		StreamUtils.writeAdvertisedBytes(out, CPrime.toCanonicalRepresentation());
		out.writeInt(pairs.length);
		for(int i = 0; i < pairs.length; i++){
			StreamUtils.writeAdvertisedBytes(out, pairs[i].C.toCanonicalRepresentation());
			StreamUtils.writeAdvertisedBytes(out, pairs[i].D.toCanonicalRepresentation());
		}
		
		return b.toByteArray();
	}
	
	@Override
	public void deserialize(InputStream source, MasterPublicParameters context)
			throws IOException{
		Waters08MasterPublicParameters ctx = (Waters08MasterPublicParameters) context;
		
		DataInputStream in = new DataInputStream(source);
		
		accessStructure = ShareGeneratingMatrix.deserialize(source);
		
		byte[] cPrimeBytes = StreamUtils.readAdvertisedBytes(in);
		Field field = ctx.getGroup();
		
		CPrime = field.newElementFromBytes(cPrimeBytes);
		
		pairs = new Waters08AuxiliaryPair[in.readInt()];
	
		for(int i = 0; i < pairs.length; i++){
			Element C = field.newElementFromBytes(
					StreamUtils.readAdvertisedBytes(in)
				);
			Element D = field.newElementFromBytes(
					StreamUtils.readAdvertisedBytes(in)
				);
			pairs[i] = new Waters08AuxiliaryPair(C,D);
		}
	}
	
	/**
	 * Auxiliary pairs accompanying the ciphertext (C_i, D_i from the Waters08
	 * paper).
	 */
	public static class Waters08AuxiliaryPair implements Serializable {
		
		public Element C;
		public Element D;
		
		public Waters08AuxiliaryPair(Element C, Element D){
			this.C = C;
			this.D = D;
		}
	}
}
