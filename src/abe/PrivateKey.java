package abe;

import java.io.IOException;
import java.util.Collection;

import abe.ABEScheme.InvalidPublicParametersException;

/**
 * Object which encapsulates a secret key for an individual party within an 
 * attribute-based encryption scheme.
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class PrivateKey {

	/**
	 * Initializes secret key based upon a file
	 * containing those parameters in the format expected by the
	 * implementation of the scheme.
	 * 
	 * @param path /path/to/file containing public parameters
	 * 
	 * @throws IOException if unable to deserialize from stream
	 * @throws InvalidPublicParametersException if the given public parameters are incorrect
	 */
	public abstract void initializeFromFile(MasterPublicParameters publicParameters, 
			String path) throws IOException, InvalidPublicParametersException;
	
	/**
	 * Returns a collection of attribute IDs contained by this key.
	 */
	public abstract Collection<Integer> getAttributeIDs();
	
	/**
	 * Writes the encapsulated secret key to a path
	 * in a format dictated by the implementation of the
	 * scheme.
	 * 
	 * @param path /path/too/file to which parameters should be written
	 */
	public abstract void writeToFile(String path) throws IOException;
	
	/**
	 * Serializes the private key to an array of bytes.
	 * @return byte serialization of the private key
	 */
	public abstract byte[] serialize() throws IOException;
	
	/**
	 * Deserializes the private key from an array of bytes.
	 * 
	 * @param parameters master public parameters providing context for this private key
	 * @param serialized serialization of the private key
	 */
	public abstract void deserialize(MasterPublicParameters parameters, 
			byte[] serialized) throws IOException, 
		InvalidPublicParametersException;
	
}
