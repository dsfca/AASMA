package general;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

import org.ini4j.InvalidFileFormatException;

public class Client extends Thread{
	
	private static int PORT;
	private IniManager ini;
	private int id;
	
	private Socket clientSocket;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	private Pedido pedido;

	
	public Client(int id, Pedido pedido) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.PORT = ini.getClientPort();
		this.id = id;
		this.pedido = pedido;
	}
	
	public void run() {
		try {
			startConnection();
			enviarPedido("X");
			stopConnection();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public void startConnection() throws IOException {
		System.out.println("HMM");
		this.clientSocket = (Socket)new Socket(ini.getOMAHost(), ini.getOMAServerPort());
		
		this.objectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
		this.objectInputStream = new ObjectInputStream(this.clientSocket.getInputStream());

		System.out.println("Coneccao iniciada lado cliente"+this.id);
	}

	public void enviarPedido(String pedido) throws IOException, ClassNotFoundException {
        //out.println(pedido);
		this.objectOutputStream.writeObject(this.pedido);
        System.out.println("Cliente "+this.id+" a aguardar pedido");
        String r = (String) objectInputStream.readObject();
        System.out.println(r);
    }
	
	public void stopConnection() throws IOException {
		this.objectInputStream.close();
		this.objectOutputStream.close();
        this.clientSocket.close();
    }

/**
	public void fazerPedido(String message) {
		try {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			OutputStream output = socket.getOutputStream();

			String total_message = message;
			String encodedString = Base64.getEncoder().encodeToString(total_message.getBytes());

			PrintWriter writer = new PrintWriter(output, true);
			writer.println(encodedString);

		} catch(IOException e) {
			e.printStackTrace();
		}
	}
**/
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
