package agentes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;
import general.Produto;

public class IMA extends Thread {
	
	private IniManager ini;
	//private Socket socket;
	private ServerSocket sOmaSocket;
	
	private File inventory;

	
	public IMA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.sOmaSocket = new ServerSocket(ini.getIMAServerPort());
		this.inventory = new File(ini.getInventoryPath());
		cleanInventory();
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	public void run() {
		try {
			Socket socket = sOmaSocket.accept();
			System.out.println("IMA: Coneccao recebida de OMA");
			newListener();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			Produto produto = (Produto) objectInputStream.readObject();
			System.out.println("IMA: " + produto.toString());
			int quantidade = checkInventory(produto);
			objectOutputStream.writeObject(quantidade);
			///////
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void receiveQuery() {
		
	}

	//Check product quantity in Inventory
	public int checkInventory(Produto produto) throws IOException {
		LineIterator it = FileUtils.lineIterator(this.inventory, "UTF-8");
		int quantidade = 0;
		try {
			while (it.hasNext()) {
				String[] line = it.nextLine().split(" ");
				if(line[0].equals(produto.getProduto())) {
					quantidade = Integer.parseInt(line[1]);
				}
			}
		} finally {
			it.close();
		}
		return quantidade;
	}
	
	//Place Final Product or Material in Inventory
	public synchronized void placeInInventory(Object object) throws IOException {
		String to_write = "";
		if(object instanceof Produto) {
			to_write = ((Produto) object).getProduto() + " " + ((Produto) object).getQuantidade() + "\n";
		}
		//}else if (object instanceof Material){}
		FileWriter fw = new FileWriter(this.inventory, true);
		fw.write(to_write);
		fw.close();
	}
	
	public void cleanInventory() throws IOException {
		FileWriter fw = new FileWriter(this.inventory, false);
		fw.write("");
		fw.close();
	}
	

	public static void main(String[] args) throws IOException {
		/**IMA ima = new IMA();
		Produto p1 = new Produto("B", 5);
		Produto p2 = new Produto("A", 6);
		ima.placeInInventory(p1);
		System.out.println(ima.checkInventory(p2));*/

	}

}
