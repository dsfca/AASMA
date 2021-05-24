package general;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ini4j.InvalidFileFormatException;

import agentes.OMA;

public class SCN extends Thread{
	
	private IniManager ini;
	

	public SCN() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
	}

	public void run() {
		int clientId = 1;
		
		try {
			OMA oma = new OMA();
			oma.start();
			
			while(true) {
				//CRIAR CLIENTES E PEDIDOS
				List <Produto> produtos = generateRandonProductList();
				Pedido pedido = new Pedido(new Timestamp(System.currentTimeMillis()), produtos);
				Client client = new Client(clientId, pedido);
				client.start();
				clientId++;
				//sleep between 5s-10s
				int sleepTime = (int)(Math.random()*5000 + 5000); 
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

	public Produto generateRandomProduct() {
		Random r = new Random();
		int result = r.nextInt(4-1) + 1;
		
		Produto produto = new Produto(generateRandomProductName(), generateRandomProductQuantity());
		return produto;
	}

	public String generateRandomProductName() {
		int max_produtos = ini.getProductMaxN();
		String [] alfa = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		
		Random r = new Random();
		int result = (r.nextInt(max_produtos-1) + 1)%26;
		return alfa[result];
	}
	
	public int generateRandomProductQuantity() {
		int max_quantidade = ini.getProductMaxQ();
		Random r = new Random();
		int result = (r.nextInt(max_quantidade-1) + 1);
		return result;
	}
	
	public List<Produto> generateRandonProductList() {
		List <Produto> produtos = new ArrayList <Produto> ();
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
