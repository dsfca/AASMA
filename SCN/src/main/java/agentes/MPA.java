package agentes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;

public class MPA extends Thread{
	
	private IniManager ini;
	
	private Socket supplierSocket;
	private ObjectOutputStream supplierObjectOutputStream;
	private ObjectInputStream supplierObjectInputStream;
	
	private ServerSocket ssocket;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	public MPA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		
		this.supplierSocket = (Socket)new Socket(ini.getSupplierHost(), ini.getSupplierServerPort());
		this.supplierObjectOutputStream = new ObjectOutputStream(this.supplierSocket.getOutputStream());
		this.supplierObjectInputStream = new ObjectInputStream(this.supplierSocket.getInputStream());
		
		this.ssocket = new ServerSocket(ini.getMPAServerPort());
	}
	
	private void newListener(){
		(new Thread(this)).start();
	}
	
	
	public void run() {
		try {
			
			Socket clientSocket = ssocket.accept();
			System.out.println("MPA: started");
			newListener();
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
			Pedido pedido = (Pedido) objectInputStream.readObject();
			System.out.println("MPA: " + pedido.toString());
			
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	public void SendOrder() {
		try {
			Socket clientSocket = ssocket.accept();
			newListener();
			
		}
	}
	*/

}
