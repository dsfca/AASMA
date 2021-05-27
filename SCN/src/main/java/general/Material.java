package general;

import java.io.Serializable;

public class Material implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String material;
	private int quantidade;
	private boolean alocado;

	public Material(String material, int quantidade) {
		this.material = material;
		this.quantidade = quantidade;
		this.alocado = false;
	}
	
	/**SETTERS*/
	public void setAlocado(boolean alocado) {
		this.alocado = alocado;
	}
	
	/**GETTERS*/
	public String getMaterial() {
		return this.material;
	}
	
	public int getQuantidade() {
		return this.quantidade;
	}
	
	public boolean getAlcoado() {
		return this.alocado;
	}
	
	public String toString() {
		return (this.material + " " + this.quantidade + " " + this.alocado);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
