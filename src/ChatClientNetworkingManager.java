import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class ChatClientNetworkingManager extends Thread{

	MessageHandler messageHandler; //Interface to communicate with GUI

	int listeningPort;

	ServerSocket serverSocket;
	Socket connection;

	Server server; //On its own Thread

	ChatClientNetworkingManager(MessageHandler messageHandler)
	{
		this.messageHandler = messageHandler;
	}

	ChatClientNetworkingManager(MessageHandler messageHandler, int listeningPort)
	{
		this.messageHandler = messageHandler;
		initListeningServer(listeningPort);
	}

	public void initListeningServer(int listeningPort)
	{
		this.listeningPort = listeningPort;
		server = new Server();
		server.start();
	}

	public String getSender()
	{
		return server.getConnectionIP();
	}

	public void sendMessage(String message, String ip, int port)
	{
		new Messenger(message, ip, port).start();
	}

	private class Messenger extends Thread
	{
		String message;
		String ip;
		int port;
		public Messenger(String message, String ip, int port)
		{
			this.message = message;
			this.ip = ip;
			this.port = port;
		}
		public void run()
		{
			try {
				connection = new Socket(ip, port);
				PrintWriter writer = new PrintWriter(connection.getOutputStream());
				writer.println(message);
				writer.flush();
				connection.close();
			} catch (IOException e) {
				System.err.println("Failed to send Message to: " + ip + ":" + port);
				e.printStackTrace();
			}
		}
	}

	private class Server extends Thread
	{
		public Socket connection;

		public Server()
		{
			initNewServerSocket();
		}
		public void initNewServerSocket()
		{
			try {
				serverSocket = new ServerSocket(listeningPort);
			} catch (IOException e){
				System.err.println("Failed to Create Server Socket at Port: " + listeningPort);
				e.printStackTrace();
			}
		}
		public void run()
		{
			try{
				while((connection = serverSocket.accept()) != null)
				{
					BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String message = reader.readLine();
					if(message != null)
					{
						messageHandler.handleMessage(message);
					}
				}
			}catch (IOException e){
				System.err.println("Server failed to form a connection");
				e.printStackTrace();
			}
		}
		public String getConnectionIP()
		{
			return connection.getLocalAddress().getHostName();
		}
	}
}
