import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class ChatClientNetworkingManager extends Thread{
	private final int MAX_MESSAGE_LENGTH = 90;
	private final String KEY = "lXEIJkNKIk2kfaBv";

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

	//PRIVATE CLASSES
	//Messenger Class Sends outgoing Messages
	//Server class receives and proccesses Messages
	private class Messenger extends Thread
	{
		Cipher cipher;
		String message;
		String ip;
		int port;
		public Messenger(String message, String ip, int port)
		{
			try {
				this.message = message + "\n";
				this.ip = ip;
				this.port = port;
				cipher = Cipher.getInstance("AES");
				byte[] keyBytes = KEY.getBytes();
				SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
				cipher.init(Cipher.ENCRYPT_MODE, keySpec);

			} catch(Exception e){
				e.printStackTrace();
			}
		}
		public void run()
		{
			try {
				connection = new Socket(ip, port);
				DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
				byte[] encryptedBytes = encrypt(message);
				writer.write(encryptedBytes, 0, encryptedBytes.length);
				writer.flush();
				connection.close();
			} catch (IOException e) {
				System.err.println("Failed to send Message to: " + ip + ":" + port);
				System.err.println(e.getLocalizedMessage());
			}
		}
		private byte[] encrypt(String inputString)
		{
			byte[] encryptedBytes = null;
			try {
				encryptedBytes = cipher.doFinal(inputString.getBytes());
			} catch (Exception e) {
				System.err.println("encrypt method failed");
			}
			return encryptedBytes;
		}
	}
	private class Server extends Thread
	{
		public Socket connection;
		private Cipher cipher;

		public Server()
		{
			//Set up Crypto stuff
			try {
				cipher = Cipher.getInstance("AES");
				byte[] keyBytes = KEY.getBytes();
				SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
				cipher.init(Cipher.DECRYPT_MODE, keySpec);
			} catch(Exception e){
				e.printStackTrace();
			}
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
					DataInputStream reader = new DataInputStream(connection.getInputStream());
					byte[] encryptedInput = new byte[16 * 513]; //Able to hold max string size
					String decryptedMessage;
					messageHandler.printName(false);
					int lengthRead = reader.read(encryptedInput);
					byte[] formattedBytes = new byte[lengthRead]; //Contains correctly sized array so encryption algorithm doesn't get cranky
					for(int i = 0; i < lengthRead; i++)
					{
						formattedBytes[i] = encryptedInput[i];
					}
					decryptedMessage = decrypt(formattedBytes);
					messageHandler.handleMessage(decryptedMessage);
				}
			}catch (IOException e){
				e.printStackTrace();
				System.err.println("Server Socket Closed");
			}
		}
		public String decrypt(byte[] encryptedBytes)
		{
			byte[] decryptedBytes = null;
			try {
				decryptedBytes = cipher.doFinal(encryptedBytes);
			} catch (Exception e) {
				System.err.println("decrypt method failed");
				e.printStackTrace();
			}
			return new String(decryptedBytes);
		}
		public String getConnectionIP()
		{
			return connection.getInetAddress().getHostName();
		}
	}
}
