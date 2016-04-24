package aefs.nodes.master;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import misc.logging.SimpleLogger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import abe.MasterPublicParameters;
import abe.PrivateKey;
import abe.schemes.waters08.Waters08MasterPublicParameters;
import abe.schemes.waters08.Waters08PrivateKey;
import aefs.encryption.rsa.GenericRSAKey;
import aefs.encryption.rsa.RSAKeyManager;
import aefs.nodes.InitializationException;

public class AEFSMasterNode {

	public MasterPublicParameters publicParameters;
	
	public GenericRSAKey publicTAKey;
	public GenericRSAKey privateTAKey;
	
	public PrivateKey key; // find a better way to do this
	
	private ThreadPoolExecutor jobPool;
	
	private AEFSMasterMonitor monitor;
	
	private int port;
	
	private ServerSocket listener;

	// TODO: HAVE THESE BE CONFIGURABLE
	
	public Set<String> volatileAttributes;
	public String serviceNodeAttribute = "server";
	public static long MAX_TTL = 86400;
	
	public Set<GenericRSAKey> trustedTAKeys;
	
	/**
	 * Initializes an AEFSMasterNode object.
	 * @param publicParametersPath path to file containing public parameters
	 * @param keyPath path to file containing the master node's private key
	 * @param numThreads number of threads to use to respond to requests
	 * @param port port on which to listen for requests
	 * @param trustPath path to directory containing trusted ticket authority public keys
	 * @param publicRSAPath path to file containing public RSA key
	 * @param privateRSAPath path to file containing private RSA key
	 * @throws InitializationException if unable to instantiate node
	 */
	public AEFSMasterNode(String publicParametersPath, String keyPath, int numThreads,
			int port, String trustPath, String publicRSAPath, String privateRSAPath) 
					throws InitializationException {
		try{
			// TODO: this should be configured
			this.volatileAttributes = new HashSet<String>();
			volatileAttributes.add("r1");
			volatileAttributes.add("r2");
			volatileAttributes.add("r3");
			volatileAttributes.add("r4");
			
			// Load public parameters
			SimpleLogger.info("Loading public parameters...");
			publicParameters = new Waters08MasterPublicParameters();
			publicParameters.initializeFromFile(publicParametersPath);
			SimpleLogger.info("Public parameters loaded from "+publicParametersPath+".");
			
			SimpleLogger.info("Loading private key...");
			key = new Waters08PrivateKey();
			key.initializeFromFile(publicParameters, keyPath);
			SimpleLogger.info("Private key loaded from "+keyPath+".");
			
			SimpleLogger.info("Loading trusted ticket authority keys...");
			trustedTAKeys = RSAKeyManager.readAllKeys(trustPath);
			SimpleLogger.info("Trusted keys loaded.");
			
			SimpleLogger.info("Loading ticket authority key (public)...");
			publicTAKey = RSAKeyManager.readKeyFromFile(new File(publicRSAPath));
			SimpleLogger.info("Public ticket authority key loaded.");
			
			SimpleLogger.info("Loading ticket authority key (private)...");
			privateTAKey = RSAKeyManager.readKeyFromFile(new File(privateRSAPath));
			SimpleLogger.info("Private ticket authority key loaded.");
			
			// Create threaded job pool
			jobPool = new ThreadPoolExecutor(numThreads, numThreads, 1, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(1000));
			
			this.port = port;
		} catch(Exception e){
			throw new InitializationException(e);
		}
	}
	
	/**
	 * Starts the master node, allowing it to serve indefinitely.
	 */
	public void serveForever() throws InitializationException{
		
		try{
			// Bind to port
			listener = new ServerSocket(port);
		} catch(Exception e){
			throw new InitializationException(e);
		}
		
		monitor = new AEFSMasterMonitor(this);
		
		Thread monitorThread = new Thread(monitor);
		// Start listening for requests
		monitorThread.start();
		
		SimpleLogger.info("AEFSMasterNode started on port "+port+".");
	}
	
	/**
	 * Stops the AEFSMasterNode.
	 */
	public void stop(){
		if(monitor != null){
			monitor.stop();
		}
		
		if(listener != null){
			try{
				listener.close();
			} catch(IOException e){
				// Do nothing.  This socket is no longer needed anyway.
			}
		}
		
		SimpleLogger.info("AEFSMasterNode stopped.");
	}
	
	/**
	 * Returns an active server socket for the master or null if
	 * there is none.
	 */
	protected ServerSocket getServerSocket(){
		return listener;
	}
	
	/**
	 * Returns the job pool being used by the master.
	 */
	protected ThreadPoolExecutor getJobPool(){
		return jobPool;
	}
	
	/**
	 * Provides a command line interface for starting an AEFSMasterNode.
	 */
	public static void main(String[] args){
		SimpleLogger.setSource("AEFSMasterNode");
		
		// Register command-line options
		Options options = new Options();
		options.addOption("help", "Prints help.");
		options.addOption("params_path", true, "Path from which public parameters should "+
				"be read and/or modified.");
		options.addOption("key_path", true, "Path from which the master's private key "+
				"should be read.");
		options.addOption("num_threads", true, "Number of threads that this node should "+
				"use to serve requests.");
		options.addOption("port", true, "Port on which to serve requests.");
		options.addOption("public_rsa_key", true, "Path to public RSA key.");
		options.addOption("private_rsa_key", true, "Path to private RSA key.");
		options.addOption("trust_path", true, "Path to directory containing trusted public ticket "
				+ "authority keys.");
		
		CommandLineParser parser = new DefaultParser();
		try{
			// Parse options
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("help")){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "AEFSMasterNode", options );
			} else{
				String paramsPath = cmd.getOptionValue("params_path");
				String keyPath = cmd.getOptionValue("key_path");
				String numThreadsStr = cmd.getOptionValue("num_threads");
				String portStr = cmd.getOptionValue("port");
				String trustPath = cmd.getOptionValue("trust_path");
				String publicRSAPath = cmd.getOptionValue("public_rsa_key");
				String privateRSAPath = cmd.getOptionValue("private_rsa_key");
				
				Integer numThreads = null;
				Integer port = null;
				
				if(paramsPath == null || keyPath == null || numThreadsStr == null ||
						portStr == null || trustPath == null || publicRSAPath == null ||
						privateRSAPath == null){
					SimpleLogger.error("Invalid parameters.  Please see '-help'.");
					System.exit(1);
				}
				
				try{
					numThreads = Integer.parseInt(numThreadsStr);
					port = Integer.parseInt(portStr);
				} catch(NumberFormatException e){
					SimpleLogger.error("num_threads and port must be numeric values.");
				}
				
				AEFSMasterNode master = new AEFSMasterNode(paramsPath, keyPath, numThreads,
						port, trustPath, publicRSAPath, privateRSAPath);
				
				master.serveForever();
				
				// Add shutdown hook to allow master to be properly stopped.
				Runtime.getRuntime().addShutdownHook(new Thread()
		        {
		            public void run(){
		            	master.stop();
		            }
		        });
			}
		} catch(Exception e){
			SimpleLogger.error("AEFSMasterNode unable to operate: "+e.
					getMessage());
		}
	}
}
