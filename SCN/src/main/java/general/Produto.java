package general;

public class Produto {
	
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
