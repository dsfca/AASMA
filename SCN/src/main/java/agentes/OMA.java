package agentes;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;

public class OMA extends Thread {

	private IniManager ini;
	private Socket socket;
	private ServerSocket ssocket;
	
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	
	public OMA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getOMAServerPort());
		//this.socket = (Socket)new Socket(ini.getOMAHost(), ini.getOMAClientPort());
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	
	public void run() {
		try {
			Socket connection = ssocket.accept();
			System.out.println("Coneccao lado servidor");
			newListener();
			this.objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
			this.objectInputStream = new ObjectInputStream(connection.getInputStream());
			Pedido pedido = (Pedido) objectInputStream.readObject();
			System.out.println(pedido.toString());
			/*this.out = new PrintWriter(connection.getOutputStream(), true);
	        this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        
	        String pedido = in.readLine();
	        System.out.println(pedido);*/
	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*	
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
*/
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
