package general;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.ini4j.InvalidFileFormatException;

import general.IniManager;

public class Supplier extends Thread{
	
	private IniManager ini;
	
	void Supplier() throws InvalidFileFormatException, IOException {
		this.ini = new IniManager();
	}
	

}
