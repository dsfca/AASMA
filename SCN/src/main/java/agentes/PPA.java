package agentes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import org.ini4j.InvalidFileFormatException;

import general.Belief;
import general.IniManager;
import general.Pedido;
import general.Produto;
import general.SortbyDate;
import general.SortbyPrice;

enum Desire {maximizeIncome, minimizeDeliveryTime};

public class PPA extends Thread {
	
	private static int SERVER_PORT;
	
	private IniManager ini;
	
	private Socket omaSocket;
	private ObjectOutputStream omaObjectOutputStream;
	private ObjectInputStream omaObjectInputStream;
	
	private Socket mpaSocket;
	private ObjectOutputStream mpaObjectOutputStream;
	private ObjectInputStream mpaObjectInputStream;
	
	private ServerSocket ssocket;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	public Belief beliefs;
	public Desire desire;
	public List<Pedido> queue;
	public List<Pedido> plan;
	
	public int n_plan;
	
	private Pedido last_order;
	
	private long id = -1;
	
	//setup socket for receiving orders, the agent's desire (maximize profit or minimize delivery time) and plan size
	public PPA(Desire d, int n) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		
		this.mpaSocket = (Socket)new Socket(ini.getMPAHost(), ini.getMPAServerPort());
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
			
			Socket clientSocket = ssocket.accept();
			System.out.println("PPA: started");
			newListener();
			
			//main thread handles deliberate behavior, the remaining threads register incoming orders
			if (Thread.currentThread().getId() == id)
				Decision();
			else{
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
				Pedido pedido = (Pedido) objectInputStream.readObject();
				
				addToQueue(pedido);
				
				System.out.println("PPA: " + pedido.toString());
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void addToQueue(Pedido pedido) {
		queue.add(pedido);	
	}
	
	//
	
	public void Decision() {
		
		while(true) {
			
			if (!queue.isEmpty())
				continue;
			
			updateBeliefs();
			
			if (!plan.isEmpty()) {
				
				Pedido next_order = plan.get(0);
				
				if (canProduce(next_order)) execute(next_order);
				else{
					buildPlan();
					if (next_order != last_order)
						order(next_order);
				}
				
				if (plan.size() < n_plan) deliberate();
			}
			else{
				deliberate();
				buildPlan();
			}
		
		}
	}
	
	private void order(Pedido order) {
		
		String string;
		int countA = 0, countB = 0, countC = 0, countD = 0;
			
		for (int i=0; i< order.getProdutos().size(); i++) 
		{
			string = order.getProdutos().get(i).getProduto();
				
			for(int j = 0; j < string.length(); j++) 
			{    
				if(string.charAt(i) == 'A')    
					countA += order.getProdutos().get(i).getQuantidade();
		        else if (string.charAt(i) == 'B')
		        	countB += order.getProdutos().get(i).getQuantidade();
		        else if (string.charAt(i) == 'C')
		        	countC += order.getProdutos().get(i).getQuantidade();
		        else
		            countD += order.getProdutos().get(i).getQuantidade();
			}    
		}
			
		List <Produto> produtos = null;
			
		if (countA > 0)
			produtos.add(new Produto("A", countA));
		if (countB > 0)
			produtos.add(new Produto("B", countB));
		if (countC > 0)
			produtos.add(new Produto("C", countC));
		if (countD > 0)
			produtos.add(new Produto("D", countD));
			
		try {
			mpaObjectOutputStream.writeObject(new Pedido(produtos));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void deliberate() {
	
		queue.addAll(plan);
		
		if (desire == Desire.maximizeIncome)
			Collections.sort(queue, new SortbyPrice());
		else
			Collections.sort(queue, new SortbyDate());
		
		
		
		plan = queue.subList(0, n_plan);
		queue.removeAll(plan);	
		
	}

	private boolean canProduce(Pedido pedido) {
		
		int a = 0, b = 0, c = 0, d = 0;
		String string;
		
		for (int i = 0; i < pedido.getProdutos().size(); i++) {
			
			string = pedido.getProdutos().get(i).getProduto();
				
			for(int j = 0; j < string.length(); j++) 
			{    
				if(string.charAt(j) == 'A')    
		            a += pedido.getProdutos().get(i).getQuantidade();
		        else if (string.charAt(j) == 'B')
		            b += pedido.getProdutos().get(i).getQuantidade();
		        else if (string.charAt(j) == 'C')
		            c += pedido.getProdutos().get(i).getQuantidade();
		        else
		            d += pedido.getProdutos().get(i).getQuantidade();
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
			
			plan.remove(pedido);

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
		
		if (desire == Desire.maximizeIncome) {
			Collections.sort(plan, new SortbyPrice());
		}
		else {
			Collections.sort(plan, new SortbyDate());
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}