package agentes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

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
	
	private String inventory_file;
	private File inventory;

	
	public IMA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.server_socket = new ServerSocket(ini.getIMAServerPort());
		this.inventory_file = ini.getInventoryPath();
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
			/**List <Object> request = (List<Object>) objectInputStream.readObject();*/

			//RECEBER NA SERVER SOCKET: OMA ou MPA
			Object [] object = (Object[]) objectInputStream.readObject();

			if (object[0].equals("ci")) {
				HashMap <Object, Integer> quantidades = interactInventory(object);
				objectOutputStream.writeObject(quantidades);

			}else if(object[0].equals("ai")) {
				interactInventory(object); //list of products
				objectOutputStream.writeObject("Added to inventory");

			}else if(object[0].equals("ri")) {
				interactInventory(object); //list of products
				objectOutputStream.writeObject("Removed from inventory");
			}
			System.out.println("IMA: " /*+ produto.toString()*/);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized HashMap <Object, Integer> interactInventory(Object[] object) throws IOException {
		List <Object> object_pm = (List<Object>) object[1];
		HashMap <Object, Integer> quantidades = new HashMap <Object, Integer>();
		
		if(object[0].equals("ci")) {
			for(int i = 0; i < object.length; i++) {
				quantidades.put(object_pm.get(i), checkInventory(object_pm.get(i)));
			}
		}else if(object[0].equals("ai")) {
			for(Object object_: object_pm) {
				int quantidade_antiga = checkInventory(object_);
				if(quantidade_antiga == 0) {
					placeInInventory(object_);
				} else {
					String [] identification = getObjectId(object_);
					replace(identification[0], quantidade_antiga, Integer.parseInt(identification[1]));
				}
			}
		}else if(object[0].equals("ri")) {
			for(Object object_: object_pm) {
				int quantidade_antiga = checkInventory(object_);
				String [] identification = getObjectId(object_);
				removeInventory(identification[0], String.valueOf(quantidade_antiga));
			}
		}
		return quantidades;
	}

	//Place Final Product or Material in Inventory - DONE
	public void placeInInventory(Object object) throws IOException {
		String to_write = "";
		if(object instanceof Produto) {
			to_write = ((Produto) object).getProduto() + " " + ((Produto) object).getQuantidade() + "\n";
		}else if(object instanceof Material) {
			to_write = ((Material) object).getMaterial() + " " + ((Material) object).getQuantidade() + "\n";
		}
		FileWriter fw = new FileWriter(this.inventory, true);
		fw.write(to_write);
		fw.close();
	}

	//REMOVE - DONE
	public void removeInventory(String object_name, String quantity) throws IOException {
		File inputFile = this.inventory;
		File tempFile = new File("myTempFile.txt");
		
		LineIterator it = FileUtils.lineIterator(this.inventory, "UTF-8");
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		//String lineToRemove = "bbb";
		String currentLine;

		while((currentLine = reader.readLine()) != null) {
		    // trim newline when comparing with lineToRemove
		    //String trimmedLine = currentLine.trim();
		    if(currentLine.equals(object_name + " " + quantity)) continue;
		    writer.write(currentLine + System.getProperty("line.separator"));
		}
		writer.close(); 
		reader.close(); 
		boolean successful = tempFile.renameTo(inputFile);
	}

	
	//GET OBJECT IDENTIFICATION - DONE
	public String [] getObjectId(Object object) {
		String [] identification = new String [2];
		if(object instanceof Produto) {
			identification[0] = ((Produto) object).getProduto();
			identification[1] = String.valueOf(((Produto) object).getQuantidade());
		}else if(object instanceof Material) {
			identification[0] = ((Material) object).getMaterial();
			identification[1] = String.valueOf(((Material) object).getQuantidade());
		}
		return identification;
	}

	//REPLACE - DONE
	public void replace(String object_name, int quantidade_antiga, int quantidade_nova) throws IOException {

		Scanner sc = new Scanner(this.inventory);
		StringBuffer buffer = new StringBuffer();
		while (sc.hasNextLine()) {
			buffer.append(sc.nextLine()+System.lineSeparator());
		}
		String fileContents = buffer.toString();
		sc.close();
		int quantidade_final = quantidade_antiga + quantidade_nova;
		String oldLine = object_name + " " + quantidade_antiga;
		String newLine = object_name + " " + quantidade_final;
		fileContents = fileContents.replaceAll(oldLine, newLine);
		FileWriter writer = new FileWriter(this.inventory_file);
		writer.append(fileContents);
		writer.flush();
	}

	//Check product quantity in Inventory - DONE
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

	//CLEAN - DONE
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
