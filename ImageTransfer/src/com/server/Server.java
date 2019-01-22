package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.server.ClientHandler;
import com.server.Server;

public class Server {
	private ServerSocket ss;
	private boolean running;
	public Server(int port) {
		try {
			ss = new ServerSocket(port);
			running = true;
			start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void start() {
		System.out.println("Starting server");
		Socket connectionToClient = null;
		while(running) { //creates and keeps the server running
			try {
				connectionToClient = ss.accept();
				System.out.println("New client");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ClientHandler handler = new ClientHandler(connectionToClient);
			Thread clientThread = new Thread(handler);
			clientThread.start();
			
			
		}
	}
	public static void main(String[] args) {
		Server s = new Server(7451);
	}
}
