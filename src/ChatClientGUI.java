import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClientGUI extends JFrame implements MessageHandler {
	private final Dimension WINDOW_SIZE = new Dimension(640, 800);
	private final Dimension PORT_INFO_SIZE = new Dimension(640, 20);
	private final int MAX_MESSAGE_SIZE = 512;
	//private final int MAX_MESSAGE_LINE_LENGTH = 87; No longer needed as line wrapping mysteriously decided to turn on

	ChatClientNetworkingManager netMan;

	JTextPane chatArea;
	StyledDocument styleDoc; //Styled Doc of chatArea used to insert Strings using Styles

	Style defaultStyle;
	Style nameRight;
	Style right;
	Style nameLeft;
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
		setTitle("Basic Chat Client v1");
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

		pack();
		setVisible(true);
	}

	//prints message on the Right side of the chatArea if rightAlignment is true
	//and Left side of the chatArea if it's false
	public void printMessage(String message, Boolean rightAlignment)
	{
		if(rightAlignment)
			printMessage(message, right);
		else
			printMessage(message, left);
	}

	//Prints message in ChatArea using Style provided
	public void printMessage(String message, Style style)
	{
		int currentLength = styleDoc.getLength();
		try {
			styleDoc.setLogicalStyle(currentLength, style);
			styleDoc.insertString(currentLength, message, style);
		} catch(BadLocationException e){
			System.err.print("Printed to Bad location in PrintMessage method\n");
		}
	}

	//Private Class to Hold top IP and Port Information
	private class PortInfoPanel extends JPanel implements ActionListener
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

			//Add Listeners
			listenButton.addActionListener(this);


			//Add items
			add(listenButton);
			add(Box.createHorizontalStrut(5));
			add(incomingPortField);
			add(Box.createHorizontalGlue());
			add(iPField);
			add(Box.createHorizontalStrut(5));
			add(outgoingPortField);
		}

		//Button Listener for "LISTEN" button
		public void actionPerformed(ActionEvent e)
		{
			netMan.initListeningServer(Integer.parseInt(incomingPortField.getText()));
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
			JScrollPane scroll = new JScrollPane(chatArea);
			styleDoc = chatArea.getStyledDocument();

			//Adjust Component settings
			chatArea.setEditable(false);
			chatArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			chatArea.setPreferredSize(CHAT_MIN_SIZE);
			scroll.setPreferredSize(CHAT_MIN_SIZE);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			DefaultCaret caret = new DefaultCaret();
			chatArea.setCaret(caret);
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); //Make it always scroll to the bottom

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
			setPreferredSize(MESSAGE_MIN_SIZE);

			//Instantiate
			inputMessageArea = new JTextArea();
			inputMessageArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			inputMessageArea.setLineWrap(true);
			inputMessageArea.setWrapStyleWord(false);

			//Component Settings
			inputMessageArea.setPreferredSize(new Dimension(620, 200));

			//Set up Listeners
			inputMessageArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), new InputMessageAreaActionListener());

			//Set up Style Stuff
			defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
			left = styleDoc.addStyle("left", defaultStyle);
			right = styleDoc.addStyle("right", defaultStyle);
			nameLeft = styleDoc.addStyle("nameLeft", left);
			nameRight = styleDoc.addStyle("nameRIght", right);
			StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
			StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
			StyleConstants.setForeground(nameLeft, Color.RED);
			StyleConstants.setForeground(nameRight, Color.BLUE);
			StyleConstants.setBold(nameLeft, true);
			StyleConstants.setBold(nameRight, true);

			//Add components
			add(inputMessageArea);
		}

		//Cuts message down to MAX_MESSAGE_SIZE
		public String formatMessage(String message)
		{
			//remove whitespace and cut message down to MAX_MESSAGE_SIZE
			String result = message.trim();
			if(result.length() > MAX_MESSAGE_SIZE)
				result = result.substring(0, MAX_MESSAGE_SIZE);

			return result;
		}

		//Listener for inputMessageArea to send a message every time you press ENTER
		private class InputMessageAreaActionListener extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
			{
				String message = inputMessageArea.getText();
				inputMessageArea.setText("");
				String formattedMessage = formatMessage(message);
				if(formattedMessage.isEmpty())
					return;
				printName(true);
				printMessage(formattedMessage + "\n\n", true);
				netMan.sendMessage(formattedMessage, iPField.getText(), Integer.parseInt(outgoingPortField.getText()));
			}
		}
	}
	//Print Name using nameRight and or nameLeft styles
	@Override
	public void printName(Boolean isYou)
	{
		if(isYou)
			printMessage(".::You::.\n", nameRight);
		else
			printMessage(".::" + netMan.getSender() + "::. \n", nameLeft);
	}

	@Override
	public void handleMessage(String message)
	{
		printMessage(message + "\n", false);
	}
}
