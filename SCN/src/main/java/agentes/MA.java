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
import java.net.UnknownHostException;
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
	
	private int possible_threads;
	private int active_threads;
	
	/**
	 * RECEBE
	 * "p", "ma_v", "ma_p", "new"
	 * 
	 * ENVIA
	 * "pronto", "buy", "get", "alocar"
	 * 	 
	 */
	
	
	public MA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.possible_threads = ini.getActiveThreads();
		this.active_threads = 0;
		
		
		this.imaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
		this.imaObjectOutputStream = new ObjectOutputStream(this.imaSocket.getOutputStream());
		this.imaObjectInputStream = new ObjectInputStream(this.imaSocket.getInputStream());
		
		this.ssocket = new ServerSocket(ini.getMAServerPort());
	}
	
	private void newListener(){
		
		(new Thread(this)).start();
	}
	
	public void run() {
		System.out.println("INIT: MA started");
		incrementThreads();
		if(this.active_threads < this.possible_threads) {
			System.out.println(this.active_threads);
			newListener();
		}
		try {
			//PRIMEIRO PEDIDO
			Pedido produzido = sendMA_V();

			//SEND "ma_p"
			while(true) {
				Object [] object_p = {"ma_p", produzido};
				Socket ppaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
				ObjectOutputStream ppaObjectOutputStream = new ObjectOutputStream(ppaSocket.getOutputStream());
				ObjectInputStream ppaObjectInputStream = new ObjectInputStream(ppaSocket.getInputStream());
				ppaObjectOutputStream.writeObject(object_p);
				Pedido novo = (Pedido) ppaObjectInputStream.readObject();
				closeSocket(ppaObjectOutputStream, ppaObjectInputStream, ppaSocket);

				produzido = produzir(novo);
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Pedido sendMA_V() throws UnknownHostException, IOException, ClassNotFoundException {
		Socket ppaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
		ObjectOutputStream ppaObjectOutputStream = new ObjectOutputStream(ppaSocket.getOutputStream());
		ObjectInputStream ppaObjectInputStream = new ObjectInputStream(ppaSocket.getInputStream());
		System.out.println("MA FIM");
		//SEND "ma_v"
		Object [] object_v = {"ma_v"};
		ppaObjectOutputStream.writeObject(object_v);
		Pedido p = (Pedido) ppaObjectInputStream.readObject();
		Pedido produzido = produzir(p);
		closeSocket(ppaObjectOutputStream, ppaObjectInputStream, ppaSocket);
		return produzido;
	}
	
	public void closeSocket(ObjectOutputStream oo, ObjectInputStream oi, Socket s) throws IOException {
		oo.close();
		oi.close();
		s.close();
	}

	public synchronized void incrementThreads() {
		this.active_threads++;
	}
	
	private Pedido produzir(Pedido pedido) {
		int wait_time = 0;
		String produto_final = "";
		
		for (Material material: pedido.getMateriais()) {
			wait_time += ((Character.getNumericValue(material.getMaterial().charAt(0)) - 9)
					* material.getQuantidade());
			produto_final = produto_final + material.getMaterial() + material.getQuantidade();
		}
		try {
			Thread.sleep(wait_time * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Produto produto = new Produto(produto_final, 1);
		pedido.setProdutoFinal(produto);
		return pedido;
	}
	
	public static void main(String[] args) {
		
	}

}