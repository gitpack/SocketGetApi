package org.shokeen.get.apis;
/*
 * HTTPRequest.java
 *
 * This class processes the following HTTP Requests.
 * 1. GET "http://localhost:9000/request?connId=12&timeout=100" : Open a connection with connection id and wait for operation for timeout milliseconds
 * 2. GET "http://localhost:9000/serverStatus" : List the available connections along with total time left for each one of them
 * 3. PUT "http://localhost:9000/kill" -data "connId=12" : Kills the connection with given connection id if connection is available otherwise 
 * 											return status as invalid connection
 */
import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author ShokeenAnil
 *
 */
public class HTTPRequest {
	private InputStream input;
	private String HTTPRequest;
	private StringBuffer request = new StringBuffer(2048);
	private String requestMethod;
	private String requestURI;
	private String requestProtocol;
	private String requestHostname;
	private String connId;

	public static Map<String, Long> paramMap = new ConcurrentHashMap<String, Long>();

	/** Creates a new instance of HTTPRequest */
	public HTTPRequest(InputStream in) {
		// The constructor simply gets passed the message stream from the Server
		// and stores this in a local variable for Parsing and processing
		input = in;
		parse();
	}

	public InputStream getInput() {
		// Get the input stream back (for debugging or logging purposes)
		return input;
	}

	private void parse() {
		// Read a set of characters from the socket
		// i.e. extract the HTTP request from the socket
		int i;
		byte[] buffer = new byte[2048];
		try {
			i = input.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			i = -1;
		}

		for (int j = 0; j < i; j++) {
			request.append((char) buffer[j]);
		}
		HTTPRequest = request.toString();
		System.out.println(HTTPRequest);
	}

	public String getHTTPRequest() {
		return HTTPRequest;
	}

	public int identifyRequest() {

		String api;

		String[] requestCols = HTTPRequest.split(" ");
		setMethod(requestCols[0]);

		System.out.println("Method :" + getMethod());

		setURI(requestCols[1]);
		System.out.println(getURI());

		if (requestURI.contains("?")) {
			api = requestURI.substring(0, requestURI.indexOf('?'));
		} else{
			api = requestURI;
		}
		System.out.println("API :" + api);
		setProtocol(requestCols[2].substring(0,requestCols[2].indexOf("Host")-1));
		System.out.println("http version:\t" + getProtocol());
		
		if (api.equals("/request")) {
			return 1;
		} else if (api.equals("/serverStatus")) {
			return 2;
		} else if (api.equals("/kill")) {
			return 3;
		}
			System.out.println("No match");
			return 0;
		
	}

	public int parseRequest() {

		String paramStr = requestURI.substring(requestURI.indexOf('?') + 1);
		String[] paramPairs = paramStr.trim().split("&");
		String connId = null;
		Long timeout = 0L;
		int time = 0;

		String[] paramKv;
		for (String paramPair : paramPairs) {
			if (paramPair.contains("=")) {
				paramKv = paramPair.split("=");
				if (paramKv[0].equals("connId")) {
					connId = paramKv[1];
				} else if (paramKv[0].equals("timeout")) {
					time = Integer.parseInt(paramKv[1]);
					timeout = time + System.currentTimeMillis();
				}
			}
		}
		paramMap.put(connId, timeout);
		return time;
	}

	public boolean parseServerStatus() {
		Long time = System.currentTimeMillis();
		if (paramMap.isEmpty()) {
			System.out.println("Map is empty");
			return false;
		}

		for (Entry<String, Long> entry : paramMap.entrySet()) {
			String key = entry.getKey().toString();
			Long value = entry.getValue();
			if (time > value) {
				paramMap.remove(key);
				System.out.println("Removed  key: " + key + " value: " + value);
			}
		}
		return true;
	}

	public boolean parseKill() {
		String[] values = (getHTTPRequest()
				.substring(request.indexOf("connId"))).split("=");
		System.out.println("Key of the connection to search is : " + values[1]);
		setConnId(values[1]);
		boolean isEmpty = !parseServerStatus();
		if (isEmpty) {
			System.out.println("No connections present now...");
			return false;
		} else {
			if (paramMap.isEmpty()) {
				System.out.println("All connections timed out..");
				return false;
			}
			System.out.println("Connections present...");
			if (paramMap.containsKey(getConnId())) {
				System.out.println("Key found..Removing");
				paramMap.remove(getConnId());
				System.out.println("After removing.. checking the key..."
						+ paramMap.get(getConnId()));
				return true;
			} else {
				System.out.println("All connections timed out..");
				return false;
			}
		}

	}

	public String getConnId() {
		return connId;
	}

	public void setConnId(String connId) {
		this.connId = connId;
	}

	public String getRequest() {
		return request.toString();
	}

	private void setMethod(String method) {
		requestMethod = method;
	}

	public String getMethod() {
		return requestMethod;
	}

	private void setURI(String uriin) {
		requestURI = uriin;
	}

	public String getURI() {
		return requestURI;
	}

	private void setProtocol(String protin) {
		requestProtocol = protin;
	}

	public String getProtocol() {
		return requestProtocol;
	}

	public String getHost() {
		return requestHostname;
	}
}
