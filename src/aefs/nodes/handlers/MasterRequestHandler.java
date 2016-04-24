package aefs.nodes.handlers;

import java.io.IOException;

import aefs.nodes.master.AEFSMasterWorker;
import aefs.protocols.requests.ClientRequest;

/**
 * Handles requests received from a client by a master node.
 */
public abstract class MasterRequestHandler {

	/**
	 * Handles the request.
	 * @param request request to handle
	 * @param worker worker handling the request
	 */
	public abstract void handle(ClientRequest request, AEFSMasterWorker worker)
		throws IOException;
	
}
