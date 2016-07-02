import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class ChatClientNetworkingManager extends Thread{
	private final int MAX_MESSAGE_LENGTH = 90;

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
		//Shut down socket if it's already running
		if(serverSocket != null)
		{
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.err.println("Couldn't close ServerSocket");
			}
		}

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
				int messageCharsSent = 0;
				while(messageCharsSent < message.length())
				{
					if(message.length() - messageCharsSent > MAX_MESSAGE_LENGTH)
					{
						writer.write(message, messageCharsSent, MAX_MESSAGE_LENGTH);
						messageCharsSent += MAX_MESSAGE_LENGTH;
						writer.println();
						writer.flush();

					}
					else
					{
						writer.write(message, messageCharsSent, message.length() - messageCharsSent);
						messageCharsSent += message.length() -messageCharsSent;
					}
				}
				writer.print("\n");
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
				System.out.println("Made new Server at Port: " + listeningPort);
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
					String message;
					messageHandler.printName();
					while((message = reader.readLine()) != null)
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
			return connection.getInetAddress().getHostName();
		}
	}
}
