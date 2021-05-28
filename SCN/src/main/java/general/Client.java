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
import java.net.ServerSocket;
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
	
	private ServerSocket ssocket;
	
	private Pedido pedido;

	
	public Client(int id, Pedido pedido) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.id = id;
		this.PORT = ini.getClientPort() + this.id;
		this.pedido = pedido;
		this.ssocket = new ServerSocket(this.PORT);
	}
	
	public void run() {
		try {
			startConnection();
			enviarPedido();
			stopConnection();
			waitPedido();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public void startConnection() throws IOException {
		this.clientSocket = (Socket)new Socket(ini.getOMAHost(), ini.getOMAServerPort());
		this.objectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
		this.objectInputStream = new ObjectInputStream(this.clientSocket.getInputStream());
	}

	public void enviarPedido() throws IOException, ClassNotFoundException {
		Object [] object = {"c", this.pedido};
		this.objectOutputStream.writeObject(object);
        String r = (String) objectInputStream.readObject();
        System.out.println("Cliente" + this.id + r);
    }
	
	public void stopConnection() throws IOException {
		this.objectInputStream.close();
		this.objectOutputStream.close();
        this.clientSocket.close();
    }

	public void waitPedido() throws IOException, ClassNotFoundException {
		Socket generalSocket = this.ssocket.accept();
		
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(generalSocket.getOutputStream());
		ObjectInputStream objectInputStream = new ObjectInputStream(generalSocket.getInputStream());
		//RECEBER PEDIDO
		Pedido pedido = (Pedido) objectInputStream.readObject();
		//ENVIAR DINHEIRO
		int total_price = pedido.getTotalPrice();
		objectOutputStream.writeObject(total_price);
		objectInputStream.close();
		objectOutputStream.close();
		generalSocket.close();
		System.out.println("CLIENTE RECEBEU!!!!");
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
