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
	
	/**PPA**/
	public int getPPAClientPort() {
		return ini.get("PPA","client_port", Integer.class);
	}
	
	public int getPPAServerPort() {
		return ini.get("PPA","server_port", Integer.class);
	}
	
	public String getPPAHost() {
		return ini.get("PPA","host", String.class);
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
	
	/**MPA**/
	public int getMPAClientPort() {
		return ini.get("MPA","client_port", Integer.class);
	}
	
	public int getMPAServerPort() {
		return ini.get("MPA","server_port", Integer.class);
	}
	
	public String getMPAHost() {
		return ini.get("MPA","host", String.class);
	}
	
	/**Client**/
	public int getClientPort() {
		return ini.get("Client","port", Integer.class);
	}
	
	public String getClientHost() {
		return ini.get("Client", "host", String.class);
	}
	
	/**Supplier**/
	public int getSupplierClientPort() {
		return ini.get("Supplier","client_port", Integer.class);
	}
	
	public int getSupplierServerPort() {
		return ini.get("Supplier","server_port", Integer.class);
	}
	
	public String getSupplierHost() {
		return ini.get("Supplier", "host", String.class);
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
