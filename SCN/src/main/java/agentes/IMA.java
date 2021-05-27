package agentes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Material;
import general.Pedido;
import general.Produto;

public class IMA extends Thread {
	
	private IniManager ini;
	//private Socket socket;
	private ServerSocket server_socket;
	
	private File inventory;

	
	public IMA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.server_socket = new ServerSocket(ini.getIMAServerPort());
		this.inventory = new File(ini.getInventoryPath());
		cleanInventory();
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}

	public void run() {
		try {
			Socket socket = server_socket.accept();
			//System.out.println("IMA: Coneccao recebida de OMA");
			newListener();

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			/**List <Object> request = (List<Object>) objectInputStream.readObject();

		if(request.get(0).equals("ci")) { //check inventory
			List <Produto> produtos = (List<Produto>) request.get(1);
			HashMap <Produto, Integer> quantidades = new HashMap<Produto, Integer>();
			for(Produto produto: produtos) {
				int quantidade = checkInventory(produto);
				quantidades.put(produto, quantidade);
			}
			objectOutputStream.writeObject(quantidades);
			System.out.println("R " + quantidades);

		}else if(request.get(0).equals("ai")) { //add inventory
			List <Object> objects = (List<Object>) request.get(1);
			for(int i = 0; i < objects.size(); i++) {
				placeInInventory(objects.get(i));
			}
			System.out.println("Adiconado ao Inventario");
		}else if(request.get(0).equals("ri")) { //remove inventory


		}**/
			System.out.println("IMA: " /*+ produto.toString()*/);
			/*int quantidade = checkInventory(produto);*/
			//objectOutputStream.writeObject(quantidade);
			///////
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void receiveQuery() {

	}

	//Check product quantity in Inventory
	public int checkInventory(Object object) throws IOException {
		LineIterator it = FileUtils.lineIterator(this.inventory, "UTF-8");
		int quantidade = 0;
		try {
			while (it.hasNext()) {
				String[] line = it.nextLine().split(" ");
				if(object instanceof Produto) {
					if(line[0].equals(((Produto) object).getProduto())) {
						quantidade = Integer.parseInt(line[1]);
					}
				}else if(object instanceof Material) {
					if(line[0].equals(((Material) object).getMaterial())) {
						quantidade = Integer.parseInt(line[1]);
					}
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

	/*public void alocateInventory() {
		List <Object> objects = (List<Object>) request.get(1);
		for(int i = 0; i < objects.size(); i++) {
			if(checkInventory(objects.get(i)) >= objects.get(i).getQuantidade()) {

	}

	public synchronized HashMap <Object, Integer> interactInventory() {

	}*/


	public static void main(String[] args) throws IOException {
		/**IMA ima = new IMA();
		Produto p1 = new Produto("B", 5);
		Produto p2 = new Produto("A", 6);
		ima.placeInInventory(p1);
		System.out.println(ima.checkInventory(p2));*/

	}

}
