package abe;

import java.io.IOException;
import java.util.Iterator;

/**
 * Object which encapsulates the master public parameters (or master public key)
 * for an attribute-based encryption system.
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class MasterPublicParameters {
	
	/**
	 * Returns the maximum number of bits upon which this scheme can operate.
	 */
	public abstract int getOperableBitLength();
	
	/**
	 * Initializes master public parameters randomly.
	 * 
	 * @param bits indication of desired bit-length for critical security
	 * parameters (such as group moduli)
	 * 
	 * @return MasterSecretKey corresponding to the public parameters generated
	 */
	public abstract MasterSecretKey initializeRandomly(int bits);
	
	/**
	 * Initializes master public parameters based upon a file
	 * containing those parameters in the format expected by the
	 * implementation of the scheme.
	 * 
	 * @param path /path/to/file containing public parameters
	 */
	public abstract void initializeFromFile(String path) throws IOException;
	
	/**
	 * Writes the encapsulated public parameters to a path
	 * in a format dictated by the implementation of the
	 * scheme.
	 * 
	 * @param path /path/too/file to which parameters should be written
	 */
	public abstract void writeToFile(String path) throws IOException;
	
	/**
	 * Registers a new attribute within the system.
	 * 
	 * @param attribute new attribute to add
	 */
	public abstract void registerNewAttribute(String attribute);
	
	/**
	 * Returns the EntityAttribute encapsulating the attribute specified
	 * (by name).
	 * 
	 * @param attribute attribute name
	 * @return EntityAttribute object or null if no attribute exists within the system
	 */
	public abstract EntityAttribute getAttribute(String attribute);
	
	/**
	 * Returns the EntityAttribute encapsulating the attribute specified
	 * (by identifier).
	 * 
	 * @param attribute attribute identifier
	 * @return EntityAttribute object or null if no attribute exists within the system
	 */
	public abstract EntityAttribute getAttribute(int identifier);
	
	/**
	 * Returns an iterator over the attributes within the system.
	 */
	public abstract Iterator<? extends EntityAttribute> getAttributeIterator();
	
	/**
	 * Serializes the master public parameters to an array of bytes.
	 */
	public abstract byte[] serialize() throws IOException;
	
	/**
	 * Sets the internal state of these parameters according to a serialized form.
	 * 
	 * @param serialized serialization of parameters to replicate
	 */
	public abstract void deserialize(byte[] serialized) throws IOException;
}
