package agentes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;

public class MPA extends Thread{
	
	private IniManager ini;
	
	private Socket supplierSocket;
	private ObjectOutputStream supplierObjectOutputStream;
	private ObjectInputStream supplierObjectInputStream;
	
	private Socket ppaSocket;
	private ObjectOutputStream ppaObjectOutputStream;
	private ObjectInputStream ppaObjectInputStream;
	
	private ServerSocket ssocket;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	private List<Pedido> queue;
	private boolean new_deliveries;
	private long id = -1;
	
	public MPA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		
		//this.supplierSocket = (Socket)new Socket(ini.getSupplierHost(), ini.getSupplierServerPort());
		//this.supplierObjectOutputStream = new ObjectOutputStream(this.supplierSocket.getOutputStream());
		//this.supplierObjectInputStream = new ObjectInputStream(this.supplierSocket.getInputStream());
		
		this.ssocket = new ServerSocket(ini.getMPAServerPort());
	}
	
	private void newListener(){
		(new Thread(this)).start();
	}
	
	
	public void run() {
		try {
			
			Socket supplierSocket = ssocket.accept();
			System.out.println("MPA: started");
			newListener();
			
			
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(supplierSocket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(supplierSocket.getInputStream());
			Pedido pedido = (Pedido) objectInputStream.readObject();
			queue.add(pedido);
			System.out.println("MPA: " + pedido.toString());
			
			sendOrder(pedido);
			
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void sendOrder(Pedido pedido) throws IOException, ClassNotFoundException {
		this.supplierObjectOutputStream.writeObject(pedido);
		Pedido completed = (Pedido) this.supplierObjectInputStream.readObject();
		
		//Send new materials to IMA
		queue.remove(completed);

	}
	

}
