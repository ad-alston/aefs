package misc.io;

/**
 * Provides utility functions for operating on arrays.
 * @author Aubrey (ada2145@columbia.edu)
 */
public class ArrayUtils {
	
	/**
	 * Copies a subarray into another array.
	 * @param from array to copy from
	 * @param into array in which to copy
	 * @param segStart start of the subarray in from
	 * @param segEnd end (exclusive) of the subarray in from
	 * @param copyStart index at which to start copying values in into
	 */
	public static void copySubarray(byte[] from, byte[] into, int segStart, int segEnd,
			int copyStart){
		int n = 0;
		for(int i = segStart; i < segEnd; ++i){
			byte value = i < 0 ? 0 : from[i];
			into[n] = value;
			++n;
		}
	}
	
	public static byte[] copyOfRange(byte[] original, int start, int end){
		byte[] result = new byte[end - start];
		
		for(int i = start; i < end; ++i){
			result[i - start] = original[i % original.length];
		}
		
		return result;
	}
}
