package abe;

import java.io.IOException;

import abe.ABEScheme.InvalidPublicParametersException;

/**
 * Object which encapsulates the master secret key for an attribute-based encryption system.
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class MasterSecretKey {
	/**
	 * Initializes master secret key based upon a file
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
	 * Writes the encapsulated secret key to a path
	 * in a format dictated by the implementation of the
	 * scheme.
	 * 
	 * @param path /path/too/file to which parameters should be written
	 */
	public abstract void writeToFile(String path) throws IOException;
	
	/**
	 * Serializes the master secret key to an array of bytes.
	 */
	public abstract byte[] serialize() throws IOException;
	
	/**
	 * Sets the internal state of this key according to a serialized form.
	 * 
	 * @param publicParameters public parameters providing mathematical context for
	 * this secret key
	 * @param serialized serialization of parameters to replicate
	 * 
	 * @throws IOException if unable to deserialize from stream
	 * @throws InvalidPublicParametersException if the given public parameters are incorrect
	 */
	public abstract void deserialize(MasterPublicParameters publicParameters,
			byte[] serialized) throws IOException, InvalidPublicParametersException;
}
