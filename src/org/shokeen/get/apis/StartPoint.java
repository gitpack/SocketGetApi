package org.shokeen.get.apis;
/*
 * Starting class of the project.
 * Initiates the thread to start the server and listen on port 9000. 
 * If needed, Port number can be changed to desired by taking input from user.
 * 
 * Server can be stopped at any time by entering q or Q in console.
 * 
 */
import java.util.Scanner;

/**
 * 
 * @author ShokeenAnil
 *
 */
public class StartPoint {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String choice;
		Server server = new Server(9000);
		Thread th = new Thread(server);
		th.start();
		System.out.println("press Q or q to stop the server...");
		choice = in.next();
		if (choice.equals("q") || choice.equals("Q")) {
			System.out.println("Stopping Server");
			th.interrupt();
			server.stop();
		}
		in.close();

	}
}
