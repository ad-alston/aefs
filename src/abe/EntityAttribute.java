package abe;

import java.io.IOException;

import abe.ABEScheme.InvalidPublicParametersException;

/**
 * Abstraction of an individual attribute.
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class EntityAttribute {
	
	/**
	 * Returns the name of this attribute.  (e.g. 'A' for attribute A)
	 */
	public abstract String getAttributeName();
	
	/**
	 * Returns the identifier of this attribute.
	 */
	public abstract int getIdentifier();
	
	/**
	 * Serializes this attribute to an array of bytes.
	 */
	public abstract byte[] serialize() throws IOException;
	
	/**
	 * Sets the internal state of this attribute according to a serialized form.
	 * 
	 * @param context master public parameters serving as the context of this attribute
	 * @param serialized serialization of attribute to replicate
	 */
	public abstract void deserialize(MasterPublicParameters context, 
			byte[] serialized) throws IOException, InvalidPublicParametersException;
}
