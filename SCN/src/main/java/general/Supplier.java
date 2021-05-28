package general;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;

public class Supplier extends Thread{
	
	private IniManager ini;
	
	private ServerSocket server_socket;
	
	public Supplier() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.server_socket = new ServerSocket(ini.getSupplierServerPort());
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	public void run() {
		try {
			Socket socket = server_socket.accept();
			newListener();
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			
			List<Material> material = (List<Material>) objectInputStream.readObject();
			
			produce(material);
			
			objectOutputStream.writeObject(material);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void produce(List<Material> material) {
		int wait_time = 0;
		
		for (int i = 0; i < material.size(); i++) {
			wait_time += Character.getNumericValue(material.get(i).getMaterial().charAt(0)) - 9;
		}
		
		try {
			Thread.sleep(wait_time * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	

}