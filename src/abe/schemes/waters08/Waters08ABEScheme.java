package abe.schemes.waters08;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import misc.numbers.BigIntegerUtilities;
import abe.ABEScheme;
import abe.AuxiliaryCiphertext;
import abe.Ciphertext;
import abe.EntityAttribute;
import abe.MasterPublicParameters;
import abe.MasterSecretKey;
import abe.PrivateKey;
import abe.lsss.ShareGeneratingMatrix;
import abe.schemes.waters08.Waters08AuxiliaryCiphertext.Waters08AuxiliaryPair;

/**
 * ABEScheme object for the default implementation of the Waters08
 * https://eprint.iacr.org/2008/290.pdf (section 3)
 * attribute-based encryption scheme within aefs.
 * 
 * This default implementation relies on random elliptic curve groups of order
 * r (where r is a Solinas prime), defined on top of a randomly chosen finite field
 * having prime modulo q such that r + 1 = q * (small value); the implementation
 * is also achieved using the JPBC Java port of the C-based PBC library.
 *
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class Waters08ABEScheme extends ABEScheme {
	
	private Waters08MasterPublicParameters publicParameters = null;
	private Waters08MasterSecretKey masterSecretKey = null;
	
	@Override
	public Ciphertext encrypt(ShareGeneratingMatrix accessStructure, byte[] plaintext,
			Random rng){
		Waters08AuxiliaryCiphertext aux = (Waters08AuxiliaryCiphertext)
				computeAuxiliaryCiphertext(accessStructure, rng);
		Waters08Ciphertext ct = new Waters08Ciphertext(aux);
		
		Field field = publicParameters.getPairing().getGT();
		BigInteger numeric = new BigInteger(plaintext);
		Element asElement = field.newElement(numeric);
		
		Element encrypted = asElement.mul(aux.getMultiplierElement());
		
		ct.setContent(encrypted.toCanonicalRepresentation());
		
		// Clear the multiplier so it can't be reused
		aux.clearMultiplierElement();
		
		return ct;
	}
	
	@Override
	public byte[] decrypt(Ciphertext ct, PrivateKey k)
			throws InvalidCiphertextException, InvalidPrivateKeyException{
		
		Waters08Ciphertext c = (Waters08Ciphertext) ct;
		Waters08AuxiliaryCiphertext aux = (Waters08AuxiliaryCiphertext)
				ct.getAuxiliary();
		Waters08PrivateKey key = (Waters08PrivateKey) k;
		
		// Calculate reconstruction coefficients within the access structure
		// according to the actual held coefficients.
		ShareGeneratingMatrix m = aux.getAccessStructure();
		Set<Integer> missed = new HashSet<Integer>();
		int[][] mat = m.getMatrix();
		for(int i = 0; i < mat.length; i++){
			int attID = m.getAttributeIDForRow(i);
			if(key.getAttributeKey(attID)==null){
				missed.add(i);
			}
		}

		m.computeReconstructionCoefficients(missed);
		
		// Compute the multiplier
		Element multiplier = computeMultiplier(aux, k);

		Field field = publicParameters.getPairing().getGT();
		
		byte[] plaintextBytes = field.newElementFromBytes(c.getContent()).
				div(multiplier).toBigInteger().toByteArray();
		
		multiplier.setToOne(); // clear the multiplier
		aux.clearMultiplierElement();
		
		return plaintextBytes;
	}
	
	@Override
	public AuxiliaryCiphertext 
			computeAuxiliaryCiphertext(ShareGeneratingMatrix accessStructure,
					Random rng){
		if(publicParameters == null)
			throw new IllegalStateException("No public parameters set for this scheme.");
		
		// Get matrix underlying the access structure and its dimensions
		int[][] M = accessStructure.getMatrix();
		int l = M.length;
		int n = M[0].length;
		
		// Generate random vector v in (Z_p)^n
		// note: v[0] == s
		BigInteger[] v = new BigInteger[n];
		// Generate secret for 
		for(int i = 0; i < n; i++)
			v[i] = BigIntegerUtilities.random(rng, publicParameters.getGroup().getOrder());
		
		// Generate vector of lambda values alongside random vector r
		BigInteger[] r = new BigInteger[l];
		BigInteger[] lambda = new BigInteger[l];
		
		for(int i = 0; i < l; i++){
			BigInteger lambdaVal = BigInteger.ZERO;
			for(int j = 0; j < n; j++){
				lambdaVal = lambdaVal.add(v[j].multiply(BigInteger.valueOf(M[i][j])));
			}
			lambda[i] = lambdaVal;
			r[i] = BigIntegerUtilities.random(rng, publicParameters.getGroup().getOrder());
		}
		
		// C' = g^{s}
		Element Cp = publicParameters.getGenerator().duplicate().pow(v[0]);
		
		Waters08AuxiliaryPair[] pairs = new Waters08AuxiliaryPair[l];
		
		for(int i = 0; i < l; i++){
			int id = accessStructure.getAttributeIDForRow(i);
			Waters08EntityAttribute attribute = publicParameters.getAttribute(id);
			
					     // g^{a * lambda_i} * h^{-r_i}
			Element Cl = null;
			
			if(lambda[i].compareTo(BigInteger.ZERO) > 0){
				Cl = publicParameters.getGPowA().duplicate().pow(lambda[i]).
					div(attribute.geth().duplicate().pow(r[i]));
			} else{
				Cl = publicParameters.getGroup().newOneElement().div(
						publicParameters.getGPowA().duplicate().pow(lambda[i].abs())).
						div(attribute.geth().duplicate().pow(r[i]));
			}
			
			// g^{r_i}
			Element Dl = publicParameters.getGenerator().duplicate().pow(r[i]);
			
			pairs[i] = new Waters08AuxiliaryPair(Cl, Dl);
		} 
		
		Waters08AuxiliaryCiphertext aux = new Waters08AuxiliaryCiphertext(accessStructure,
				Cp, pairs);
		
		// C = M * e(g,g)^{alpha * s}
		Element multiplier = 
				publicParameters.getPairingAlpha().duplicate().pow(v[0]);
		
		aux.setMultiplierElement(multiplier);
		
		return aux;
	}
	
	@Override
	public Element computeMultiplier(AuxiliaryCiphertext c, PrivateKey sk) throws 
			InvalidCiphertextException, InvalidPrivateKeyException {
		if(! (c instanceof Waters08AuxiliaryCiphertext))
			throw new InvalidCiphertextException("Ciphertext not a Waters08AuxiliaryCiphertext.");
		if(! (sk instanceof Waters08PrivateKey))
			throw new InvalidPrivateKeyException("Private key not Waters08PrivateKey.");
		
		Waters08AuxiliaryCiphertext aux = (Waters08AuxiliaryCiphertext) c;
		Waters08PrivateKey k = (Waters08PrivateKey) sk;
		
		Pairing pairingOperator = publicParameters.getPairing();
		
		Element numerator = pairingOperator.pairing(aux.getCPrime(), k.getK()); // e(C',K)
		Waters08AuxiliaryPair[] pairs = aux.getPairs();
		Element denominator = null;
		
		int[] w = aux.getAccessStructure().getReconstructionCoefficients(); // w_i
		int i = 0;
		do{
			int attributeID = aux.getAccessStructure().getAttributeIDForRow(i);
			
			if(k.getAttributeKey(attributeID) == null){
				i++; continue; // skip if attribute not held
			}
			
			Element a = pairingOperator.pairing(pairs[i].C, k.getL()); // e(C_i, L)
			Element b = pairingOperator.pairing(pairs[i].D, k.getAttributeKey(attributeID));
					// e(D_i, K_{f(i)})
			Element r = a.mul(b);
			
			BigInteger wi = BigInteger.valueOf(w[i]);
			
			if(denominator == null){
				if(wi.compareTo(BigInteger.ZERO) < 0){
					denominator = pairingOperator.getGT().newOneElement().div(r.pow(wi.abs()));
				} else{
					denominator = r.pow(wi);
				}
			} else{
				if(wi.compareTo(BigInteger.ZERO) < 0){
					denominator = denominator.div(r.pow(wi.abs()));
				} else{
					denominator = denominator.mul(r.pow(wi));
				}
			}
			
			i++;
		} while(i < pairs.length);
		
		Element multiplier = numerator.div(denominator);
		
		return multiplier;
	}
	
	@Override
	public PrivateKey generatePrivateKeyInternal(List<? extends EntityAttribute>
			attributes) throws InvalidAttributeException{
		// Generate private key according to section 3 of the Waters 08 paper.
		if(masterSecretKey == null)
			throw new IllegalStateException("No master secret key set for this scheme.  "+
					"Cannot generate private keys without MSK.");
		
		if(publicParameters == null)
			throw new IllegalStateException("No public parameters set for this scheme.  "+
					"Cannot generate private keys without public parameters.");
		
		SecureRandom rng = new SecureRandom();
		
		BigInteger tSecret = BigIntegerUtilities.random(rng, 
				publicParameters.getGroup().getOrder()); // Choose t
		
		Element k = masterSecretKey.getGPowAlpha().duplicate(). // g^{alpha} 
					mul(publicParameters.getGPowA().duplicate().pow(tSecret)); // * g^(at)
		Element l = publicParameters.getGenerator().duplicate().pow(tSecret); // g^t
		
		Waters08PrivateKey key = new Waters08PrivateKey(k, l);
		
		for(EntityAttribute att : attributes){
			if(att == null || !(att instanceof Waters08EntityAttribute)){
				throw new InvalidAttributeException("Invalid attribute passed.");
			}
			
			Waters08EntityAttribute a = (Waters08EntityAttribute) att;
			Element hPowT = a.geth().duplicate().pow(tSecret); // (h_x)^{t}
			
			key.addAttributeKey(a.getIdentifier(), hPowT);
		}
		
		return key;
		
	}
	
	@Override
	public void setPublicParameters(MasterPublicParameters parameters)
			throws InvalidPublicParametersException{
		
		if(!(parameters instanceof Waters08MasterPublicParameters))
			throw new InvalidPublicParametersException("Waters08MasterPublicParameters "+
					"required for Waters08ABEScheme.");
		
		publicParameters = (Waters08MasterPublicParameters) parameters;
	}
	
	@Override
	public void setMasterSecretKey(MasterSecretKey key)
			throws InvalidMasterSecretKeyException{
		
		if(!(key instanceof Waters08MasterSecretKey))
			throw new InvalidMasterSecretKeyException("Waters08MasterPublicParameters "+
					"required for Waters08ABEScheme.");
		
		masterSecretKey = (Waters08MasterSecretKey) key;
	}
	
	@Override
	public MasterPublicParameters getPublicParameters(){
		return publicParameters;
	}
	
	@Override
	public MasterSecretKey getMasterSecretKey(){
		return masterSecretKey;
	}
}
