package abe.lsss;
/**
 * LSSSOrNode
 * 
 * Represents an OR gate within the tree representation of an
 * access policy expressed as a monotone boolean formula.
 * 
 * (See http://www.cs.bgu.ac.il/~beimel/Papers/Survey.pdf for
 *  detailed descriptions of LSSS and SSS.) 
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class LSSSOrNode extends LSSSBranchingNode{
	
	/**
	 * Constructs an LSSSOrNode having the passed left and right
	 * children.
	 * @param left left child
	 * @param right right child
	 */
	public LSSSOrNode(LSSSNode left, LSSSNode right){
		super(left, right);
	}
	
	@Override
	public ShareGeneratingMatrix getMatrix(int[] label, int counter){
		// Pass label on; leave counter the same
		ShareGeneratingMatrix matrixLeft = leftChild().getMatrix(label, counter);
		ShareGeneratingMatrix matrixRight = rightChild().getMatrix(label, counter);
		// NOTE: By passing the label as such, this subtree (characterized by the label)
		//       will be satisfied so long as either the left or the right subtrees can 
		//       satisfy.  (i.e., we can follow the chain upwards to reach the original label
		//       of (1, 0, 0 . . .) iff one of the children can also do so)
		
		// Combine matrices
		matrixLeft.joinWith(matrixRight);
		
		return matrixLeft;
	}
}
