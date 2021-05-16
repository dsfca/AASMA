import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

import org.ini4j.InvalidFileFormatException;

public class OMA implements Runnable {
	
	private static int SERVER_PORT;
	
	private IniManager ini;
	private Socket socket;
	private ServerSocket ssocket;

	public OMA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.SERVER_PORT = ini.getOMAServerPort();
		this.ssocket = new ServerSocket(SERVER_PORT);
		this.socket = (Socket)new Socket(ini.getOMAhost(), ini.getOMAClientPort());
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	
	public void run() {
		try {
			Socket connection = ssocket.accept();
			newListener();
			DataInputStream dis = new DataInputStream(connection.getInputStream());
			int message_lenght = dis.readInt();
			byte[] binary_message = new byte[message_lenght];
			dis.readFully(binary_message, 0, binary_message.length); // read the message

			System.out.println("recebido binario com " + binary_message.length + " bytes");
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) {
        try {
        	OutputStream output = socket.getOutputStream();
        	
        	String total_message = message;
        	String encodedString = Base64.getEncoder().encodeToString(total_message.getBytes());
        	
        	PrintWriter writer = new PrintWriter(output, true);
        	writer.println(encodedString);
            
        } catch(IOException e) {
            e.printStackTrace();
        }

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
