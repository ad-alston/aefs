package misc.numbers;

import java.math.BigInteger;
import java.util.Random;

/**
 * Utilities for operations utilizing arbitrary precision integers.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class BigIntegerUtilities {

	/**
	 * Returns a random BigInteger having value less than the passed bound
	 * parameter
	 * 
	 * @param generator RNG to use
	 * @param bound upper bound on number to generate
	 * @return
	 */
	public static BigInteger random(Random generator, BigInteger bound){
		// Expected BigInteger.new() calls: 2
		BigInteger r;
		do{
			r = new BigInteger(bound.bitLength(), generator);
		} while(r.compareTo(bound) >= 0);
		
		return r;
	}
	
	/**
	 * Returns a component to a curve (as a BigInteger).
	 * @param raw bytes containing the curve element as a contiguous array of x and y bytes
	 * @param x set to true if x dimension desired; else will return y dimension
	 */
	public static BigInteger curveComponent(byte[] raw, boolean x){
		byte[] coordinateBytes = new byte[raw.length/2];
		int i = x ? 0 : raw.length / 2;
		for(int j = 0; j < coordinateBytes.length; j++){
			coordinateBytes[j] = raw[i];
			++i;
		}
		return new BigInteger(coordinateBytes);
	}
	
	public static byte[] longToBytes(long value){
		return BigInteger.valueOf(value).toByteArray();
	}
}
