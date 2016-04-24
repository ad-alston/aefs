package abe.lsss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ShareGeneratingMatrix
 * 
 * Within the context of a linear secret sharing scheme (LSSS) and
 * an access policy expressed in the form of a monotone boolean
 * expression (with no repeated variables) of attributes, represents 
 * the equivalent monotone span program.
 * 
 * (See http://www.cs.bgu.ac.il/~beimel/Papers/Survey.pdf for
 *  detailed descriptions of LSSS and SSS.) 
 *  
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class ShareGeneratingMatrix {
	
	protected HashMap<Integer, int[]> idToRow;
	
	protected HashMap<Integer, Integer> idToRowIndex;
	protected HashMap<Integer, Integer> rowIndexToId;
	
	protected int rowLength = 0; // Internal record of maximum row length seen
	protected int rowCount = 0;
	
	public int[][] matrix = null;
	public int[] reconstructionCoefficients = null;
	
	/**
	 * Constructs an empty ShareGeneratingMatrix.
	 */
	public ShareGeneratingMatrix(){
		idToRow = new HashMap<Integer, int[]>();
		idToRowIndex = new HashMap<Integer, Integer>();
		rowIndexToId = new HashMap<Integer, Integer>();
	}
	
	/**
	 * Returns a map from an attribute id to a corresponding row in the
	 * ShareGeneratingMatrix.
	 */
	public int[][] getMatrix(){
		return matrix;
	}
	
	/**
	 * Returns a map from an attribute id to a corresponding row in the
	 * ShareGeneratingMatrix.
	 */
	public HashMap<Integer, int[]> getMapping(){
		return idToRow;
	}
	
	/**
	 * Returns the reconstruction coefficients for this share generating matrix.
	 */
	public int[] getReconstructionCoefficients(){
		return reconstructionCoefficients;
	}
	
	/**
	 * Returns the attribute ID associated with the given row index.
	 */
	public Integer getAttributeIDForRow(int row){
		return rowIndexToId.get(row);
	}
	
	/**
	 * Adds a new row to the ShareGeneratingMatrix. 
	 * @param row row to add
	 * @param attributeID attribute ID corresponding to this row
	 * 
	 * @throws IllegalStateException if writeMatrix() has been called
	 * @throws IllegalStateException if a repeated attributeID is seen
	 */
	public void addRow(int[] row, int attributeID){
		if(this.matrix != null){ // Verify that the matrix has not yet been written
			throw new 
				IllegalStateException("Cannot add row after writeMatrix() has been called.");
		}
		if(idToRow.get(attributeID) != null){
			throw new
				IllegalStateException("Repeated attribute ID found.  Attribute structure "+
						"must represent monotone boolean formula with no repeated variables.");
		}
		this.idToRow.put(attributeID, row);
		this.idToRowIndex.put(attributeID, rowCount);
		this.rowIndexToId.put(rowCount, attributeID);
		if(row.length > this.rowLength) this.rowLength = row.length;
		rowCount++;
	}
	
	/**
	 * Performs a union operation between this and another ShareGeneratingMatrix.
	 * This modifies the current ShareGeneratingMatrix in place.
	 * @param other the other ShareGeneratingMatrix with which to join
	 * @throws IllegalStateException if writeMatrix() has been called
	 * @throws IllegalStateException if a repeated attributeID is seen (this implies
	 * repeated variables)
	 */
	public void joinWith(ShareGeneratingMatrix other){
		if(this.matrix != null){ // Verify that the matrix has not yet been written
			throw new 
				IllegalStateException("Cannot modify after writeMatrix() has been called.");
		}
		
		for(Map.Entry<Integer, int[]> entry : other.getMapping().entrySet()) {
	        Integer id = entry.getKey();
	        int[] row = entry.getValue();
	        this.addRow(row, id);
	    }
	}
	
	/**
	 * Given its current state and internal mapping, writes the content
	 * of the ShareGeneratingMatrix internally as a static two-dimensional array.
	 */
	public void writeMatrix(){
		this.matrix = new int[this.rowCount][this.rowLength];
		int i = 0;
		
		for(Map.Entry<Integer, int[]> entry : this.idToRow.entrySet()) {
	        Integer id = entry.getKey();
	        int[] row = entry.getValue();
	        
	        for(int j = 0; j < rowLength; j++){
	        	if(j < row.length)
	        		matrix[i][j] = row[j];
	        	else
	        		matrix[i][j] = 0;
	        }
	        
	        idToRow.put(id, matrix[i]);
	        idToRowIndex.put(id,  i);
	        rowIndexToId.put(i,  id);
	        i++;
	    }
	}
	
	/**
	 * Internally computes the reconstruction coefficients for this 
	 * share generating matrix.  The 'reconstruction coefficients' are the
	 * coefficients w_i correpsonding to the linear reconstruction 
	 * property described here: https://eprint.iacr.org/2008/290.pdf
	 * 
	 * @throws IllegalStateException if matrix has not yet been written
	 */
	public void computeReconstructionCoefficients(Set<Integer> zeros){
		if(this.matrix == null){
			throw new IllegalStateException("Cannot compute reconstruction "+
					"coefficients when matrix has not been written.");
		}
		// The vector of secret reconstruction coefficients corresponds
		// to the vector w in the equation M^{T}w = (1, 0, 0 . . . )^{T}.
		
		// Use regular Gaussian elimination to compute the reconstruction
		// coefficients.
		int[][] workingMatrix = 
				new int[this.matrix[0].length][this.matrix.length+1];
		
		// copy the matrix and target vector (1, 0, 0, 0 .  . .) ^ {T} into
		// the working matrix
		for(int i = 0; i < this.matrix.length; i++){
			for(int j = 0; j < this.matrix[0].length; j++)
				workingMatrix[j][i] = this.matrix[i][j];
		}
		for(int i = 1; i < workingMatrix.length; i++){
			workingMatrix[i][workingMatrix[0].length-1] = 0;
		}
		workingMatrix[0][workingMatrix[0].length-1] = 1;
		
		for(int row : zeros){
			for(int i = 0; i < workingMatrix.length; i++){
				workingMatrix[i][row] = 0;
			}
		}
		
		int[] coefficients = new int[workingMatrix[0].length - 1];
		
		// Forward elimination
		for(int i = 0; i < workingMatrix.length-1; i++){
			if(workingMatrix[i][i] == 0){ // If current focus is 0, seek non-zero
										  // column value and swap
				for(int j = i+1; j < workingMatrix.length; j++){
					if(workingMatrix[j][i] != 0){
						int[] tmp = workingMatrix[j];
						workingMatrix[j] = workingMatrix[i];
						workingMatrix[i] = tmp;
						break;
					}
				}
			}
			// Row modifications to coerce upper triangular form
			for(int j = i+1; j < workingMatrix.length; j++){
				if(workingMatrix[i][i] == 0) continue;
				
				// If the pivot does not divide, make sure it does
				if(workingMatrix[j][i] % workingMatrix[i][i] != 0){
					for(int k = i; k < workingMatrix[0].length; k++){
						workingMatrix[j][k] *= workingMatrix[i][i];
					}
				}
				
				// Normal forward elimination
				workingMatrix[j][i] = (int) 
						(workingMatrix[j][i] / workingMatrix[i][i]);
				for(int k = j + 1; k < workingMatrix[0].length; k++){
					workingMatrix[j][k] -= 
							workingMatrix[i][k] * workingMatrix[j][i];
				}
			}
		}
		
		for(int i = workingMatrix.length; i < coefficients.length; i++){
			// Initialize free variables randomly on interval [0, 1]
			coefficients[i] = 1;
		}
		
		for(int i = workingMatrix.length - 1; i >= 0; i--){
			if(workingMatrix[i][i] == 0) continue;
			int t = 0;
			for(int j = i + 1; j < workingMatrix[0].length-1; j++){
				t += workingMatrix[i][j] * coefficients[j];
			}
			t = workingMatrix[i][workingMatrix[0].length-1] - t;
			if(t % workingMatrix[i][i] != 0){
				coefficients[i] = t;
				for(int q = i + 1; q < coefficients.length; q++){
					coefficients[q] *= workingMatrix[i][i];
				}
			} else{
				coefficients[i] = t / workingMatrix[i][i];
			}
		}
		
		this.reconstructionCoefficients = coefficients;
	}
	
	/**
	 * Converts this share generating matrix to an array of bytes.
	 */
	public byte[] serialize() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream d = new DataOutputStream(b);
		
		d.writeInt(matrix.length);
		d.writeInt(matrix[0].length);
		
		for(int i = 0; i < matrix.length; i++){
			for(int j = 0; j < matrix[0].length; j++)
				d.writeInt(matrix[i][j]);
		}
		
		d.writeInt(rowLength);
		d.writeInt(rowCount);
		
		d.writeInt(idToRowIndex.size());
		for(Integer i : idToRowIndex.keySet()){
			d.writeInt(i);
			d.writeInt(idToRowIndex.get(i));
		}
		
		return b.toByteArray();
	}
	
	/**
	 * Converts this share generating matrix to an array of bytes.
	 */
	public static ShareGeneratingMatrix deserialize(InputStream inp) throws IOException {
		DataInputStream in = new DataInputStream(inp);
		
		int r = in.readInt(); int c = in.readInt();
		int[][] matrix = new int[r][c];
		
		for(int i = 0; i < matrix.length; i++){
			for(int j = 0; j < matrix[0].length; j++){
				matrix[i][j] = in.readInt();
			}
		}
		
		
		int rowLength = in.readInt();
		int rowCount = in.readInt();

		HashMap<Integer, Integer> idToRowIndex = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> rowIndexToId = new HashMap<Integer, Integer>();
		
		int nkeys = in.readInt();
		for(int i = 0; i < nkeys; i++){
			int id = in.readInt();
			int row = in.readInt();
			idToRowIndex.put(id, row);
			rowIndexToId.put(row, id);
		}
		
		ShareGeneratingMatrix s = new ShareGeneratingMatrix();
		
		s.matrix = matrix;
		s.rowLength = rowLength;
		s.rowCount = rowCount;
		s.idToRowIndex = idToRowIndex;
		s.rowIndexToId = rowIndexToId;
		
		return s;
	}
	
}
