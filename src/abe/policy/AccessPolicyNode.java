package abe.policy;

import abe.EntityAttribute;
import abe.MasterPublicParameters;
import abe.lsss.LSSSAndNode;
import abe.lsss.LSSSLeafNode;
import abe.lsss.LSSSNode;
import abe.lsss.LSSSOrNode;

/**
 * Intermediary tree node generated while parsing an attribute policy expression.
 */
public abstract class AccessPolicyNode {

	/**
	 * Converts this AccessPolicyNode to a LSSSNode that can be used to 
	 * specify a cryptographic access policy on a resource.
	 * @param params public parameters of the scheme being used
	 */
	public abstract LSSSNode toLSSSNode(MasterPublicParameters params)
		throws NoSuchAttributeException;
	
	/**
	 * Tree node representing a logical junction.
	 */
	public abstract static class AccessPolicyBranchingNode extends AccessPolicyNode{
		
		protected AccessPolicyNode left;
		protected AccessPolicyNode right;
		
		public AccessPolicyBranchingNode(){ }
		
		public AccessPolicyBranchingNode(AccessPolicyNode left, AccessPolicyNode right){
			this.left = left;
			this.right = right;
		}
	}
	
	/**
	 * Tree node representing an AND junction.
	 */
	public static class AccessPolicyAndNode extends AccessPolicyBranchingNode{ 
		
		public AccessPolicyAndNode(AccessPolicyNode left, AccessPolicyNode right){
			super(left, right);
		}
		
		@Override
		public LSSSNode toLSSSNode(MasterPublicParameters p) throws NoSuchAttributeException{
			return new LSSSAndNode(this.left.toLSSSNode(p), this.right.toLSSSNode(p));
		}
	}
	
	/**
	 * Tree node representing an OR junction.
	 */
	public static class AccessPolicyOrNode extends AccessPolicyBranchingNode{ 
		
		public AccessPolicyOrNode(AccessPolicyNode left, AccessPolicyNode right){
			super(left, right);
		}
		
		@Override
		public LSSSNode toLSSSNode(MasterPublicParameters p) throws NoSuchAttributeException{
			return new LSSSOrNode(this.left.toLSSSNode(p), this.right.toLSSSNode(p));
		}
		
	}
	
	/**
	 * Tree node representing a tree leaf (i.e., a single attribute).
	 */
	public static class AccessPolicyLeafNode extends AccessPolicyNode{
		
		private String attributeName;
		
		public AccessPolicyLeafNode(String attributeName){
			this.attributeName = attributeName;
		}
		
		@Override
		public LSSSNode toLSSSNode(MasterPublicParameters p) throws NoSuchAttributeException{
			EntityAttribute attr = p.getAttribute(attributeName);
			
			// Throw exception if attribute does not exist
			if(attr == null) throw new NoSuchAttributeException();
			
			return new LSSSLeafNode(attr.getIdentifier());
		}
		
	}
}
