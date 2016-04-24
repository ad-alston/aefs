package abe;

import it.unisa.dia.gas.jpbc.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Encapsulation of the components of a ciphertext which may be used for
 * decryption but are not a function of the encrypted message within an
 * ABE scheme.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class AuxiliaryCiphertext implements Serializable {
	
	/**
	 * Returns the element to be multiplied against plaintexts to produce ciphertexts.
	 */
	public abstract Element getMultiplierElement();
	
	/**
	 * Sets the element to be used internally to encrypt plaintext blocks.
	 * @param mul element to be set as the element
	 */
	public abstract void setMultiplierElement(Element mul);
	
	/**
	 * Clears the multiplier element.
	 */
	public abstract void clearMultiplierElement();
	
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
