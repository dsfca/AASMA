package general;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ini4j.InvalidFileFormatException;

import agentes.IMA;
import agentes.MA;
import agentes.MPA;
import agentes.OMA;
import agentes.PPA;
import agentes.PPA.Desire;

public class SCN extends Thread{
	
	private IniManager ini;
	

	public SCN() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
	}

	public void run() {
		int clientId = 1;
		
		Desire d = Desire.maximizeIncome;
		
		if(ini.getMode().equals("minimizeDeliveryTime")) {
			d = Desire.minimizeDeliveryTime;
		}else if(ini.getMode().equals("maximizeIncome")) {
			d= Desire.maximizeIncome;
		}
		
		try {
			MPA mpa = new MPA();
			mpa.start();
			PPA ppa = new PPA(d);
			ppa.start();
			IMA ima = new IMA();
			ima.start();
			OMA oma = new OMA(0.5f);
			oma.start();
			MA ma = new MA();
			ma.start();
			Supplier s = new Supplier();
			s.start();
			
			while(true) {
				//CRIAR CLIENTES E PEDIDOS
				List <Material> produtos = generateRandonProductList();
				Pedido pedido = new Pedido(new Timestamp(System.currentTimeMillis()), produtos, clientId);
				Client client = new Client(clientId, pedido);
				client.start();
				clientId++;
				//sleep between 5s-10s
				int sleepTime = (int)(Math.random()*5000 + ini.getSleepAddition()); 
				sleep(sleepTime);

			}
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Material generateRandomProduct() {
		Random r = new Random();
		int result = r.nextInt(4-1) + 1;
		
		Material produto = new Material(generateRandomProductName(), generateRandomProductQuantity());
		return produto;
	}

	public String generateRandomProductName() {
		int max_produtos = ini.getProductMaxN();
		String [] alfa = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		
		Random r = new Random();
		int result = (r.nextInt(max_produtos-1))%26;
		return alfa[result];
	}
	
	public int generateRandomProductQuantity() {
		int max_quantidade = ini.getProductMaxQ();
		Random r = new Random();
		int result = (r.nextInt(max_quantidade-1) + 1);
		return result;
	}
	
	public List<Material> generateRandonProductList() {
		List <Material> produtos = new ArrayList <Material> ();
		int max_produtos = ini.getPedidoMaxP();
		Random r = new Random();
		int result = (r.nextInt(max_produtos-1) + 1);
		
		for (int i = 0; i < result; i++) {
			produtos.add(generateRandomProduct());
		}
		return produtos;
	}

	public static void main(String[] args) throws InvalidFileFormatException, IOException {
		SCN scn = new SCN();
		scn.start();

	}

}
