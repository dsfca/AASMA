import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
	
	private Timestamp data_rececao;
	private Timestamp data_limite;
	private List <Produto> produtos;
	private EstadoPedido estado;
	private int TotalPrice;

	public Pedido(Timestamp data_rececao, List <Produto> produtos) {
		this.data_rececao = data_rececao;
		// data limite
		this.produtos = produtos;
		this.estado = EstadoPedido.PEDIDO;
	}
	
	/**SETTERS*/
	public void setEstado(int estado) {
		if (estado == 1) {
			this.estado = EstadoPedido.PLANEADO;
		}
		else if (estado == 2) {
			this.estado = EstadoPedido.ALOCADO;
		}
		else if (estado == 3) {
			this.estado = EstadoPedido.ENTREGUE;
		}
	}
	
	public void setDataLimite(Timestamp data_limite) {
		this.data_limite = data_limite;
	}
	
	/**GETTERS*/
	public Timestamp getDataRececao() {
		return this.data_rececao;
	}

	public Timestamp getDataLimite() {
		return this.data_limite;
	}
	
	public List <Produto> getProdutos(){
		return this.produtos;
	}
	
	public EstadoPedido getEstado() {
		return this.estado;
	}
	
	public String toString() {
		return (this.data_rececao + " " + this.data_limite + " " + this.produtos + " " + this.estado);
	}
	
	public static void main(String[] args) {
		Produto prod = new Produto("A", 1);
		List <Produto> produtos =  new ArrayList <Produto>();
		produtos.add(prod);
		produtos.add(prod);
		Pedido p = new Pedido(null, produtos);
		System.out.println(p);

	}
	
	public int getTotalPrice() {
		return TotalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		TotalPrice = totalPrice;
	}

	enum EstadoPedido { //ALTERAR
		PEDIDO,
		PLANEADO,
		ALOCADO,
		ENTREGUE;
	}

}
