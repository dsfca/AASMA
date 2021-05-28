package agentes;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;
import general.Material;

public class OMA extends Thread {

	private IniManager ini;
	
	private ServerSocket ssocket;
	/*private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	
	private Socket imaSocket;
	private ObjectOutputStream imaObjectOutputStream;
	private ObjectInputStream imaObjectInputStream;
	
	private Socket ppaSocket;
	private ObjectOutputStream ppaObjectOutputStream;
	private ObjectInputStream ppaObjectInputStream;
	*/
	//performance
	private int requestsReceived;
	private int requestsDelivered;
	private int datasCumpridas;
	private int moneyReceived;
	
	private int id;
	
	private int averageQueueTime;
	private int averageQueueTimePerMaterialAvailability;
	private int pendingOrders;
	
	public OMA() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getOMAServerPort());
		this.id = 0;
		//this.averageQueueTimePerMaterialAvailability = 1000;
		this.pendingOrders = 0;
		
		//performance
		this.requestsReceived = 0;
		this.requestsDelivered = 0;
		this.datasCumpridas = 0;
		this.moneyReceived = 0;
		/**
		this.imaSocket = (Socket)new Socket(ini.getIMAHost(), ini.getIMAServerPort());
		this.imaObjectOutputStream = new ObjectOutputStream(this.imaSocket.getOutputStream());
		this.imaObjectInputStream = new ObjectInputStream(this.imaSocket.getInputStream());
		
		this.ssocket = new ServerSocket(ini.getOMAServerPort());
		*/
		/*this.ppaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
		this.ppaObjectOutputStream = new ObjectOutputStream(this.ppaSocket.getOutputStream());
		this.ppaObjectInputStream = new ObjectInputStream(this.ppaSocket.getInputStream());*/
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}
	
	//CUIDADO COM OS OOS OIS PARA VARIOS CLIENTES
	public void run() {
		try {
			//FROM CLIENTS
			int my_id = ++id;
			Socket generalSocket = ssocket.accept();
			newListener();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(generalSocket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(generalSocket.getInputStream());
			
			Socket imaSocket = (Socket) new Socket(ini.getIMAHost(), ini.getIMAServerPort());
			ObjectOutputStream imaObjectOutputStream = new ObjectOutputStream(imaSocket.getOutputStream());
			ObjectInputStream imaObjectInputStream = new ObjectInputStream(imaSocket.getInputStream());
			System.out.println("OMA: connected streams successfully");
			
			Socket ppaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
			ObjectOutputStream ppaObjectOutputStream = new ObjectOutputStream(ppaSocket.getOutputStream());
			ObjectInputStream ppaObjectInputStream = new ObjectInputStream(ppaSocket.getInputStream());

			//RECEBER BASE
			Object [] object = (Object[]) objectInputStream.readObject();
			
			//VINDO DO CLIENTE
			if (object[0].equals("c")) {
				//ENVIAR IMA
				/** */setRequestsReceived(); 
				Pedido pedido = (Pedido) object[1];
				Object [] object_enviar = {"ci", pedido.getMateriais()};
				imaObjectOutputStream.writeObject(object_enviar);
				HashMap <Material, Integer> quantidades = (HashMap<Material, Integer>) imaObjectInputStream.readObject();
				System.out.println(quantidades);
				objectOutputStream.writeObject(" a aguardar pedido");
				//(ESTIMAR DATA ENTREGA
				pedido.setDataLimite(new Timestamp(System.currentTimeMillis()+10000));
				//ENVIAR PPA
				//ppaObjectOutputStream.writeObject(pedido);
			
			//VINDO DO PPA
			}else if(object[0].equals("pronto")) {
				Pedido pedido = (Pedido) object[1];
				Socket enviar_cliente = (Socket) new Socket(ini.getClientHost(), ini.getClientPort() + pedido.getClientId());
				ObjectOutputStream clientObjectOutputStream = new ObjectOutputStream(enviar_cliente.getOutputStream());
				ObjectInputStream clientObjectInputStream = new ObjectInputStream(enviar_cliente.getInputStream());
				//ENVIAR PEDIDO AO CLIENTE
				clientObjectOutputStream.writeObject(pedido);
				//RECEBER DINHEIRO
				int final_price = (int) clientObjectInputStream.readObject();
				/** */setRequestsDelivered();
				/** */setMoneyReceived(final_price);
				if(pedido.getDataLimite().compareTo(new Timestamp(System.currentTimeMillis())) > 0) {
					/** */setDatasCumpridas();
				}
				clientObjectOutputStream.close();
				clientObjectInputStream.close();
				enviar_cliente.close();
			}
			//(RECEBER RESPOSTA
			//Produto produto_final = objectInputStream.readObject();
			//(RESPONDER CLIENTE (SERIA GIRO UMA INTERFACE COM OS CLIENTES EM ESPERA?)
			/**objectOutputStream.writeObject(object);*/
			pendingOrders++;
			System.out.println("OMA" + my_id + ": Terminou");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Timestamp estimateDeliveryDate(Pedido pedido, HashMap<Material, Integer> quantidades) {
		// TODO Auto-generated method stub
		List <Material> required_material = pedido.getMateriais();
		
		int estimated_production_time = 0;
		int estimated_queue_time = 0;
		
		int remaining_material_quantity = 0;
		
		for(int i = 0; i < required_material.size(); i++) {
			estimated_production_time += (Character.getNumericValue(required_material.get(i).getMaterial().charAt(i)) - 9) * required_material.get(i).getQuantidade();
			//TODO after fixing PPA, maybe reajust for material availability and total price
			//remaining_material_quantity = required_material.get(i).getQuantidade() - quantidades.get(required_material.get(i).getMaterial());
			//remaining_material_quantity = (remaining_material_quantity < 0) ? 0 : remaining_material_quantity;
			estimated_queue_time = averageQueueTime;
		}
		
		Long estimated_time = (long) (estimated_production_time + estimated_queue_time);
		
		Long estimated_date_in_millisec = pedido.getDataRececao().getTime() + estimated_time;
		Timestamp estimated_date = new Timestamp(estimated_date_in_millisec);
			
		return estimated_date;
	}

	//TODO When an order is completed:
	// decrement pendingOrders
	// reajust averageQueueTime (order.queueTime() = order.totalTime() - order.productionTime())
	
	public void receiveFromClient() {
		
	}
	
	/*public void enviarPedidoPPA(Pedido pedido) throws IOException {
		this.objectOutputStream.writeObject(pedido);
	}*/
	
	//PARA ESTIMAR DATA
	/*public HashMap<Produto, Integer> queryIMAavailability(Pedido pedido) throws IOException, ClassNotFoundException {
		HashMap <Produto, Integer> quantidades = new HashMap<Produto, Integer>();
		
		for (Produto produto: pedido.getProdutos()) {
			this.imaObjectOutputStream.writeObject(produto);
			int quantidade = (Integer) this.imaObjectInputStream.readObject();
			System.out.println(quantidade);
			quantidades.put(produto, quantidade);
		}
		return quantidades;
	}*/
/*	
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
*/
	
	private synchronized void setRequestsReceived() {
		this.requestsReceived++;
	}
	
	private synchronized void setRequestsDelivered() {
		this.requestsDelivered++;
	}
	
	private synchronized void setDatasCumpridas() {
		this.datasCumpridas++;
	}
	
	private synchronized void setMoneyReceived(int money) {
		this.moneyReceived = this.moneyReceived + money;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}