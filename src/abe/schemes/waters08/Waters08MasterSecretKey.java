package abe.schemes.waters08;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import misc.io.StreamUtils;
import abe.ABEScheme.InvalidPublicParametersException;
import abe.MasterPublicParameters;
import abe.MasterSecretKey;

/**
 * Master secret key object for the default implementation of the Waters08
 * https://eprint.iacr.org/2008/290.pdf (section 3)
 * attribute-based encryption scheme within aefs.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class Waters08MasterSecretKey extends MasterSecretKey {
	
	private Element g_alpha = null; // g^{alpha} from the Waters paper
	
	
	public Waters08MasterSecretKey(){ }
	
	/**
	 * Constructs a Waters08MasterSecretKey
	 * 
	 * @param g_alpha generator to the elliptic curve group chosen for the
	 * master public parameters, risen to the power of alpha (an integer mod 
	 * the order of the group)
	 */
	public Waters08MasterSecretKey(Element g_alpha){
		this.g_alpha = g_alpha;
	}
	
	/**
	 * Returns the secret g^{alpha} as given in the Waters08 paper.
	 */
	public Element getGPowAlpha(){
		return g_alpha;
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
		
		StreamUtils.writeAdvertisedBytes(out, g_alpha.toCanonicalRepresentation());
		
		return stream.toByteArray();
	}
	
	@Override
	public void deserialize(MasterPublicParameters parameters, byte[] serialized) 
			throws IOException, InvalidPublicParametersException {
		if(! (parameters instanceof Waters08MasterPublicParameters) ){
			throw new InvalidPublicParametersException("Waters08 master public parameters "+
					"required.");
		}
		
		Waters08MasterPublicParameters params = (Waters08MasterPublicParameters) parameters;
		
		ByteArrayInputStream stream = new ByteArrayInputStream(serialized);
		DataInputStream in = new DataInputStream(stream);
		
		Field group = params.getGroup();
		
		this.g_alpha = group.newElementFromBytes(StreamUtils.readAdvertisedBytes(in));
	}
	
	@Override
	public String toString(){
		return "g^{alpha}: "+g_alpha.toString();
	}
}
