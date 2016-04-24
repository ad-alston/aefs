package aefs.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import misc.logging.SimpleLogger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import abe.ABEScheme.InvalidPublicParametersException;
import abe.MasterPublicParameters;
import abe.PrivateKey;
import abe.schemes.waters08.Waters08MasterPublicParameters;
import abe.schemes.waters08.Waters08PrivateKey;
import aefs.protocols.authorization.MasterSessionKey;
import aefs.protocols.authorization.MasterSessionToken;
import aefs.protocols.requests.ClientMasterSessionRequest;
import aefs.protocols.requests.ClientRequest;
import aefs.protocols.requests.ClientRequestFactory;
import aefs.protocols.requests.InvalidCommandException;
import aefs.protocols.requests.MalformedRequestException;

public class AEFSClient {
	
	public String masterAddress;
	public Integer masterPort;
	
	public MasterPublicParameters publicParams;
	public PrivateKey privateKey;
	
	public MasterSessionKey sessionKey = null;
	public MasterSessionToken sessionToken = null;;
	
	/**
	 * Initializes an AEFSClient.
	 * @param masterAddress address of the AEFS master
	 * @param masterPort port on which the AEFS master is listening
	 * @param paramsPath path to public parameters
	 * @param keyPath path to private key
	 */
	public AEFSClient(String masterAddress, Integer masterPort, 
				String paramsPath, String keyPath)
			throws IOException, InvalidPublicParametersException {
		publicParams = new Waters08MasterPublicParameters();
		publicParams.initializeFromFile(paramsPath);
		SimpleLogger.info("Public parameters loaded successfully.");
		
		privateKey = new Waters08PrivateKey();
		privateKey.initializeFromFile(publicParams, keyPath);
		SimpleLogger.info("Private key loaded successfully.");
	
		this.masterAddress = masterAddress;
		this.masterPort = masterPort;
		
		// Form master session request and attempt to receive an authorization
		// token.
		ArrayList<String> attributes = new ArrayList<String>();
		for(Integer id : privateKey.getAttributeIDs()){
			attributes.add(publicParams.getAttribute(id).getAttributeName());
		}
		long ttl = 3600 * 24; // ttl of 1 day
		
		ClientMasterSessionRequest request = 
				new ClientMasterSessionRequest(attributes, ttl);
		try{
			SimpleLogger.info("Requesting master session token from AEFS master...");
			request.initiateRequest(this);
			SimpleLogger.info("Session token received.");
		} catch(MalformedRequestException e){
			SimpleLogger.error("Unable to receive attribute authorization from "+
					"master: "+e.getMessage());
		}
	}
	
	/**
	 * Command line interface to the AEFS client.
	 */
	public static void main(String[] args){
		SimpleLogger.setSource("AEFSClient");
		
		// Register command-line options
		Options options = new Options();
		options.addOption("help", "Prints help.");
		
		options.addOption("params_path", true, "Path from which the AEFS master public "+
				"parameters should be read.");
		options.addOption("key_path", true, "Path from the private key should be read.");
		options.addOption("master_address", true, "Address pointing to the AEFS master.");
		
		CommandLineParser parser = new DefaultParser();
		try{
			// Parse options
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("help")){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "AEFSClient", options );
			} else{
				String paramsPath = cmd.getOptionValue("params_path");
				String keyPath = cmd.getOptionValue("key_path");
				String masterAddr = cmd.getOptionValue("master_address");
				
				if(paramsPath == null || keyPath == null || masterAddr == null){
					SimpleLogger.error("Invalid parameters.  Please see '-help'.");
					System.exit(1);
				}
				
				// Parse port from address
				Integer masterPort = null;
				if(masterAddr.contains(":")){
					try{
						String[] split = masterAddr.split(":");
						masterAddr = split[0];
						masterPort = Integer.parseInt(split[1]);
					} catch(NumberFormatException e){}
				}
				if(masterPort == null){
					SimpleLogger.error("Invalid master address.  Must be in form <IP>:<port>.");
				}
				
				// Load public parameters and private key
				MasterPublicParameters publicParams = new Waters08MasterPublicParameters();
				publicParams.initializeFromFile(paramsPath);
				SimpleLogger.info("Public parameters loaded successfully.");
				
				PrivateKey privateKey = new Waters08PrivateKey();
				privateKey.initializeFromFile(publicParams, keyPath);
				SimpleLogger.info("Private key loaded successfully.");
				
				AEFSClient client = new AEFSClient(masterAddr, masterPort, 
						paramsPath, keyPath);
				
				// check for session key
				if(client.sessionKey == null){
					SimpleLogger.error("Unable to establish session key.  Exiting.");
					System.exit(1);
				} else if(client.sessionToken == null){
					SimpleLogger.error("No session token received.  Exiting.");
				}
				
				SimpleLogger.info("Client started.  Use <Ctrl-C> to quit.");
				
				// Set up simple command input loop.
				Thread executionThread = new Thread(){
					public void run(){
						try{
							Scanner in = new Scanner(System.in);
							while(true){
								System.out.print("AEFS> ");
								
								String input = in.nextLine();
								if(input.length() > 0){
									ClientRequest request = null;
									try{
										// Parse command
										request = ClientRequestFactory.
												requestFromCommandString(input);
										
										// Send request
										request.initiateRequest(client);
									} catch(InvalidCommandException e){
										SimpleLogger.error(e.getMessage());
									} catch(MalformedRequestException e){
										SimpleLogger.error(e.getMessage());
									}
								}
								
							}
						} catch(NoSuchElementException e){
							// Silently ignore scanner death on Ctrl-c.
						}
					}
				};
				
				// Start execution
				executionThread.start();
				
				// Add shutdown hook to allow the client to be properly stopped.
				Runtime.getRuntime().addShutdownHook(new Thread()
		        {
		            public void run(){
		            	System.out.println("");
		            	SimpleLogger.info("AEFS client exiting.");
		            }
		            
		        });
			}
		} catch(Exception e){
			SimpleLogger.error("AEFS encountered an error and must terminate: "+e.
					getMessage());
		}
	}
}
