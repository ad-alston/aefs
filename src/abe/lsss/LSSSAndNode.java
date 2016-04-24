package abe.lsss;
/**
 * LSSSAndNode
 * 
 * Represents an AND gate within the tree representation of an
 * access policy expressed as a monotone boolean formula.
 * 
 * (See http://www.cs.bgu.ac.il/~beimel/Papers/Survey.pdf for
 *  detailed descriptions of LSSS and SSS.) 
 *  
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class LSSSAndNode extends LSSSBranchingNode{
	
	/**
	 * Constructs an LSSSAndNode having the passed left and right
	 * children.
	 * @param left left child
	 * @param right right child
	 */
	public LSSSAndNode(LSSSNode left, LSSSNode right){
		super(left, right);
	}
	
	@Override
	public ShareGeneratingMatrix getMatrix(int[] label, int counter){
		int[] labelLeft = new int[counter+1];
		int[] labelRight = new int[counter+1];
		
		// Construct the left label as label|1; the right as (0. . . 0)|-1
		for(int i = 0; i < counter; i++){
			labelLeft[i] = label[i];
			labelRight[i] = 0;
		}
		labelLeft[counter] = 1;
		labelRight[counter] = -1;
		
		// Pass respective counter and increment counter + 1
		ShareGeneratingMatrix matrixLeft = leftChild().getMatrix(labelLeft, counter+1);
		ShareGeneratingMatrix matrixRight = rightChild().getMatrix(labelRight, counter+1);
		// NOTE:  By passing the labels as such, we reach a point where we need the additive
		// cancellations from the left and the right branches.  (I.e., we can only ever reach
		// the original labeling of (1, 0, 0, 0) if both subtrees can be satisfied
		// such that the 1 and the -1 from the left and the right cancel additively)
		
		// Combine matrices
		matrixLeft.joinWith(matrixRight);
		
		return matrixLeft;
	}
}
