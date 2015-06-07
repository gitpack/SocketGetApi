package org.shokeen.get.apis;

/*This worker handles following HTTP Requests and send HTTP response to the client.
 * 1. GET "http://localhost:9000/request?connId=12&timeout=100" : Open a connection with connection id and wait for operation for timeout milliseconds
 * 2. GET "http://localhost:9000/serverStatus" : List the available connections along with total time left for each one of them
 * 3. PUT "http://localhost:9000/kill" -data "connId=12" : Kills the connection with given connection id if connection is available otherwise 
 * 											return status as invalid connection.
 * 
 */
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map.Entry;

/**
 * 
 * @author ShokeenAnil
 *
 */

public class Worker implements Runnable {

	protected Socket clientSocket = null;
	protected int serverPort;
	protected int identifier;
	protected int timeout;
	protected StringBuffer response = new StringBuffer(2048);

	public Worker(Socket clientSocket, int serverPort) {
		this.clientSocket = clientSocket;
		this.serverPort = serverPort;
	}

	// This method will parse the HTTP request and determine if it is a valid
	// request and then extract
	// the various elements of interest for use by the server.
	// These will be accessed through appropriate methods rather than directly.
	public void run() {
		try {
			InputStream input = clientSocket.getInputStream();
			OutputStream output = clientSocket.getOutputStream();
			int no_of_instances = 0;
			long time = System.currentTimeMillis();
			HTTPRequest request = new HTTPRequest(input);
			identifier = request.identifyRequest();

			if (identifier == 1) {
				System.out.println("Process handling for GET /request");
				// need timeout for putting request to process for that time
				// After time, response will contain "status":"ok"
				// /request.parseEntireRequest(identifier);
				timeout = request.parseRequest();
				// System.out.println(System.currentTimeMillis());

				Thread.sleep(timeout);
				// System.out.println(System.currentTimeMillis());
				response.append("{\"Status\":\"ok\"}");

			} else if (identifier == 2) {
				System.out.println("Process handling for GET /serverStatus");
				// returns all the running requests on the server with their
				// time left for completion.
				// needs conn id and timeouts for sending them to response
				// remove all the connections whose time has passed
				response.append("{");
				if (request.parseServerStatus()) {
					for (Entry<String, Long> entry : HTTPRequest.paramMap
							.entrySet()) {
						// System.out.println("inside loop");
						String key = entry.getKey().toString();
						Long value = entry.getValue();
						no_of_instances++;
						response.append("\"" + key + "\":\"" + (value - time)
								/ 1000 + "\"");
						if (no_of_instances < HTTPRequest.paramMap.size()) {
							response.append(",");
						}
					}
				} else {
					System.out.println("No connection currently");
					response.append("");
				}
				response.append("}");

			} else if (identifier == 3) {
				System.out.println("Process handling for PUT /kill");
				// payload will be passed and will contain conn id... if the
				// conn id is there in map and still time left then kill it and
				// return status killed
				// parent request will return status ok
				// if no request found, return conn id not
				// available..."status":"invalid connection id:conn id"

				if (!request.parseKill()) {
					response.append("{\"Status\":\"Invalid Connection Id : \""
							+ request.getConnId() + "\"}");
				} else {
					response.append("{\"Status\":\"ok\"}");
				}
			} else {
				System.out.println("Bad request");
				response.append("Bad Request");
			}

			output.write(("HTTP/1.1 200 OK\n\n" + response + "\n").getBytes());

			output.close();
			input.close();
		} catch (IOException e) {
			// report exception somewhere.
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}