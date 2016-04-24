package abe.schemes.waters08;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import abe.Ciphertext;
import abe.lsss.LSSSAndNode;
import abe.lsss.LSSSLeafNode;
import abe.lsss.LSSSNode;
import abe.lsss.LSSSOrNode;
import abe.lsss.ShareGeneratingMatrix;


public class Test {
	public static void main(String[] args){
		try{
			
			Waters08MasterPublicParameters p = new Waters08MasterPublicParameters();
			Waters08MasterSecretKey msk = (Waters08MasterSecretKey) p.initializeRandomly(512);
			
			System.out.println("MSK, MPP generated");
			
			p.registerNewAttribute("attribute_1");
			p.registerNewAttribute("attribute_2");
			p.registerNewAttribute("attribute_3");
			p.registerNewAttribute("attribute_4");
			
			p.writeToFile("public.parameters");
			
			Waters08MasterPublicParameters p2 = new Waters08MasterPublicParameters();

			p2.initializeFromFile("public.parameters");
			
			msk.writeToFile("master.key");
			
			Waters08MasterSecretKey msk2 = new Waters08MasterSecretKey();
			msk2.initializeFromFile(p2, "master.key");
			
			Waters08ABEScheme scheme = new Waters08ABEScheme();
			scheme.setPublicParameters(p2);
			scheme.setMasterSecretKey(msk2);
			
			Pairing pa = p2.getPairing();
			Field g1 = pa.getG1();
			
			Element ea = g1.newRandomElement();
			Element eb = g1.newRandomElement();
			
			long ct = System.currentTimeMillis();
			pa.pairing(ea, eb);
			ct = System.currentTimeMillis() - ct;
			System.out.println(ct+"ms pairing");
			
			List<String> attributes = new ArrayList<String>();
			attributes.add("attribute_1");
			attributes.add("attribute_2");
			attributes.add("attribute_3");
			attributes.add("attribute_4");
			Waters08PrivateKey key = (Waters08PrivateKey) scheme.generatePrivateKey(attributes);
			
			System.out.println("PK produced");
			
			key.writeToFile("private.key");
			
			Waters08PrivateKey key2 = new Waters08PrivateKey();
			key2.initializeFromFile(p2, "private.key");
			
			LSSSNode n = new LSSSAndNode(new LSSSLeafNode(1), new LSSSLeafNode(2));
			n = new LSSSOrNode(new LSSSLeafNode(3), n);
			n = new LSSSAndNode(new LSSSLeafNode(0), n);
			// 0 AND ( 3 OR (1 AND 2) )
			
			ShareGeneratingMatrix m = n.getMatrix();
			
			BigInteger message = new BigInteger("123456");
			
			Ciphertext ctt = scheme.encrypt(m, message.toByteArray(), new SecureRandom());
			
			byte[] cd = ctt.serialize();
			Ciphertext cttt = new Waters08Ciphertext();
			cttt.deserialize(new ByteArrayInputStream(cd), p2);
			
			byte[] pt = scheme.decrypt(ctt, key2);
			BigInteger recons = new BigInteger(pt);
			
			System.out.println(recons.toString());
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
