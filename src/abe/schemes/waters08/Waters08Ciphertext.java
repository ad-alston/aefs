package abe.schemes.waters08;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import misc.io.StreamUtils;
import abe.AuxiliaryCiphertext;
import abe.Ciphertext;
import abe.MasterPublicParameters;

/**
 * Abstraction of the ciphertext produced by the Waters 08 ABE scheme.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class Waters08Ciphertext extends Ciphertext implements Serializable{
	
	private Waters08AuxiliaryCiphertext auxiliary;
	
	private byte[] content;
	
	public Waters08Ciphertext(){ }
	
	/**
	 * Constructs a Waters08Ciphertext object making use of the
	 * passed auxiliary ciphertext.
	 * 
	 * @param aux auxiliary ciphertext
	 */
	public Waters08Ciphertext(Waters08AuxiliaryCiphertext aux){
		auxiliary = aux;
		this.content = new byte[0];
	}
	
	/**
	 * Sets the primary ciphertext content.
	 */
	public void setContent(byte[] content){
		this.content = content;
	}
	
	/**
	 * Returns the primary ciphertext content.
	 */
	public byte[] getContent(){
		return this.content;
	}
	
	@Override
	public byte[] serialize() throws IOException{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		
		out.write(auxiliary.serialize());
		StreamUtils.writeAdvertisedBytes(out, content);
		
		return b.toByteArray();
	}
	
	@Override
	public void deserialize(InputStream source, MasterPublicParameters context)
			throws IOException{
		
		auxiliary = new Waters08AuxiliaryCiphertext();
		auxiliary.deserialize(source, context);
		
		content = StreamUtils.readAdvertisedBytes(new DataInputStream(source));
	}
	
	@Override
	public byte[] getRaw(){
		return getContent();
	}
	
	@Override
	public AuxiliaryCiphertext getAuxiliary(){
		return auxiliary;
	}

}
