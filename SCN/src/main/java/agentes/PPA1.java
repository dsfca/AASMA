package agentes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ini4j.InvalidFileFormatException;

import agentes.PPA.Desire;
import general.IniManager;
import general.Material;
import general.Pedido;
import general.Produto;
import general.SortbyDate;
import general.SortbyPrice;

public class PPA1 extends Thread {
	
	private ServerSocket ssocket;
	private IniManager ini;
	private int id_deliberative;
	public List<Pedido> plan;
	
	//DELIBERATIVE
	public Desire desire;

	public PPA1(Desire desire) throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.ssocket = new ServerSocket(ini.getPPAServerPort());
		this.id_deliberative = -1;
		this.plan = new ArrayList<Pedido>();
		this.desire = desire;
	}
	
	private void newListener()
	{
		(new Thread(this)).start();
	}

	public void run() {
		try {
			
			if(this.id_deliberative == -1) {
				System.out.println("Delibertive id: " + Thread.currentThread().getId());
				this.id_deliberative = (int) Thread.currentThread().getId();
				newListener();
			}
			if (Thread.currentThread().getId() == this.id_deliberative) {
				Decision();
			
			}else{
				System.out.println("Registration Thread:" + Thread.currentThread().getId());
				Socket generalSocket = ssocket.accept();
				sleep(1000);
				newListener();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(generalSocket.getOutputStream());
				ObjectInputStream objectInputStream = new ObjectInputStream(generalSocket.getInputStream());
				
				//RECEBER BASE
				Object [] object = (Object[]) objectInputStream.readObject();

				if(object[0].equals("oma")) {
					Pedido pedido = (Pedido) object[1];
					closeSocket(objectOutputStream, objectInputStream, generalSocket);
					//add lista???
				
				}else if(object[0].equals("ma_v")) { //vaziu
					//ESPERAR E ENTREGAR MA
					while(this.plan.isEmpty()) {
						System.out.println("Waiting for request " + Thread.currentThread().getId());
						wait();
						System.out.println("Stop wait " + Thread.currentThread().getId());
					}
					Pedido pedido = editPlan(5, null, null, 0); //retira pedido e devolve
					objectOutputStream.writeObject(pedido);
					closeSocket(objectOutputStream, objectInputStream, generalSocket);
				
				}else if(object[0].equals("ma_p")) { //com pedido
					Pedido pedido_final = (Pedido) objectInputStream.readObject();
					//ENTREGAR PROXIMO PEDIDO MA E FECHAR
					Pedido pedido = editPlan(5, null, null, 0);
					objectOutputStream.writeObject(pedido);
					closeSocket(objectOutputStream, objectInputStream, generalSocket);
					//ENTREGAR OMA
					Object [] object_final = {"pronto", pedido_final};
					Socket omaSocket = (Socket)new Socket(ini.getOMAHost(), ini.getOMAServerPort());
					ObjectOutputStream omaObjectOutputStream = new ObjectOutputStream(omaSocket.getOutputStream());
					ObjectInputStream omaObjectInputStream = new ObjectInputStream(omaSocket.getInputStream());
					omaObjectOutputStream.writeObject(object_final);
					closeSocket(omaObjectOutputStream, omaObjectInputStream, omaSocket);
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
	
	public void closeSocket(ObjectOutputStream oo, ObjectInputStream oi, Socket s) throws IOException {
		oo.close();
		oi.close();
		s.close();
	}
	
	public void Decision() throws UnknownHostException, IOException, ClassNotFoundException {
		//while(true) {
		Socket mpaSocket = (Socket)new Socket(ini.getMPAHost(), ini.getMPAServerPort());
		ObjectOutputStream mpaObjectOutputStream = new ObjectOutputStream(mpaSocket.getOutputStream());
		ObjectInputStream mpaObjectInputStream = new ObjectInputStream(mpaSocket.getInputStream());
		
		Pedido pedido = editPlan(6, null, null, 0); //modo, X, X, index_do_plan
		if(!(pedido==null)) {
			mpaObjectOutputStream.writeObject(pedido.getMateriais());
			HashMap <Material, Integer> quantidades = (HashMap<Material, Integer>) mpaObjectInputStream.readObject();
		} else {
			System.out.println("PPA WARNING: plan still null");
		}
		//}
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
