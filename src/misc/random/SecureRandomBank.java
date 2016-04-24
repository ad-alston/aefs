package misc.random;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Rotating queue of SecureRandom sources.  Aimed at decreasing contention
 * among multiple requests for secure randomness.
 */
public class SecureRandomBank {
	private Queue<SecureRandom> rngs;
	
	public SecureRandomBank(int n){
		rngs = new LinkedList<SecureRandom>();
		for(int i = 0; i < n; i++){
			rngs.add(new SecureRandom());
		}
	}
	
	public synchronized SecureRandom nextRNG(){
		SecureRandom rng = rngs.poll();
		
		rngs.add(rng);
		
		return rng;
	}
}
