package org.shokeen.get.apis;
/*
 * Class responsible for initiating the server socket and start listening on the given port.
 * 
 * Creates threads for operation requests to be handled by worker objects.
 * 
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * 
 * @author ShokeenAnil
 *
 */

public class Server implements Runnable {

	protected int serverPort;
	protected ServerSocket serverSocket = null;
	protected boolean isStopped = false;
	protected Thread runningThread = null;

	public Server(int port) {
		this.serverPort = port;
	}

	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while (!isStopped()) {
			Socket clientSocket = null;
			try {
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				if (isStopped()) {
					System.out.println("Server Stopped.");
					return;
				}
				throw new RuntimeException("Error accepting client connection",
						e);
			}
			new Thread(new Worker(clientSocket, serverPort)).start();

		}
		System.out.println("Server Stopped.");

	}

	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop() {
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	private synchronized void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port " + serverPort, e);
		}
	}

}