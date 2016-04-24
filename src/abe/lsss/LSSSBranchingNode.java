package abe.lsss;

/**
 * LSSSBranchingNode
 * 
 * Represents a generic logical branching gate (AND or OR)
 * within the tree representation of an access policy expressed 
 * as a monotone boolean formula.
 * 
 * (See http://www.cs.bgu.ac.il/~beimel/Papers/Survey.pdf for
 *  detailed descriptions of LSSS and SSS.) 
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public abstract class LSSSBranchingNode extends LSSSNode{
	private LSSSNode left; // Node on the left 
	private LSSSNode right; // Node on the right
	
	/**
	 * Constructs a branching LSSS tree structure node with the given left
	 * and right children.
	 * 
	 * Example construction:
	 * 		Access policy: {A} AND {B}
	 * 		Use: new LSSAndNode(Leaf{A}, Leaf{B})
	 * 
	 * @param left left child
	 * @param right right child
	 */
	public LSSSBranchingNode(LSSSNode left, LSSSNode right){
		this.left = left;
		this.right = right;
	}
	
	/**
	 * Returns the left child of this branching node.
	 */
	public LSSSNode leftChild(){
		return this.left;
	}
	
	/**
	 * Returns the right child of this branching node.
	 */
	public LSSSNode rightChild(){
		return this.right;
	}
}
