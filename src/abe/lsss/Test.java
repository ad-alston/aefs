package abe.lsss;

public class Test {
	public static void main(String[] args){
		LSSSNode n = new LSSSAndNode(new LSSSLeafNode(1), new LSSSLeafNode(2));
		n = new LSSSOrNode(new LSSSLeafNode(3), n);
		n = new LSSSAndNode(new LSSSLeafNode(0), n);

		ShareGeneratingMatrix m = n.getMatrix();
		
		for(int i = 0; i < m.matrix.length; i++){
			for(int j = 0; j < m.matrix[0].length; j++){
				System.out.print(m.matrix[i][j]+" ");
			}
			System.out.print("\n");
		}
		System.out.println(" ");
		m.computeReconstructionCoefficients();
		
		int[] rrs = m.getReconstructionCoefficients();
		
		for(int i = 0; i < rrs.length; i++){
			System.out.println(rrs[i]);
		}
	}
}
