package general;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Pedido implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timestamp data_rececao;
	private Timestamp data_limite;
	private List <Material> materiais;
	private Material produto_final;
	private EstadoPedido estado;
	private int totalPrice;

	public Pedido(Timestamp data_rececao, List <Material> materiais) {
		this.data_rececao = data_rececao;
		// data limite
		this.materiais = materiais;
		this.estado = EstadoPedido.PEDIDO;
		this.produto_final = null;
	}
	
	public Pedido(List <Material> materiais) {
		this.materiais = materiais;
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
	
	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	public void setProdutoFinal(Material produto_final) {
		this.produto_final = produto_final;
	}
	/**GETTERS*/
	public Timestamp getDataRececao() {
		return this.data_rececao;
	}

	public Timestamp getDataLimite() {
		return this.data_limite;
	}
	
	public List <Material> getMateriais(){
		return this.materiais;
	}
	
	public EstadoPedido getEstado() {
		return this.estado;
	}
	
	public String toString() {
		return (this.data_rececao + " " + this.data_limite + " " + this.materiais + " " + this.estado);
	}
	
	public Material getProdutoFinal() {
		return this.produto_final;
	}
	
	public static void main(String[] args) {
		Material prod = new Material("A", 1);
		List <Material> produtos =  new ArrayList <Material>();
		produtos.add(prod);
		produtos.add(prod);
		Pedido p = new Pedido(null, produtos);
		System.out.println(p);

	}

	enum EstadoPedido { //ALTERAR
		PEDIDO,
		PLANEADO,
		ALOCADO,
		ENTREGUE;
	}

}
