package general;

import java.util.HashMap;

public class Belief {
	
	public Belief(){
		quantidades = new HashMap<Material, Integer>();
	}
	
	public HashMap<Material, Integer> quantidades;
	
	public boolean MA_available;
	
}