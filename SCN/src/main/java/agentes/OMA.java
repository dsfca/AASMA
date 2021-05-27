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
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;
import general.Material;

public class OMA extends Thread {

	private IniManager ini;
	
	private ServerSocket ssocket;
	/*private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	private Socket imaSocket;
	private ObjectOutputStream imaObjectOutputStream;
	private ObjectInputStream imaObjectInputStream;
	
	private Socket ppaSocket;
	private ObjectOutputStream ppaObjectOutputStream;
	private ObjectInputStream ppaObjectInputStream;
	*/
	//performance
	private int requestsReceived;
	private int requestsDelivered;
	private int datasCumpridas;
	
	private int id;
	
	public OMA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getOMAServerPort());
		this.id = 0;
		/**
		this.imaSocket = (Socket)new Socket(ini.getIMAHost(), ini.getIMAServerPort());
		this.imaObjectOutputStream = new ObjectOutputStream(this.imaSocket.getOutputStream());
		this.imaObjectInputStream = new ObjectInputStream(this.imaSocket.getInputStream());
		
		this.ssocket = new ServerSocket(ini.getOMAServerPort());
		*/
		/*this.ppaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
		this.ppaObjectOutputStream = new ObjectOutputStream(this.ppaSocket.getOutputStream());
		this.ppaObjectInputStream = new ObjectInputStream(this.ppaSocket.getInputStream());*/
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	//CUIDADO COM OS OOS OIS PARA VARIOS CLIENTES
	public void run() {
		try {
			//FROM CLIENTS
			int my_id = ++id;
			Socket generalSocket = ssocket.accept();
			newListener();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(generalSocket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(generalSocket.getInputStream());
			
			Socket imaSocket = (Socket) new Socket(ini.getIMAHost(), ini.getIMAServerPort());
			ObjectOutputStream imaObjectOutputStream = new ObjectOutputStream(imaSocket.getOutputStream());
			ObjectInputStream imaObjectInputStream = new ObjectInputStream(imaSocket.getInputStream());
			System.out.println("OMA: connected streams successfully");
			
			/*Socket ppaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
			ObjectOutputStream ppaObjectOutputStream = new ObjectOutputStream(ppaSocket.getOutputStream());
			ObjectInputStream ppaObjectInputStream = new ObjectInputStream(ppaSocket.getInputStream());
			*/
			Pedido pedido = (Pedido) objectInputStream.readObject();
			System.out.println("OMA" + my_id + ": " + pedido.toString());
			
			//ENVIAR IMA
			/*Check*/
			Object [] object = {"ci", pedido.getMateriais()};
			imaObjectOutputStream.writeObject(object);
			HashMap <Material, Integer> quantidades = (HashMap<Material, Integer>) imaObjectInputStream.readObject();
			System.out.println(quantidades);
			//(ESTIMAR DATA ENTREGA
			pedido.setDataLimite(new Timestamp(System.currentTimeMillis()+10000));
			//(ENVIAR PPA
			/**ppaObjectOutputStream.writeObject(pedido);*/
			//(RECEBER RESPOSTA
			//Produto produto_final = objectInputStream.readObject();
			//(RESPONDER CLIENTE (SERIA GIRO UMA INTERFACE COM OS CLIENTES EM ESPERA?)
			/**objectOutputStream.writeObject(object);*/
			System.out.println("OMA" + my_id + ": Terminou");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void receiveFromClient() {
		
	}
	
	/*public void enviarPedidoPPA(Pedido pedido) throws IOException {
		this.objectOutputStream.writeObject(pedido);
	}*/
	
	//PARA ESTIMAR DATA
	/*public HashMap<Produto, Integer> queryIMAavailability(Pedido pedido) throws IOException, ClassNotFoundException {
		HashMap <Produto, Integer> quantidades = new HashMap<Produto, Integer>();
		
		for (Produto produto: pedido.getProdutos()) {
			this.imaObjectOutputStream.writeObject(produto);
			int quantidade = (Integer) this.imaObjectInputStream.readObject();
			System.out.println(quantidade);
			quantidades.put(produto, quantidade);
		}
		return quantidades;
	}*/
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
