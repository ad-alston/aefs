package abe;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Abstraction for ciphertext produced by an ABE scheme.
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class Ciphertext implements Serializable{

	/**
	 * Returns the auxiliary ciphertext associated with this ciphertext.
	 */
	public abstract AuxiliaryCiphertext getAuxiliary();
	
	/**
	 * Returns the raw content of this ciphertext.
	 */
	public abstract byte[] getRaw();
	
	/**
	 * Serializes this auxiliary ciphertext to an array of bytes.
	 */
	public abstract byte[] serialize() throws IOException;
	
	/**
	 * Deserializes an auxiliary ciphertext from an InputStream 
	 * source.  Modifies this object's state to match the stream.
	 * @param InputStream source
	 */
	public abstract void deserialize(InputStream source, MasterPublicParameters
			context) throws IOException;
}
