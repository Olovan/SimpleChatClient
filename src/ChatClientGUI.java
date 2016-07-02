import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClientGUI extends JFrame implements MessageHandler {
	private final Dimension WINDOW_SIZE = new Dimension(640, 800);
	private final Dimension PORT_INFO_SIZE = new Dimension(640, 20);

	ChatClientNetworkingManager netMan;

	JTextPane chatArea;
	StyledDocument styleDoc;
	StyleContext context = new StyleContext();
	Style defaultStyle;
	Style right;
	Style left;

	JTextArea inputMessageArea;

	JTextField iPField;
	JTextField incomingPortField;
	JTextField outgoingPortField;

	JButton listenButton;
	JButton sendButton;

	public ChatClientGUI()
	{
		//Set up Frame
		setTitle("Basic Chat Client v0.1");
		setMinimumSize(WINDOW_SIZE);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setContentPane(panel);

		//Add stuff
		add(new PortInfoPanel());
		add(new ChatPanel());
		add(new MessagePanel());

		//Initialize Network Manager
		netMan = new ChatClientNetworkingManager(this, Integer.parseInt(incomingPortField.getText()));

		setVisible(true);
	}

	public void printMessage(String message, Boolean ourMessage)
	{
		try {
			int currentLength = styleDoc.getLength();
			int messageLength = message.length();
			if(ourMessage)
			{
				styleDoc.setLogicalStyle(currentLength, right);
				styleDoc.insertString(currentLength, message, right);
			}
			else
			{
				styleDoc.setLogicalStyle(currentLength, left);
				styleDoc.insertString(currentLength, message, left);
			}
		} catch (BadLocationException e) {
			System.err.println("Printed to Bad location");
		}

	}

	//Private Class to Hold top IP and Port Information
	private class PortInfoPanel extends JPanel
	{
		private final Dimension MAX_SIZE = new Dimension(6400, 25);
		private final Dimension MAX_LISTEN_BUTTON_SIZE = new Dimension(80, 25);
		private final Dimension MAX_INCOMING_PORT_SIZE = new Dimension(50, 25);
		private final Dimension MAX_IP_SIZE = new Dimension(200, 25);
		private final Dimension MAX_OUTGOING_PORT_SIZE = new Dimension(50, 25);

		public PortInfoPanel()
		{
			//Set up Panel
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setMinimumSize(PORT_INFO_SIZE);
			setPreferredSize(PORT_INFO_SIZE);
			setMaximumSize(new Dimension(6400, 25));

			//Instantiate
			listenButton = new JButton("Listen");
			incomingPortField = new JTextField("8888");
			iPField = new JTextField("localhost");
			outgoingPortField = new JTextField("8888");

			//Set Comp Settings
			listenButton.setMaximumSize(MAX_LISTEN_BUTTON_SIZE);
			incomingPortField.setMaximumSize(MAX_INCOMING_PORT_SIZE);
			iPField.setMaximumSize(MAX_IP_SIZE);
			outgoingPortField.setMaximumSize(MAX_OUTGOING_PORT_SIZE);
			listenButton.setMinimumSize(MAX_LISTEN_BUTTON_SIZE);
			incomingPortField.setMinimumSize(MAX_INCOMING_PORT_SIZE);
			iPField.setMinimumSize(MAX_IP_SIZE);
			outgoingPortField.setMinimumSize(MAX_OUTGOING_PORT_SIZE);
			listenButton.setPreferredSize(MAX_LISTEN_BUTTON_SIZE);
			incomingPortField.setPreferredSize(MAX_INCOMING_PORT_SIZE);
			iPField.setPreferredSize(MAX_IP_SIZE);
			outgoingPortField.setPreferredSize(MAX_OUTGOING_PORT_SIZE);


			//Add items
			add(listenButton);
			add(Box.createHorizontalStrut(5));
			add(incomingPortField);
			add(Box.createHorizontalGlue());
			add(iPField);
			add(Box.createHorizontalStrut(5));
			add(outgoingPortField);
		}
	}

	private class ChatPanel extends JPanel
	{
		private final Dimension CHAT_MIN_SIZE = new Dimension(640, 500);
		//private final Dimension CHAT_MAX_SIZE = new Dimension(640, 500);

		public ChatPanel()
		{
			//Set up Panel
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setMinimumSize(CHAT_MIN_SIZE);
			setBorder(BorderFactory.createEmptyBorder(5,0,5,0));

			//Instantiate Components
			chatArea = new JTextPane();
			chatArea.setEditable(false);
			chatArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			JScrollPane scroll = new JScrollPane(chatArea);
			styleDoc = chatArea.getStyledDocument();
			context = StyleContext.getDefaultStyleContext();

			add(scroll);
		}
	}
	private class MessagePanel extends JPanel 
	{
		private final Dimension MESSAGE_MIN_SIZE = new Dimension(640, 200);
		private final Dimension MESSAGE_MAX_SIZE = new Dimension(6400, 200);


		public MessagePanel()
		{
			//Set Panel Settings
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setMinimumSize(MESSAGE_MIN_SIZE);
			setMaximumSize(MESSAGE_MAX_SIZE);
			setPreferredSize(MESSAGE_MIN_SIZE);

			//Instantiate
			inputMessageArea = new JTextArea();
			inputMessageArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

			//Set up Listeners
			inputMessageArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), new InputMessageAreaActionListener());

			//Set up Style Stuff
			defaultStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
			left = styleDoc.addStyle("left", defaultStyle);
			StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
			right = styleDoc.addStyle("right", defaultStyle);
			StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);

			//Add components
			add(inputMessageArea);
		}


		//Listener for inputMessageArea to send a message every time you press ENTER
		private class InputMessageAreaActionListener extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
			{

				String message = inputMessageArea.getText();
				inputMessageArea.setText("");
				printMessage(".::You::.\n", true);
				printMessage(message + "\n\n", true);

				netMan.sendMessage(message, iPField.getText(), Integer.parseInt(outgoingPortField.getText()));
			}
		}
	}

	@Override
	public void handleMessage(String message)
	{
		printMessage(".::Guest:" + netMan.getSender() + "::. \n", false);
		printMessage(message + "\n\n", false);
	}
}
