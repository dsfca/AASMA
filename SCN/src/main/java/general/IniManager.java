package general;
import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class IniManager {
	
	private Ini ini;

	
	public IniManager() throws InvalidFileFormatException, IOException {
		this.ini = new Ini(new File("parametros.ini"));
	}
	
	/**OMA**/
	
	public int getOMAClientPort() {
		return ini.get("OMA","client_port", Integer.class);
	}
	
	public int getOMAServerPort() {
		return ini.get("OMA","server_port", Integer.class);
	}
	
	public String getOMAHost() {
		return ini.get("OMA","host", String.class);
	}
	
	/**IMA**/
	public int getIMAServerPort() {
		return ini.get("IMA","server_port", Integer.class);
	}
	
	public String getIMAHost() {
		return ini.get("IMA","host", String.class);
	}
	
	public String getInventoryPath() {
		return ini.get("IMA","inventory_path", String.class);
	}
	
	/**Client**/
	public int getClientPort() {
		return ini.get("Client","port", Integer.class);
	}
	
	public String getClientHost() {
		return ini.get("Client", "host", String.class);
	}
	
	/**Produto**/
	public int getProductMaxQ() {
		return ini.get("Produto","max_quantidade", Integer.class);
	}
	
	public int getProductMaxN() {
		return ini.get("Produto","max_produtos_disponibilizados", Integer.class);
	}
	
	/**Pedido**/
	public int getPedidoMaxP() {
		return ini.get("Pedido","max_produtos_na_lista", Integer.class);
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
