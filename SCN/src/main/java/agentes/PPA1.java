package agentes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ini4j.InvalidFileFormatException;

import general.Belief;
import general.IniManager;
import general.Material;
import general.Pedido;
import general.SortbyDate;
import general.SortbyPrice;

public class PPA1 extends Thread {
	
	private ServerSocket ssocket;
	private IniManager ini;
	private int id_deliberative;
	
	public Belief beliefs;
	public Desire desire;
	public volatile List<Pedido> queue;
	public List<Pedido> plan;
	private Pedido last_order;
	private boolean MA_available;
	public volatile List<Pedido> to_manufacture;
	public boolean orders_to_manufacture;
	
	//DELIBERATIVE
	/**
	 * RECEBE
	 * "oma", "ma_v", "ma_p", "new"
	 * 
	 * ENVIA
	 * "pronto", "buy", "get", "alocar"
	 * 	 
	 */
	
	public PPA1(Desire desire) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getPPAServerPort());
		this.id_deliberative = -1;
		this.plan = new ArrayList<Pedido>();
		this.queue = new ArrayList<Pedido>();
		this.desire = desire;
		this.last_order = null;
		this.beliefs = new Belief();
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}

	public void run() {
		try {
			System.out.println("INIT: PPA started");
			if(this.id_deliberative == -1) {
				System.out.println("INIT: PPA delibertive " + Thread.currentThread().getId());
				this.id_deliberative = (int) Thread.currentThread().getId();
				newListener();
			}
			if (Thread.currentThread().getId() == this.id_deliberative) {
				Decision();
				System.out.println("yo");
			
			}else{
				System.out.println("INIT: PPA registration " + Thread.currentThread().getId());
				Socket generalSocket = ssocket.accept();
				System.out.println(generalSocket.getPort());
				newListener();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(generalSocket.getOutputStream());
				ObjectInputStream objectInputStream = new ObjectInputStream(generalSocket.getInputStream());
				//RECEBER BASE
				Object [] object = (Object[]) objectInputStream.readObject();

				if(object[0].equals("oma")) {
					Pedido pedido = (Pedido) object[1];
					closeSocket(objectOutputStream, objectInputStream, generalSocket);
					
					if (desire == Desire.minimizeDeliveryTime)
						buy(pedido.getMateriais());
					
					addToQueue(pedido);
				
				}else if(object[0].equals("ma_v")) { //vaziu
					//ESPERAR E ENTREGAR MA
					
					while(!orders_to_manufacture) {
						MA_available = true;
						secureWait();
					}
					
					Pedido pedido = to_manufacture.get(0);
					orders_to_manufacture = false;
					
					objectOutputStream.writeObject(pedido);

					MA_available = false;
					
					closeSocket(objectOutputStream, objectInputStream, generalSocket);
				
				}else if(object[0].equals("ma_p")) {//com pedido
					
					Pedido pedido_final = (Pedido) object[1];
					
					Object [] object_final = {"pronto", pedido_final};
					Socket omaSocket = (Socket)new Socket(ini.getOMAHost(), ini.getOMAServerPort());
					ObjectOutputStream omaObjectOutputStream = new ObjectOutputStream(omaSocket.getOutputStream());
					ObjectInputStream omaObjectInputStream = new ObjectInputStream(omaSocket.getInputStream());
					omaObjectOutputStream.writeObject(object_final);
					closeSocket(omaObjectOutputStream, omaObjectInputStream, omaSocket);
					
					while(orders_to_manufacture = false) {
						MA_available = true;
						secureWait();
					}
					
					Pedido pedido = to_manufacture.remove(0);
					
					orders_to_manufacture = false;
					
					objectOutputStream.writeObject(pedido);

					MA_available = false;
					
					closeSocket(objectOutputStream, objectInputStream, generalSocket);
				
				}else if(object[0].equals("new")) {
					//CHEGOU MATERIAL UPDATE ALGUMA COISA...
					closeSocket(objectOutputStream, objectInputStream, generalSocket);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void secureWait() throws InterruptedException {
		System.out.println("PPA: Waiting for request " + Thread.currentThread().getId());
		wait();
		System.out.println("PPA: Stop wait " + Thread.currentThread().getId());
	
	}
	
	public void closeSocket(ObjectOutputStream oo, ObjectInputStream oi, Socket s) throws IOException {
		oo.close();
		oi.close();
		s.close();
	}
	
	private synchronized void addToQueue(Pedido pedido) {
		queue.add(pedido);
	}
	
	public void Decision() throws UnknownHostException, IOException, ClassNotFoundException {

		while(true) {
			
			Pedido pedido = editPlan(6, null, null, 0);
			
			if (queue.isEmpty() && pedido == null)
				continue;
			
			updateBeliefs();
						
			if (!plan.isEmpty()) {
				
				if (desire == Desire.minimizeDeliveryTime) {
					
					boolean manufactured = false;
					
					for (int i = 0; i < plan.size(); i++) {
						pedido = editPlan(6, null, null, 0);
						if (canProduce(pedido.getMateriais())) {
							manufacture(pedido);
							manufactured = true;
							break;
						}
					}
					
					if (!manufactured) {
						editPlan(1, null, queue, 0);
						editPlan(2, null, null, 0);
					}
						
				}else {
					
					Pedido next_order = editPlan(6, null, null, 0);
					if(next_order != null) {
						if (canProduce(next_order.getMateriais()))
							manufacture(next_order);
						else{
							if (next_order != last_order) {
								buy(next_order.getMateriais());
								last_order = next_order;
							}
							editPlan(1, null, queue, 0);
							editPlan(2, null, null, 0);
						}
					}	
				}
			}else {
				editPlan(1, null, queue, 0);
				editPlan(2, null, null, 0);
			}
		}
	}
	

	private void updateBeliefs() {
		
		try {
			
			Socket mpaSocket = (Socket)new Socket(ini.getMPAHost(), ini.getMPAServerPort());
			ObjectOutputStream mpaObjectOutputStream = new ObjectOutputStream(mpaSocket.getOutputStream());
			ObjectInputStream mpaObjectInputStream = new ObjectInputStream(mpaSocket.getInputStream());
			
			Object [] object = {"get", new ArrayList<Object>()};
			mpaObjectOutputStream.writeObject(object);
			beliefs.quantidades = (HashMap<Material, Integer>) mpaObjectInputStream.readObject();
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		beliefs.MA_available = MA_available;
	}
	
	private boolean canProduce(List <Material> necessary_materials) {
		int available_quantity;
		
		for (int i = 0; i < necessary_materials.size(); i++) {
			Material current_material = new Material(necessary_materials.get(i).getMaterial(), 1);
			available_quantity = beliefs.quantidades.get(current_material);
			if (available_quantity < necessary_materials.get(i).getQuantidade())
				return false;
		}
		
		return beliefs.MA_available;
	}
	
	//TODO
	private void manufacture(Pedido pedido) {
		
		try {
			alocar(pedido.getMateriais());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		to_manufacture.add(pedido);
		orders_to_manufacture = true;
		editPlan(3, pedido, null, 0);
	}

	//BUY - 2
	public void buy(List <Material> materiais) throws UnknownHostException, IOException {
		List <Material> required = materiais;
		
		if (desire == Desire.maximizeIncome) {
			int missing_quantity;
			Material current_material;
			List<Material> to_order = null;
			
			for (Material material: materiais) {
				
				missing_quantity = material.getQuantidade() - beliefs.quantidades.get(material.getMaterial());
				
				current_material = new Material(material.getMaterial(), missing_quantity);
				
				if (missing_quantity > 0)
					to_order.add(current_material);	
			}
			
			required = to_order;	
		}
		
		Socket mpaSocket = (Socket)new Socket(ini.getMPAHost(), ini.getMPAServerPort());
		ObjectOutputStream mpaObjectOutputStream = new ObjectOutputStream(mpaSocket.getOutputStream());
		ObjectInputStream mpaObjectInputStream = new ObjectInputStream(mpaSocket.getInputStream());
		
		Object [] object_mpa = {"buy", required};
		mpaObjectOutputStream.writeObject(object_mpa);
		closeSocket(mpaObjectOutputStream, mpaObjectInputStream, mpaSocket);
		
	}
	
	//ALOCAR - 3
	public boolean alocar(List <Material> materiais) throws UnknownHostException, IOException, ClassNotFoundException {
		Socket mpaSocket = (Socket)new Socket(ini.getMPAHost(), ini.getMPAServerPort());
		ObjectOutputStream mpaObjectOutputStream = new ObjectOutputStream(mpaSocket.getOutputStream());
		ObjectInputStream mpaObjectInputStream = new ObjectInputStream(mpaSocket.getInputStream());
		
		Object [] object_mpa = {"alocar", materiais};
		mpaObjectOutputStream.writeObject(object_mpa);
		Object [] mensagem = (Object[]) mpaObjectInputStream.readObject();
		closeSocket(mpaObjectOutputStream, mpaObjectInputStream, mpaSocket);
		if (!mensagem[0].equals("Success")) {
			List <Material> existentes = (List<Material>) mensagem[1];
			buy(materiais);
			return false;
		}else {
			return true;
		}
		
	}
	
	public synchronized Pedido editPlan(int mode, Pedido pedido, List<Pedido> queue_aux, int index) {
		if(mode == 1) { //equal to
			this.plan.clear();
			this.plan.addAll(queue_aux);
			notify();
		
		}else if(mode == 2) { //build plan
			if (this.desire == Desire.maximizeIncome) {
				Collections.sort(plan, new SortbyPrice());
			}
			else {
				Collections.sort(plan, new SortbyDate());
			}
		}else if(mode == 3) { //remove
			this.plan.remove(pedido);
		
		}else if(mode == 4) { //add
			this.plan.add(pedido);
			notify();
		
		}else if(mode == 5) { //retirar e remover - usado
			Pedido pedido_aux = this.plan.get(0);
			this.plan.remove(pedido_aux);
			pedido = pedido_aux; //verificar que troca
		
		}else if(mode == 6) { //get - usado
			if(!this.plan.isEmpty())
				pedido = this.plan.get(index);
		}
		return pedido;
	}
	
	public enum Desire {maximizeIncome, minimizeDeliveryTime};

	public static void main(String[] args) throws InvalidFileFormatException, IOException {
		PPA1 ppa = new PPA1(Desire.maximizeIncome);
		ppa.start();

	}

}
