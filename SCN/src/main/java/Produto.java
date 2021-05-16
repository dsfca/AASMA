
public class Produto {
	
	private String produto;
	private int quantidade;
	private boolean alocado;

	public Produto(String produto, int quantidade) {
		this.produto = produto;
		this.quantidade = quantidade;
		this.alocado = false;
	}
	
	/**SETTERS*/
	public void setAlocado(boolean alocado) {
		this.alocado = alocado;
	}
	
	/**GETTERS*/
	public String getProduto() {
		return this.produto;
	}
	
	public int getQuantidade() {
		return this.quantidade;
	}
	
	public boolean getAlcoado() {
		return this.alocado;
	}
	
	public String toString() {
		return (this.produto + " " + this.quantidade + " " + this.alocado);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
