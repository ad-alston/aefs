package aefs.nodes.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

import misc.logging.SimpleLogger;
import misc.random.SecureRandomBank;

/**
 * Simple worker which listens for incoming connections
 * to the master.
 */
public class AEFSMasterMonitor implements Runnable {
	
	private boolean stopped;
	private AEFSMasterNode master;
	
	public AEFSMasterMonitor(AEFSMasterNode master){
		this.master = master;
		stopped = false;
	}
	
	@Override
	public void run(){
		ServerSocket listener = master.getServerSocket();
		ThreadPoolExecutor pool = master.getJobPool();
		
		// Respond to requests until server socket closed or 
		// monitor stopped.
		while(!stopped){
			try{
				Socket client = listener.accept();
				pool.execute(new AEFSMasterWorker(client, master));
			} catch(IOException e){
				SimpleLogger.info("AEFSMasterNode server socket closed.  "+
						"No longer monitoring for requests.");
				break;
			}
		}
	}
	
	/**
	 * Stops the connection monitor.
	 */
	public void stop(){
		this.stopped = false;
	}
}
