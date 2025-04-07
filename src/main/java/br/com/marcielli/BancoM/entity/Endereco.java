package br.com.marcielli.BancoM.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Endereco {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String cep;
	private String cidade;
	private String estado;
	private String rua;
	private String numero;
	private String bairro;
	private String complemento;
	
	@OneToOne(mappedBy = "endereco")
	@JsonBackReference
	private Cliente cliente;
	
	public Endereco() {}
	
	public Endereco(Long id, String cep, String cidade, String estado, String rua, String numero, String bairro,
			String complemento, Cliente cliente) {
		super();
		this.id = id;
		this.cep = cep;
		this.cidade = cidade;
		this.estado = estado;
		this.rua = rua;
		this.numero = numero;
		this.bairro = bairro;
		this.complemento = complemento;
		this.cliente = cliente;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	public String getCep() {
		return cep;
	}
	public void setCep(String cep) {
		this.cep = cep;
	}
	public String getCidade() {
		return cidade;
	}
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public String getRua() {
		return rua;
	}
	public void setRua(String rua) {
		this.rua = rua;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		this.numero = numero;
	}
	public String getBairro() {
		return bairro;
	}
	public void setBairro(String bairro) {
		this.bairro = bairro;
	}
	public String getComplemento() {
		return complemento;
	}
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	@Override
	public String toString() {
		return "Endereco [id=" + id + ", cep=" + cep + ", cidade=" + cidade + ", estado=" + estado + ", rua=" + rua
				+ ", numero=" + numero + ", bairro=" + bairro + ", complemento=" + complemento + ", cliente=" + cliente
				+ "]";
	}	
	
}
