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
	
	private Socket mpaSocket;
	private ObjectOutputStream mpaObjectOutputStream;
	private ObjectInputStream mpaObjectInputStream;
	
	private ServerSocket ssocket;
	
	public Belief beliefs;
	public Desire desire;
	public volatile List<Pedido> queue;
	public volatile List<Pedido> plan;
	
	public int n_plan;
	
	private Pedido last_order;
	
	private long id = -1;
	
	//setup socket for receiving orders, the agent's desire (maximize profit or minimize delivery time) and plan size
	public PPA(Desire d, int n) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.queue = new ArrayList<Pedido>();
		this.plan = new ArrayList<Pedido>();
		

		this.mpaSocket = (Socket) new Socket(ini.getMPAHost(), ini.getMPAServerPort());
		this.mpaObjectOutputStream = new ObjectOutputStream(this.mpaSocket.getOutputStream());
		this.mpaObjectInputStream = new ObjectInputStream(this.mpaSocket.getInputStream());

		
		this.ssocket = new ServerSocket(ini.getPPAServerPort());
		
		this.desire = d;
		this.n_plan = n;
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
			
			if (queue.isEmpty())
				continue;
			
			int size;
			
			updateBeliefs();
			
			size = plan.size();
						
			if (size != 0) {
				
				Pedido next_order = plan.get(0);
				
				if (canProduce(next_order)) execute(next_order);
				else{
					buildPlan();
					if (next_order != last_order)
						order(next_order);
				}
				
				if (plan.size() < this.n_plan) deliberate();
			}
			else{
				deliberate();
				synchronized(plan) {
					if (plan.size() > 1) buildPlan();
				}
			}
		
		}
	}
	
	private void order(Pedido order) {
		
		String string;
		int countA = 0, countB = 0, countC = 0, countD = 0;
			
		for (int i=0; i< order.getMateriais().size(); i++) 
		{
			string = order.getMateriais().get(i).getMaterial();
				
			for(int j = 0; j < string.length(); j++) 
			{    
				if(string.charAt(i) == 'A')    
					countA += order.getMateriais().get(i).getQuantidade();
		        else if (string.charAt(i) == 'B')
		        	countB += order.getMateriais().get(i).getQuantidade();
		        else if (string.charAt(i) == 'C')
		        	countC += order.getMateriais().get(i).getQuantidade();
		        else
		            countD += order.getMateriais().get(i).getQuantidade();
			}    
		}
			
		List <Material> produtos = null;
			
		if (countA > 0)
			produtos.add(new Material("A", countA));
		if (countB > 0)
			produtos.add(new Material("B", countB));
		if (countC > 0)
			produtos.add(new Material("C", countC));
		if (countD > 0)
			produtos.add(new Material("D", countD));
			
		try {
			mpaObjectOutputStream.writeObject(new Pedido(produtos));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private synchronized void deliberate() {
	
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
	
	

	private boolean canProduce(Pedido pedido) {
		
		int a = 0, b = 0, c = 0, d = 0;
		String string;
		
		for (int i = 0; i < pedido.getMateriais().size(); i++) {
			
			string = pedido.getMateriais().get(i).getMaterial();
				
			for(int j = 0; j < string.length(); j++) 
			{    
				if(string.charAt(j) == 'A')    
		            a += pedido.getMateriais().get(i).getQuantidade();
		        else if (string.charAt(j) == 'B')
		            b += pedido.getMateriais().get(i).getQuantidade();
		        else if (string.charAt(j) == 'C')
		            c += pedido.getMateriais().get(i).getQuantidade();
		        else
		            d += pedido.getMateriais().get(i).getQuantidade();
		    }
		}
		
		if ( beliefs.a_level < a || beliefs.b_level < b || beliefs.c_level < c || beliefs.d_level < d )
			return false;
		
		return beliefs.MA_available;
	}

	private void execute(Pedido pedido) {
		
		Socket socket;
		
		OutputStream outputStream;
		try {
			socket = new Socket("localhost", 7777);
			outputStream = socket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(pedido);
			socket.close();
			
			editPlan(3, pedido, null); //remove pedido from plan

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		updateBeliefs();
	}

	private void updateBeliefs() {
		// Ask IMA for material stocks:
		// beliefs.a_level = 
		// beliefs.b_level = 
		// beliefs.c_level = 
		// beliefs.d_level = 
	
		//Ask MPA if there are new deliveries
		/*
		try {
			//connect to MA
			beliefs.MA_available = true;
		} catch (IOException e) {
			beliefs.MA_available = false;
		}
		*/
	}

	public void buildPlan() {
		editPlan(2, null, null);
	}
	
	public synchronized List<Pedido> editPlan(int mode, Pedido pedido, List<Pedido> queue_aux) {
		List<Pedido> queue_aux1 = new ArrayList<Pedido>();
		if(mode == 1) { //equal to
			this.plan = queue_aux.subList(0, this.queue.size());
		
		}else if(mode == 2) { //build plan
			if (desire == Desire.maximizeIncome) {
				Collections.sort(plan, new SortbyPrice());
			}
			else {
				Collections.sort(plan, new SortbyDate());
			}
		}else if(mode == 3) { //remove
			this.plan.remove(pedido);
		
		}else if(mode == 4) {
			queue_aux.addAll(this.plan);
			queue_aux1 = queue_aux;
		}
		return queue_aux1;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public enum Desire {maximizeIncome, minimizeDeliveryTime};

}