package abe.lsss;

import java.io.Serializable;

/**
 * LSSSNode
 * 
 * Within the context of a linear secret sharing scheme (LSSS) and
 * an access policy expressed in the form of a monotone boolean
 * expression (with no repeated variables) of attributes, represents the 
 * head of a subtree within the tree representation of that policy.
 * 
 * (See http://www.cs.bgu.ac.il/~beimel/Papers/Survey.pdf for
 *  detailed descriptions of LSSS and SSS.) 
 *  
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class LSSSNode implements Serializable {
	
	/**
	 * Converts this the access structure rooted at this node to a
	 * share generating matrix.
	 * 
	 * @return equivalent share generating matrix
	 */
	public ShareGeneratingMatrix getMatrix(){
		ShareGeneratingMatrix matrix = getMatrix(new int[]{ 1 }, 1);
		// Finalize the contents of the matrix
		matrix.writeMatrix();
		
		return matrix;
	}
	
	/**
	 * Intermediate getMatrix routine to be called for any defined step in 
	 * the tree to matrix conversion procedure.
	 * 
	 * See (https://eprint.iacr.org/2010/351.pdf) for details.
	 * 
	 * @param label assigned by parent
	 * @param counter value received from parent
	 * @return ShareGeneratingMatrix equivalent to the access structure from 
	 * this subtree
	 */
	public abstract ShareGeneratingMatrix getMatrix(int[] label, int counter);
	
}
