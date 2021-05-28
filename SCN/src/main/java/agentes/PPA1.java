package agentes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
				//Decision();
			
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
					//add lista???
				
				}else if(object[0].equals("ma_v")) { //vaziu
					//ESPERAR E ENTREGAR MA
					while(this.plan.isEmpty()) {
						System.out.println("Waiting for request " + Thread.currentThread().getId());
						wait();
						System.out.println("Stop wait " + Thread.currentThread().getId());
					}
					Pedido pedido = editPlan(5, null, null); //retira pedido e devolve
					objectOutputStream.writeObject(pedido);
				}else if(object[0].equals("ma_p")) { //com pedido
					Pedido pedido_final = (Pedido) objectInputStream.readObject();
					//ENTREGAR PROXIMO PEDIDO MA E FECHAR
					Pedido pedido = editPlan(5, null, null);
					objectOutputStream.writeObject(pedido);
					//ENTREGAR OMA
					Object [] object_final = {"pronto", pedido_final};
					Socket omaSocket = (Socket)new Socket(ini.getOMAHost(), ini.getOMAServerPort());
					ObjectOutputStream imaObjectOutputStream = new ObjectOutputStream(omaSocket.getOutputStream());
					ObjectInputStream imaObjectInputStream = new ObjectInputStream(omaSocket.getInputStream());
					imaObjectOutputStream.writeObject(object_final);
				}

				//ENVIAR PARA OMA
				//produto que devia vir do MA
				Produto produto = new Produto("XXX", 1);
				objectOutputStream.writeObject(produto);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized Pedido editPlan(int mode, Pedido pedido, List<Pedido> queue_aux) {
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
		}
		return pedido;
	}
	
	public enum Desire {maximizeIncome, minimizeDeliveryTime};

	public static void main(String[] args) throws InvalidFileFormatException, IOException {
		PPA1 ppa = new PPA1(Desire.maximizeIncome);
		ppa.start();

	}

}
