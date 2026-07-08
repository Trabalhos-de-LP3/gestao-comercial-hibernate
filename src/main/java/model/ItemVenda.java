package model;

import exception.NegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_venda")
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private int quantidade;

    @Column(name = "preco_unitario")
    private double precoUnitario;

    private double subtotal;

    public ItemVenda() {
    }

    public ItemVenda(Produto produto, int quantidade) {
        if (!produto.podeSerVendido()) {
            throw new NegocioException("Produto inativo não pode ser vendido.");
        }
        if (quantidade <= 0) {
            throw new NegocioException("Quantidade do item deve ser maior que zero.");
        }
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPrecoVenda();
        calcularSubtotal();
    }

    public void calcularSubtotal() {
        this.subtotal = quantidade * precoUnitario;
    }

    public int getId() {
        return id;
    }

    public Venda getVenda() {
        return venda;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new NegocioException("Quantidade do item deve ser maior que zero.");
        }
        this.quantidade = quantidade;
        calcularSubtotal();
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
        calcularSubtotal();
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return produto.getNome() + " | Qtd: " + quantidade
                + " | Unit: R$ " + precoUnitario
                + " | Subtotal: R$ " + subtotal;
    }
}
