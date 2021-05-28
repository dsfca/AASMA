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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Material;
import general.Pedido;
import general.Produto;

public class MA extends Thread {

	private IniManager ini;
	
	private Socket imaSocket;
	private ObjectOutputStream imaObjectOutputStream;
	private ObjectInputStream imaObjectInputStream;
	
	private ServerSocket ssocket;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	private int n_threads_active;
	private int open_threads=1;
	
	
	
	
	public MA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.n_threads_active = ini.getActiveThreads();
		this.imaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
		this.imaObjectOutputStream = new ObjectOutputStream(this.imaSocket.getOutputStream());
		this.imaObjectInputStream = new ObjectInputStream(this.imaSocket.getInputStream());
		
		this.ssocket = new ServerSocket(ini.getMAServerPort());
	}
	
	private void newListener()
	{
		
		(new Thread(this)).start();
	}
	
	
	
	/*private void listenToPPA() {
		try {
			while(true) {
				//OUVE O PPA E AVISA AS THREDS POR AQUI
				System.out.println("a dormir");
				Thread.sleep(100000000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	private synchronized Pedido connect() {
		Pedido pedido=null;
		try {
			
			Socket clientSocket = ssocket.accept();
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
			pedido = (Pedido) objectInputStream.readObject();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return pedido;
		
	}
	
	private void produz(Pedido pedido) {
		
		try {
			int sum=0;
			int sum_aux=0;
			List <Material> materiais = pedido.getMateriais();/*CORRIGIR PARA MATERIAL*/
			
			for(int i=0;i<materiais.size();i++) {
				for(int j=0;j<materiais.get(i).getMaterial().length();j++) {
					sum_aux+=Character.getNumericValue(materiais.get(i).getMaterial().charAt(j))-9;
					
				}
				sum+=sum_aux*materiais.get(i).getQuantidade();
				sum_aux=0;
				
			}	
			System.out.println(sum);
			Thread.sleep(sum*1000);
			// dorme durante sum*1000 para simular
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	
		
	}
	
	public void run() {
		try {
			/*System.out.println("aqui1");
			/*if(initialize_socket==true) {
				initialize_socket=false;
				newListener();
				listenToPPA();
						
				//SOCKETS
			}*/
			
			if(open_threads<this.n_threads_active) {
				open_threads++;
				this.n_threads_active--;
				newListener();
			}
			
			while(true) {
				Pedido pedido=connect();
				produz(pedido);
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
	}

}