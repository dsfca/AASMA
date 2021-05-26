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
import java.util.HashMap;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;
import general.Produto;

public class OMA extends Thread {

	private IniManager ini;
	
	private Socket imaSocket;
	private ObjectOutputStream imaObjectOutputStream;
	private ObjectInputStream imaObjectInputStream;
	
	private ServerSocket ssocket;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	
	public OMA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		
		this.imaSocket = (Socket)new Socket(ini.getIMAHost(), ini.getIMAServerPort());
		this.imaObjectOutputStream = new ObjectOutputStream(this.imaSocket.getOutputStream());
		this.imaObjectInputStream = new ObjectInputStream(this.imaSocket.getInputStream());
		
		this.ssocket = new ServerSocket(ini.getOMAServerPort());
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	//CUIDADO COM OS OOS OIS PARA VARIOS CLIENTES
	public void run() {
		try {
			Socket clientSocket = ssocket.accept();
			System.out.println("OMA: started");
			newListener();
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
			Pedido pedido = (Pedido) objectInputStream.readObject();
			System.out.println("OMA: " + pedido.toString());
			
			HashMap <Produto, Integer> quantidades = queryIMAavailability(pedido);
			System.out.println(quantidades);
			//(ESTIMAR DATA ENTREGA)
			
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
	
	public void receiveFromClient() {
		
	}
	
	public void enviarPedidoPPA(Pedido pedido) throws IOException {
		this.objectOutputStream.writeObject(pedido);
	}
	
	//PARA ESTIMAR DATA
	public HashMap<Produto, Integer> queryIMAavailability(Pedido pedido) throws IOException, ClassNotFoundException {
		HashMap <Produto, Integer> quantidades = new HashMap<Produto, Integer>();
		
		for (Produto produto: pedido.getProdutos()) {
			this.imaObjectOutputStream.writeObject(produto);
			int quantidade = (Integer) this.imaObjectInputStream.readObject();
			System.out.println(quantidade);
			quantidades.put(produto, quantidade);
		}
		return quantidades;
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
