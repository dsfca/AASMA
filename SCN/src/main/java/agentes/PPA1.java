package agentes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;
import general.Produto;

public class PPA1 extends Thread {
	
	private ServerSocket ssocket;
	private IniManager ini;
	private int id_deliberative;

	public PPA1() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getPPAServerPort());
		this.id_deliberative = -1;
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}

	public void run() {
		try {
			
			if(this.id_deliberative == -1) {
				System.out.println("Delibertive id: " + Thread.currentThread().getId());
				this.id_deliberative = (int) Thread.currentThread().getId();
				newListener();
			}
			if (Thread.currentThread().getId() == this.id_deliberative) {
				//Decision();
			
			}else{
				System.out.println("Registration Thread:" + Thread.currentThread().getId());
				Socket generalSocket = ssocket.accept();
				sleep(1000);
				newListener();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(generalSocket.getOutputStream());
				ObjectInputStream objectInputStream = new ObjectInputStream(generalSocket.getInputStream());
				//RECEBER DO OMA
				Pedido pedido = (Pedido) objectInputStream.readObject();
				System.out.println(pedido.toString());
				
				//ENVIAR PARA OMA
				//produto que devia vir do MA
				Produto produto = new Produto("XXX", 1);
				objectOutputStream.writeObject(produto);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InvalidFileFormatException, IOException {
		PPA1 ppa = new PPA1();
		ppa.start();

	}

}
