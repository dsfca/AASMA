package general;

import java.io.Serializable;

public class Produto implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String produto;
	private int quantidade;

	public Produto(String produto, int quantidade) {
		this.produto = produto;
		this.quantidade = quantidade;
	}
	
	public String getProduto() {
		return this.produto;
	}
	
	public int getQuantidade() {
		return this.quantidade;
	}

	public static void main(String[] args) {
		

	}

}