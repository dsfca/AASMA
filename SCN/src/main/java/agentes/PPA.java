package agentes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
import general.Pedido;
import general.Material;
import general.SortbyDate;
import general.SortbyPrice;

public class PPA extends Thread {
	
	private IniManager ini;
	
	private ServerSocket ssocket;
	
	public Belief beliefs;
	public Desire desire;
	public volatile List<Pedido> queue;
	public volatile List<Pedido> plan;
	
	private Pedido last_order;
	
	private long id = -1;
	
	//setup socket for receiving orders, the agent's desire (maximize profit or minimize delivery time) and plan size
	public PPA(Desire d) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.queue = new ArrayList<Pedido>();
		this.plan = new ArrayList<Pedido>();
		
		this.ssocket = new ServerSocket(ini.getPPAServerPort());
		
		this.desire = d;
		this.last_order = null;
	}
	
	//create new thread
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	
	public void run() {
		try {
			if (id == -1)
				this.id =  Thread.currentThread().getId();
			
			//main thread handles deliberate behavior, the remaining threads register incoming orders
			//System.out.println(Thread.currentThread().getName());
			if (Thread.currentThread().getId() == id) {
				System.out.println("Deliberation Thread:" + Thread.currentThread().getName());
				newListener();
				Decision();
			}else{
				System.out.println("Registration Thread:" + Thread.currentThread().getName());
				System.out.println("Sou uma thread");
				Socket clientSocket = ssocket.accept();
				newListener();
				ObjectOutputStream objectOutputStream= new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
				Pedido pedido = (Pedido) objectInputStream.readObject();
				
				System.out.println("PPA (" + Thread.currentThread().getName() + "): " + pedido.toString());
				
				if (desire == Desire.minimizeDeliveryTime)
					order(pedido);
				
				addToQueue(pedido);
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void addToQueue(Pedido pedido) {
		queue.add(pedido);
	}
	
	//
	
	public synchronized void Decision() {
		
		while(true) {
			
			if (queue.isEmpty() && plan.isEmpty())
				continue;
			
			updateBeliefs();
						
			if (plan.isEmpty()) {
				
				if (desire == Desire.minimizeDeliveryTime) {
					
					boolean manufactured = false;
					
					for (int i = 0; i < plan.size(); i++) {
						if (canProduce(plan.get(i))) {
							manufacture(plan.get(i));
							manufactured = true;
							break;
						}
					}
					
					if (!manufactured)
						buildPlan();
						
				}else {
					
					Pedido next_order = plan.get(0);
				
					if (canProduce(next_order)) manufacture(next_order);
					else{
						if (next_order != last_order)
							order(next_order);
						buildPlan();
					}
					
				}
			}else {
				buildPlan();
			}
		
		}
	}
	
	private void updateBeliefs() {
		
		String [] alfa = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		List<Material> materials = null;
		for (int i = 0; i < alfa.length; i++) {
			materials.add( new Material(alfa[i], 1));
		}
		
		try {
			
			Socket imaSocket = (Socket) new Socket(ini.getIMAHost(), ini.getIMAServerPort());
			ObjectOutputStream imaObjectOutputStream = new ObjectOutputStream(imaSocket.getOutputStream());
			ObjectInputStream imaObjectInputStream = new ObjectInputStream(imaSocket.getInputStream());
			
			Object [] object_enviar = {"ci", materials};
			imaObjectOutputStream.writeObject(object_enviar);
			
			beliefs.quantidades = (HashMap<String, Integer>) imaObjectInputStream.readObject();
			
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
		
		//Check if MA is ready for production
	}
	
	private boolean canProduce(Pedido pedido) {
		List <Material> necessary_materials = pedido.getMateriais();
		int available_quantity;
		
		for (int i = 0; i < necessary_materials.size(); i++) {
			Material current_material = new Material(necessary_materials.get(i).getMaterial(), 1);
			available_quantity = beliefs.quantidades.get(current_material);
			if (available_quantity < necessary_materials.get(i).getQuantidade())
				return false;
			
		}
		
		return beliefs.MA_available;
	}
	
	private void manufacture(Pedido pedido) {
		try {
			Socket maSocket = (Socket) new Socket(ini.getMAHost(), ini.getMAServerPort());
			ObjectOutputStream maObjectOutputStream = new ObjectOutputStream(maSocket.getOutputStream());
			ObjectInputStream maObjectInputStream = new ObjectInputStream(maSocket.getInputStream());
			
			maObjectOutputStream.writeObject(pedido);
			
			editPlan(3, pedido, null); //remove pedido from plan

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void order(Pedido order) {
		
		List<Material> required_materials = order.getMateriais();
		
		if (desire == Desire.maximizeIncome) {
			
			int missing_quantity;
			Material current_material;
			List<Material> to_order = null;
			
			for (int i = 0; i < required_materials.size(); i++) {
				
				current_material = new Material(required_materials.get(i).getMaterial(), 1);

				missing_quantity = required_materials.get(i).getQuantidade() - beliefs.quantidades.get(current_material);
				
				if (missing_quantity > 0) {
					current_material = new Material(required_materials.get(i).getMaterial(), missing_quantity);
					to_order.add(current_material);
				}		
			}
			
			required_materials = to_order;
	
		}
		
		Socket mpaSocket;
		try {
			mpaSocket = (Socket) new Socket(ini.getMPAHost(), ini.getMPAServerPort());
			ObjectOutputStream mpaObjectOutputStream = new ObjectOutputStream(mpaSocket.getOutputStream());
			ObjectInputStream mpaObjectInputStream = new ObjectInputStream(mpaSocket.getInputStream());
			
			mpaObjectOutputStream.writeObject(required_materials);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		last_order = order;
		
	}

	private synchronized void buildPlan() {
	
		List<Pedido> queue_aux = queue;
		//queue_aux.addAll(this.plan);
		editPlan(4, null, queue_aux);
		
		
		if (desire == Desire.maximizeIncome)
			Collections.sort(queue_aux, new SortbyPrice());
		else
			Collections.sort(queue_aux, new SortbyDate());
		
		editPlan(1, null, queue_aux);
		//queue_aux.removeAll(plan);	
		
	}
	

	
	public synchronized List<Pedido> editPlan(int mode, Pedido pedido, List<Pedido> queue_aux) {
		List<Pedido> queue_aux1 = new ArrayList<Pedido>();
		if(mode == 1) { //equal to
			this.plan = queue_aux.subList(0, this.queue.size());
		
		}
		else if(mode == 3) { //remove
			this.plan.remove(pedido);
		
		}else if(mode == 4) {
			queue_aux.addAll(this.plan);
			queue_aux1 = queue_aux;
		}
		return queue_aux1;
	}
	
	public enum Desire {maximizeIncome, minimizeDeliveryTime};

}