package general;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
			
			List <Material> material = (List<Material>) objectInputStream.readObject();
			
			produce(material);
			
			/*SEND TO MPA*/
			Socket mpaSocket = (Socket)new Socket(ini.getMPAHost(), ini.getMPAServerPort());
			ObjectOutputStream mpaObjectOutputStream = new ObjectOutputStream(mpaSocket.getOutputStream());
			ObjectInputStream mpaObjectInputStream = new ObjectInputStream(mpaSocket.getInputStream());
			
			Object [] object = {"new", material};
			mpaObjectOutputStream.writeObject(object);
			closeSocket(mpaObjectOutputStream, mpaObjectInputStream, mpaSocket);
			closeSocket(objectOutputStream, objectInputStream, socket);
			
		} catch (IOException e) {
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {
			//e.printStackTrace();
		}
	}
	
	public void produce(List<Material> materials) {
		int wait_time = 0;
		
		for (Material material: materials) {
			wait_time += ((Character.getNumericValue(material.getMaterial().charAt(0)) - 9)
					* material.getQuantidade());
		}
		try {
			Thread.sleep(wait_time * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	public void closeSocket(ObjectOutputStream oo, ObjectInputStream oi, Socket s) throws IOException {
		oo.close();
		oi.close();
		s.close();
	}
	
	public static void main(String [] args) throws InvalidFileFormatException, IOException {
		Supplier s = new Supplier();
		List <Material> m = new ArrayList<Material>();
		Material m1 = new Material("B", 1);
		m.add(m1);
		s.produce(m);
	}
	
	

}