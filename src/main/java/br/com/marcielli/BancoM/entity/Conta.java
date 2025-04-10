package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Conta implements Serializable { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
//	@ManyToOne(cascade = {CascadeType.ALL}) 
	@ManyToOne
	@JoinColumn(name = "clienteId")
	@JsonBackReference
	private Cliente cliente;
	
	@Enumerated(EnumType.STRING)
	private TipoConta tipoConta;
	
	@Enumerated(EnumType.STRING)
	private CategoriaConta categoriaConta;
	
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "taxasId")
	private List<Taxas> taxas;
	
	private float saldoConta;	
	
	@JsonIgnore
	@Transient
	private float valorTransferencia;
	
	private String numeroConta;	
	
	@Column(name = "chave_pix")
	private String pixAleatorio;
	
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "transferenciaId")
	private List<Transferencia> transferencia;
	
	@OneToMany(mappedBy = "conta", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonManagedReference
	private List<Cartao> cartoes;
	
	
	private boolean status;
	
	public Conta() {}

	public Conta(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta,
			float saldoConta, String numeroConta, List<Taxas> taxas) {
		super();	
		this.cliente = cliente;
		this.tipoConta = tipoConta;
		this.categoriaConta = categoriaConta;
		this.saldoConta = saldoConta;
		this.numeroConta = numeroConta;
		this.taxas = taxas;
	}

	public Conta(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta,
			float saldoConta, String numeroConta) {
		super();	
		this.cliente = cliente;
		this.tipoConta = tipoConta;
		this.categoriaConta = categoriaConta;
		this.saldoConta = saldoConta;
		this.numeroConta = numeroConta;
	}
	
	public Conta(Cliente cliente, TipoConta tipoConta,float saldoConta, String numeroConta) {
		super();	
		this.cliente = cliente;
		this.tipoConta = tipoConta;	
		this.saldoConta = saldoConta;
		this.numeroConta = numeroConta;
	}
	
	
	
	
	
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getPixAleatorio() {
		return pixAleatorio;
	}

	public void setPixAleatorio(String pixAleatorio) {		
		this.pixAleatorio = pixAleatorio;
	}

	public float getValorTransferencia() {
		return valorTransferencia;
	}


	public void setValorTransferencia(float valorTransferencia) {
		this.valorTransferencia = valorTransferencia;
	}


	public List<Transferencia> getTransferencia() {
		return transferencia;
	}


	public void setTransferencia(List<Transferencia> transferencia) {
		this.transferencia = transferencia;
	}


	public Cliente getCliente() {
		return cliente;
	}


	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}


	public TipoConta getTipoConta() {
		return tipoConta;
	}


	public void setTipoConta(TipoConta tipoConta) {
		this.tipoConta = tipoConta;
	}


	public CategoriaConta getCategoriaConta() {		
		return categoriaConta;
	}


	public void setCategoriaConta(CategoriaConta categoriaConta) {
		this.categoriaConta = categoriaConta;
	}	
	
	public float getSaldoConta() {
		return saldoConta;
	}

	public void setSaldoConta(float saldoConta) {
		this.saldoConta = saldoConta;
	}


	public String getNumeroConta() {
		return numeroConta;
	}


	public void setNumeroConta(String numeroConta) {
		this.numeroConta = numeroConta;
	}


	public Long getId() {
		return id;
	}


	public List<Taxas> getTaxas() {
		return taxas;
	}


	public void setTaxas(List<Taxas> taxas) {
		this.taxas = taxas;
	}

	public List<Cartao> getCartoes() {
		return cartoes;
	}

	public void setCartoes(List<Cartao> cartoes) {
		this.cartoes = cartoes;
	}

	@Override
	public String toString() {
		return "Conta [id=" + id + ", version=" + version + ", cliente=" + cliente + ", tipoConta=" + tipoConta
				+ ", categoriaConta=" + categoriaConta + ", saldoConta=" + saldoConta + ", valorTransferencia="
				+ valorTransferencia + ", numeroConta=" + numeroConta + ", transferencia=" + transferencia + "]";
	}




}
