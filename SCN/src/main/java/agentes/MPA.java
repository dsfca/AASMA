package agentes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Material;
import general.Pedido;

public class MPA extends Thread{
	
	private IniManager ini;
	
	private ServerSocket ssocket;
	
	private List<Pedido> queue;
	
	public MPA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getMPAServerPort());
	}
	
	private void newListener(){
		(new Thread(this)).start();
	}
	
	
	public void run() {
		try {
			
			Socket socket = ssocket.accept();
			System.out.println("MPA: started");
			newListener();
			
			
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
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
		
		Socket supplierSocket = (Socket) new Socket(ini.getSupplierHost(),ini.getSupplierServerPort());
		ObjectOutputStream supplierObjectOutputStream = new ObjectOutputStream(supplierSocket.getOutputStream());
		ObjectInputStream supplierObjectInputStream = new ObjectInputStream(supplierSocket.getInputStream());

		supplierObjectOutputStream.writeObject(pedido.getMateriais());
		
		List<Material> materials = (List<Material>) supplierObjectInputStream.readObject();
		
		//enviar materiais novos para o IMA
		
		queue.remove(pedido);

	}
	

}
