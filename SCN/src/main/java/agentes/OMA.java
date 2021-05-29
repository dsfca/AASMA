package agentes;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;
import general.Pedido;
import general.Material;

public class OMA extends Thread {

	private IniManager ini;
	
	private ServerSocket ssocket;
	
	//performance
	private int requestsReceived;
	private int requestsDelivered;
	private int datasCumpridas;
	private int moneyReceived;
	
	private int id;
	
	private float discount_factor;
	private int averageQueueTime;
	
	private JFrame frame;
	private JTextField recebidos_value;
	private JTextField entregues_value;
	private JTextField datas_value;
	private JTextField money_value;

	
	/**
	 * RECEBE
	 * "c", "pronto"
	 * 
	 * ENVIA
	 * "oma", "ci"
	 * 	 
	 */
	
	public OMA(float df) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getOMAServerPort());
		this.id = 0;
		this.averageQueueTime = 0;
		
		//performance
		this.requestsReceived = 0;
		this.requestsDelivered = 0;
		this.datasCumpridas = 0;
		this.moneyReceived = 0;
		this.discount_factor = df;
		showUI();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(400, 450);
		frame.setResizable(false);
		frame.setVisible(true);
		
	}

	public void showUI(){
		this.frame = new JFrame("Performance");
		frame.setLayout(new FlowLayout());
		JPanel painel = new JPanel();
		painel.setLayout(new GridLayout(5,1));

		//Recebidos
		JLabel recebidos = new JLabel("Pedidos Recebidos:");
		this.recebidos_value = new JTextField();
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(1,2));
		center.add(recebidos);
		center.add(recebidos_value);
		//Entregues
		JLabel entregues = new JLabel("Pedidos Entregues:");
		this.entregues_value = new JTextField();
		JPanel center2 = new JPanel();
		center2.setLayout(new GridLayout(1,2));
		center2.add(entregues);
		center2.add(entregues_value);
		//Datas Cumpridas
		JLabel datas = new JLabel("Datas Cumpridas:");
		this.datas_value = new JTextField();
		JPanel center3 = new JPanel();
		center3.setLayout(new GridLayout(1,2));
		center3.add(datas);
		center3.add(datas_value);
		//Dinheiro Recebido
		JLabel money = new JLabel("Dinheiro Recebido:");
		this.money_value = new JTextField();
		JPanel center4 = new JPanel();
		center4.setLayout(new GridLayout(1,2));
		center4.add(money);
		center4.add(money_value);
		//Dinheiro Recebido
		JLabel modo = new JLabel("Modo de Operacao: "+String.valueOf(ini.getMode()));
		JPanel center5 = new JPanel();
		center5.setLayout(new GridLayout(1,1));
		center5.add(modo);

		painel.add(center);
		painel.add(center2);
		painel.add(center3);
		painel.add(center4);
		painel.add(center5);
		frame.add(painel);
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
			
			//RECEBER BASE
			Object [] object = (Object[]) objectInputStream.readObject();
			
			//VINDO DO CLIENTE
			if (object[0].equals("c")) {
				//ENVIAR IMA
				/** */setRequestsReceived(); 
				Pedido pedido = (Pedido) object[1];
				Object [] object_enviar = {"ci", pedido.getMateriais()};
	
				Socket imaSocket = (Socket) new Socket(ini.getIMAHost(), ini.getIMAServerPort());
				ObjectOutputStream imaObjectOutputStream = new ObjectOutputStream(imaSocket.getOutputStream());
				ObjectInputStream imaObjectInputStream = new ObjectInputStream(imaSocket.getInputStream());
				
				imaObjectOutputStream.writeObject(object_enviar);
				HashMap <Material, Integer> quantidades = (HashMap<Material, Integer>) imaObjectInputStream.readObject();
				//System.out.println(quantidades);
				//ESTIMAR DATA
				pedido.setDataLimite(estimateDeliveryDate(pedido, quantidades));
				//CALCULAR PRE�O
				pedido.setTotalPrice(calculatePrice(pedido.getMateriais()));
				//ENVIAR PPA
				Object [] object_ppa = {"oma", pedido};
				
				Socket ppaSocket = (Socket)new Socket(ini.getPPAHost(), ini.getPPAServerPort());
				ObjectOutputStream ppaObjectOutputStream = new ObjectOutputStream(ppaSocket.getOutputStream());
				ObjectInputStream ppaObjectInputStream = new ObjectInputStream(ppaSocket.getInputStream());
				
				System.out.println("Cliente"+ pedido.getClientId() + " fez pedido: " + pedido.toString());
				ppaObjectOutputStream.writeObject(object_ppa);
				//CLIENTE AGUARDE
				objectOutputStream.writeObject(" a aguardar pedido");
				closeSocket(objectOutputStream, objectInputStream, generalSocket);
				closeSocket(imaObjectOutputStream, imaObjectInputStream, imaSocket);
				closeSocket(ppaObjectOutputStream, ppaObjectInputStream, ppaSocket);
			
			//VINDO DO PPA
			}else if(object[0].equals("pronto")) {
				Pedido pedido = (Pedido) object[1];
				closeSocket(objectOutputStream, objectInputStream, generalSocket);
				Socket enviar_cliente = (Socket) new Socket(ini.getClientHost(), ini.getClientPort() + pedido.getClientId());
				ObjectOutputStream clientObjectOutputStream = new ObjectOutputStream(enviar_cliente.getOutputStream());
				ObjectInputStream clientObjectInputStream = new ObjectInputStream(enviar_cliente.getInputStream());
				//ENVIAR PEDIDO AO CLIENTE
				System.out.println("Pedido " + pedido.getProdutoFinal().getProduto() + " entregue a cliente" + pedido.getClientId());
				clientObjectOutputStream.writeObject(pedido);
				//RECEBER DINHEIRO
				int final_price = (int) clientObjectInputStream.readObject();
				/** */setRequestsDelivered();
				/** */setMoneyReceived(final_price);
				Timestamp deliver_date = new Timestamp(System.currentTimeMillis());
				if(pedido.getDataLimite().compareTo(deliver_date) > 0) {
					/** */setDatasCumpridas();
				}
				adjustEstimate(pedido, deliver_date, final_price);
				closeSocket(clientObjectOutputStream, clientObjectInputStream, enviar_cliente);
			}
			//(RESPONDER CLIENTE (SERIA GIRO UMA INTERFACE COM OS CLIENTES EM ESPERA?)

		} catch (IOException e) {
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		}
	}
	
	private int calculatePrice(List<Material> materiais) {
		int price = 0;
		for (Material material: materiais) {
			price = price + (Character.getNumericValue(material.getMaterial().charAt(0)) - 9) * 10 * material.getQuantidade();
		}
		return price;
	}

	public void closeSocket(ObjectOutputStream oo, ObjectInputStream oi, Socket s) throws IOException {
		oo.close();
		oi.close();
		s.close();
	}

	private Timestamp estimateDeliveryDate(Pedido pedido, HashMap<Material, Integer> quantidades) {
		List <Material> required_material = pedido.getMateriais();
		
		int estimated_production_time = 0;
		int estimated_queue_time = 0;

		int remaining_material_quantity = 0;

		for(Material material: required_material) {
			estimated_production_time += (Character.getNumericValue(material.getMaterial().charAt(0)) - 9) * material.getQuantidade();
			estimated_queue_time = averageQueueTime;
		}

		Long estimated_time = (long) (estimated_production_time + estimated_queue_time);

		Long estimated_date_in_millisec = pedido.getDataRececao().getTime() + estimated_time;
		Timestamp estimated_date = new Timestamp(estimated_date_in_millisec);
			
		return estimated_date;
	}

	public void adjustEstimate(Pedido pedido, Timestamp deliver_date, int price) {
		
		List <Material> required_material = pedido.getMateriais();
		int estimated_production_time = 0;
		
		for(Material material: required_material) {
			estimated_production_time += (Character.getNumericValue(material.getMaterial().charAt(0)) - 9) * material.getQuantidade();
		}
		
		long delivery_time = deliver_date.getTime() - pedido.getDataRececao().getTime();
		long queue_time = delivery_time - estimated_production_time;
		
		long estimated_delivery_time = pedido.getDataLimite().getTime() - pedido.getDataRececao().getTime();
		long estimated_queue_time = estimated_delivery_time - estimated_production_time;
		
		long deviation = queue_time - estimated_queue_time;
		
		averageQueueTime += discount_factor*deviation;
	}
	
	private synchronized void setRequestsReceived() {
		this.requestsReceived++;
		this.recebidos_value.setText(String.valueOf(requestsReceived));
	}
	
	private synchronized void setRequestsDelivered() {
		this.requestsDelivered++;
		this.entregues_value.setText(String.valueOf(requestsDelivered));
	}
	
	private synchronized void setDatasCumpridas() {
		this.datasCumpridas++;
		this.datas_value.setText(String.valueOf(datasCumpridas));
	}
	
	private synchronized void setMoneyReceived(int money) {
		this.moneyReceived = this.moneyReceived + money;
		this.money_value.setText(String.valueOf(moneyReceived));

	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}