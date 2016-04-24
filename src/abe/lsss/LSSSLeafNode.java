package abe.lsss;
/**
 * LSSSLeafNode
 * 
 * Represents an AND gate within the tree representation of an
 * access policy expressed as a monotone boolean formula.
 * 
 * (See http://www.cs.bgu.ac.il/~beimel/Papers/Survey.pdf for
 *  detailed descriptions of LSSS and SSS.) 
 *  
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class LSSSLeafNode extends LSSSNode {
	
	private int identifierValue;
	
	/**
	 * Constructs an LSSSLeafNode for a given attribute
	 * identifier.
	 * 
	 * Example construction case:
	 * 		A resource only allows access to parties with 
	 *      the attribute {Admin}.  The numerical identifier
	 *      for Admin in the system is 0; the access structure 
	 *      for this policy would be a single LSSSLeafNode
	 *      constructed via new 'LSSSLeafNode(0)' 
	 * 
	 * @param attributeID numerical identifier of the attribute
	 * held by the leaf
	 */
	public LSSSLeafNode(int attributeID){
		this.identifierValue = attributeID;
	}

	@Override
	public ShareGeneratingMatrix getMatrix(int[] label, int counter){
		ShareGeneratingMatrix matrix = new ShareGeneratingMatrix();
		matrix.addRow(label, this.identifierValue);
		
		return matrix;
	}
}
