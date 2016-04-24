package aefs.keygen;

import java.util.ArrayList;
import java.util.List;

import misc.logging.SimpleLogger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import abe.schemes.waters08.Waters08ABEScheme;
import abe.schemes.waters08.Waters08MasterPublicParameters;
import abe.schemes.waters08.Waters08MasterSecretKey;
import abe.schemes.waters08.Waters08PrivateKey;

public class AEFSKeygen {

	public static void main(String[] args){
		SimpleLogger.setSource("AEFSKeygen");
		
		// Register command-line options
		Options options = new Options();
		options.addOption("help", "Prints help.");
		options.addOption("new_keypair", "Specifies that a new keypair should be "+
				"generated.");
		options.addOption("register_attributes", true, "Designates a list of attributes "+
				"that should be registered.");
		options.addOption("new_private_key", true, "Specifies that a new private key "+
				"should be generated for a given subset of attributes");
		options.addOption("num_bits", true, "Number of bits to use for key and parameter "+
				"generation.");
		options.addOption("params_path", true, "Path from which public parameters should "+
				"be read or modified.");
		options.addOption("msk_path", true, "Path from which master secret key should be "+
				"read or modified.");
		options.addOption("key_path", true, "Path from which a private key should be read "+
				"or modified.");
		
		CommandLineParser parser = new DefaultParser();
		try{
			// Parse options
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("help")){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "AEFSKeygen", options );
				
			} if(cmd.hasOption("new_keypair")){
				// Generation of new key pairs
				String paramsPath = cmd.getOptionValue("params_path");
				String mskPath = cmd.getOptionValue("msk_path");
				String bitStr = cmd.getOptionValue("num_bits");
				
				if(paramsPath == null || mskPath == null || bitStr == null){
					SimpleLogger.error("params_path, msk_path, or num_bits not specified.");
					System.exit(1);
				}
				
				int numBits = Integer.valueOf(bitStr);
				SimpleLogger.info("Generating MSK and public parameters in "+numBits+
						" bits.");
				
				// Initialize public parameters and write to file
				Waters08MasterPublicParameters p = new Waters08MasterPublicParameters();
				Waters08MasterSecretKey msk = (Waters08MasterSecretKey) 
						p.initializeRandomly(numBits);
				
				p.writeToFile(paramsPath);
				SimpleLogger.info("Public parameters written to "+paramsPath);
				
				// Write MSK to file
				msk.writeToFile(mskPath);
				SimpleLogger.info("MSK written to "+mskPath);
				
			} if(cmd.hasOption("register_attributes")){
				// Get semi-colon-separated list of attributes to register
				String[] attributes = cmd.getOptionValue("register_attributes").split(";");
				
				String paramsPath = cmd.getOptionValue("params_path");
				if(paramsPath == null){
					SimpleLogger.error("params_path not specified.");
					System.exit(1);
				}
				
				// Read in public parameters
				Waters08MasterPublicParameters p = new Waters08MasterPublicParameters();
				p.initializeFromFile(paramsPath);
				SimpleLogger.info("Public parameters loaded from "+paramsPath);
				
				// Register new attributes
				for(String attribute : attributes){
					p.registerNewAttribute(attribute);
					SimpleLogger.info("Registered "+attribute+" attribute.");
				}
				
				// Write the attributes to file
				p.writeToFile(paramsPath);
				SimpleLogger.info("Public parameters written to "+paramsPath);
				
			} if(cmd.hasOption("new_private_key")){
				// Generation of a new private key
				String[] attributes = cmd.getOptionValue("new_private_key").split(";");
				
				String paramsPath = cmd.getOptionValue("params_path");
				String mskPath = cmd.getOptionValue("msk_path");
				String keyPath = cmd.getOptionValue("key_path");
				
				if(paramsPath == null || mskPath == null || keyPath == null){
					SimpleLogger.error("params_path, msk_path, or key_path not specified.");
					System.exit(1);
				}
				
				// Load public parameters
				Waters08MasterPublicParameters p = new Waters08MasterPublicParameters();
				p.initializeFromFile(paramsPath);
				SimpleLogger.info("Public parameters loaded from "+paramsPath);
				
				// Load msk
				Waters08MasterSecretKey msk = new Waters08MasterSecretKey();
				msk.initializeFromFile(p, mskPath);
				SimpleLogger.info("MSK loaded from "+mskPath);
				
				// Generate private key
				Waters08ABEScheme scheme = new Waters08ABEScheme();
				scheme.setPublicParameters(p);
				scheme.setMasterSecretKey(msk);
				
				List<String> attributeList = new ArrayList<String>();
				for(String attribute : attributes) attributeList.add(attribute);
				
				Waters08PrivateKey key = (Waters08PrivateKey) 
						scheme.generatePrivateKey(attributeList);
				
				SimpleLogger.info("Private key generated.");
				
				// Save private key to file
				key.writeToFile(keyPath);
				
				SimpleLogger.info("Private key written to "+keyPath+".");
			}
			
		} catch(Exception e){
			SimpleLogger.error("AEFS Keygen failed.  Reason: "+e.getMessage());
		}
	}
	
}
