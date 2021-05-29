package agentes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Material;
import general.Pedido;

public class MPA extends Thread{
	
	private IniManager ini;
	
	private ServerSocket ssocket;
	
	private List<Pedido> queue;
	
	/**
	 * RECEBE
	 * "get", "buy", "alocar", "new"
	 * 
	 * ENVIA
	 * "gi", "buy", "ri", "ai"
	 * 	 
	 */
	
	public MPA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getMPAServerPort());
	}
	
	private void newListener(){
		(new Thread(this)).start();
	}
	
	
	public void run() {
		try {
			Socket generalSocket = ssocket.accept();
			newListener();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(generalSocket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(generalSocket.getInputStream());
		
			Object [] object = (Object[]) objectInputStream.readObject();
			
			if (object[0].equals("get")) {
				//CONSULTAR IMA
				Object [] object_ima = {"gi", new ArrayList<Object>()};
				
				Socket imaSocket = (Socket) new Socket(ini.getIMAHost(), ini.getIMAServerPort());
				ObjectOutputStream imaObjectOutputStream = new ObjectOutputStream(imaSocket.getOutputStream());
				ObjectInputStream imaObjectInputStream = new ObjectInputStream(imaSocket.getInputStream());
				
				imaObjectOutputStream.writeObject(object_ima);
				HashMap <Object, Integer> quantidades = (HashMap <Object, Integer>) imaObjectInputStream.readObject();
				//ENVIAR PPA
				objectOutputStream.writeObject(quantidades);
				closeSocket(objectOutputStream, objectInputStream, generalSocket);
				closeSocket(imaObjectOutputStream, imaObjectInputStream, imaSocket);
			
			}else if(object[0].equals("buy")) {
				List <Material> comprar = (List<Material>) object[1];
				//COMPRAR SUPPLIER
				Socket supSocket = (Socket) new Socket(ini.getSupplierHost(), ini.getSupplierServerPort());
				ObjectOutputStream supObjectOutputStream = new ObjectOutputStream(supSocket.getOutputStream());
				ObjectInputStream supObjectInputStream = new ObjectInputStream(supSocket.getInputStream());
				supObjectOutputStream.writeObject(comprar);
				closeSocket(supObjectOutputStream, supObjectInputStream, supSocket);
			
			}else if(object[0].equals("alocar")) {
				//GET PPA
				List <Material> materiais = (List <Material>) object[1];
				//IMA
				Object [] object_ima = {"ri", materiais};
				
				Socket imaSocket = (Socket) new Socket(ini.getIMAHost(), ini.getIMAServerPort());
				ObjectOutputStream imaObjectOutputStream = new ObjectOutputStream(imaSocket.getOutputStream());
				ObjectInputStream imaObjectInputStream = new ObjectInputStream(imaSocket.getInputStream());
				
				imaObjectOutputStream.writeObject(object_ima);
				HashMap <Material, Integer> quantidades = (HashMap<Material, Integer>) imaObjectInputStream.readObject();
				//ANSWER PPA
				List <Material> materiais_existentes = new ArrayList<Material>();
				String mensagem = "";
				if(quantidades != null) {
					materiais_existentes = getListFromHashMap(quantidades, materiais);
					mensagem = "Failure";
				}else {
					mensagem = "Success";
				}
				Object [] resposta_ppa = {mensagem, materiais_existentes};
				objectOutputStream.writeObject(resposta_ppa);
			    //System.out.println("MPA: Materiais quant" + materiais_existentes);
			    closeSocket(objectOutputStream, objectInputStream, generalSocket);
			    closeSocket(imaObjectOutputStream, imaObjectInputStream, imaSocket);
			
			}else if(object[0].equals("new")) {
				List <Material> materiais = (List<Material>) object[1];
				closeSocket(objectOutputStream, objectInputStream, generalSocket);
				//ENVIAR IMA
				Object [] object_ima = {"ai", materiais};
				
				Socket imaSocket = (Socket) new Socket(ini.getIMAHost(), ini.getIMAServerPort());
				ObjectOutputStream imaObjectOutputStream = new ObjectOutputStream(imaSocket.getOutputStream());
				ObjectInputStream imaObjectInputStream = new ObjectInputStream(imaSocket.getInputStream());
				
				imaObjectOutputStream.writeObject(object_ima);
				if(imaObjectInputStream.readObject().equals("Added to inventory")) {
					closeSocket(imaObjectOutputStream, imaObjectInputStream, imaSocket);
					//ENVIAR PPA
					Socket ppaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
					ObjectOutputStream ppaObjectOutputStream = new ObjectOutputStream(ppaSocket.getOutputStream());
					ObjectInputStream ppaObjectInputStream = new ObjectInputStream(ppaSocket.getInputStream());
					Object [] object_ppa = {"new"};
					ppaObjectOutputStream.writeObject(object_ppa);
					closeSocket(ppaObjectOutputStream, ppaObjectInputStream, ppaSocket);
				} else {
					System.out.println("MPA WARNING: Erro ao adicionar novo material ao inventario");
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			//e.printStackTrace();
		}
	}
	
	public List <Material> getListFromHashMap(HashMap <Material, Integer> quantidades, List <Material> materiais) {
		List <Material> mat = new ArrayList <Material> ();
		Iterator it = quantidades.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        Material m = new Material(((Material) pair.getKey()).getMaterial(), (int) pair.getValue());
	        mat.add(m);
	    }
		return mat;
	}
	
	public void closeSocket(ObjectOutputStream oo, ObjectInputStream oi, Socket s) throws IOException {
		oo.close();
		oi.close();
		s.close();
	}
}