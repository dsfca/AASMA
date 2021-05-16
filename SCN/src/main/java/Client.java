import java.io.IOException;

import org.ini4j.InvalidFileFormatException;

public class Client {
	
	private static int PORT;
	private IniManager ini;

	public Client() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
		this.PORT = ini.getClientPort();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
