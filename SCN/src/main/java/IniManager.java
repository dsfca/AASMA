import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class IniManager {
	
	private Ini ini;

	
	public IniManager() throws InvalidFileFormatException, IOException {
		this.ini = new Ini(new File("parametros.ini"));
	}
	
	
	public int getOMAClientPort() {
		return ini.get("OMA","client_port", Integer.class);
	}
	
	public int getOMAServerPort() {
		return ini.get("OMA","server_port", Integer.class);
	}
	
	public String getOMAhost() {
		return ini.get("OMA","host", String.class);
	}
	
	public int getClientPort() {
		return ini.get("Client","port", Integer.class);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
