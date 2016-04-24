package misc.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Utility class which provides functionality to prevent repeated code
 * while manipulating IO streams.
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class StreamUtils {

	/**
	 * Writes a sequence of bytes, preceded by an 'advertisement' of its length.
	 * @param out output stream to which to write
	 * @param data data to write
	 * @throws IOException if unable to write
	 */
	public static void writeAdvertisedBytes(DataOutputStream out, byte[] data)
			throws IOException {
		out.writeInt(data.length);
		out.write(data);
	}
	
	/**
	 * Reads a sequence of bytes as advertised by its length.
	 * @param in input stream from which to read
	 * @return byte array containing the byte array
	 */
	public static byte[] readAdvertisedBytes(DataInputStream in) throws IOException{
		byte[] data = new byte[ in.readInt() ];
		in.read(data);
		
		return data;
	}
	
	public static byte[] combineByteArrays(byte[][] arrays){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for(int j = 0; j < arrays.length; j++){
			try{
				out.write(arrays[j]);
			} catch(IOException e){ }
		}
		return out.toByteArray();
	}
}
