import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.ini4j.InvalidFileFormatException;

enum Desire {maximizeIncome, minimizeDeliveryTime};

public class PPA implements Runnable {
	
	private static int SERVER_PORT;
	
	private IniManager ini;
	private Socket socket;
	private ServerSocket ssocket;
	
	public Belief beliefs;
	public Desire desire;
	public List<Pedido> queue;
	public List<Pedido> plan;
	
	public int n_plan;
	
	private Pedido last_order;
	

	public PPA(Desire d, int n) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.SERVER_PORT = ini.getOMAServerPort();
		this.ssocket = new ServerSocket(SERVER_PORT);
		this.socket = (Socket)new Socket(ini.getOMAhost(), ini.getOMAClientPort());
		
		this.desire = d;
		this.n_plan = n;
		this.last_order = null;
	}
	
	//+
	
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	
	public void run() {
		try {
			Socket connection = ssocket.accept();
			newListener();
			DataInputStream dis = new DataInputStream(connection.getInputStream());
			int message_lenght = dis.readInt();
			byte[] binary_message = new byte[message_lenght];
			dis.readFully(binary_message, 0, binary_message.length); // read the message

			System.out.println("recebido binario com " + binary_message.length + " bytes");
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) {
        try {
        	OutputStream output = socket.getOutputStream();
        	
        	String total_message = message;
        	String encodedString = Base64.getEncoder().encodeToString(total_message.getBytes());
        	
        	PrintWriter writer = new PrintWriter(output, true);
        	writer.println(encodedString);
            
        } catch(IOException e) {
            e.printStackTrace();
        }

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
		
		if (!(last_order == order)) {
			
			//send material order to the IMA
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
		
		Produto produto;
		int a = 0, b = 0, c = 0, d = 0;
		
		for (int i = 0; i < pedido.getProdutos().size(); i++) {
			
			produto = pedido.getProdutos().get(i);
			
			if (produto.getProduto() == "X"){
				// a += 
				// b +=
				// c +=
				// d +=
			}
			else if (produto.getProduto() == "Y"){
				// a += 
				// b +=
				// c +=
				// d +=
			}
			else{
				// a += 
				// b +=
				// c +=
				// d +=
			}
			
			if ( beliefs.a_level < a || beliefs.b_level < b || beliefs.c_level < c || beliefs.d_level < d )
				return false;	
		}
		
		//return true only if MA is available for production
		return true;
	}

	private void execute(Pedido pedido) {
		// Send order to MA
		// maybe tell IMA to allocate the material?
		
		updateBeliefs();
	}

	private void updateBeliefs() {
		// Ask IMA for material stocks:
		// beliefs.a_level = 
		// beliefs.b_level = 
		// beliefs.c_level = 
		// beliefs.d_level = 
	
		//Ask MPA if there are new deliveries
		//beliefs.new_deliveries
		
		//Ask MA if is clear to produce
		//beliefs.MPA_available
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