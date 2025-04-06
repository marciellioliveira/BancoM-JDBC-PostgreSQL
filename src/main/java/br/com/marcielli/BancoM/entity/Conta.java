package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.Funcao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
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
	
	@ManyToOne(cascade = {CascadeType.ALL}) 
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
	
	private float valorTransferencia;
	
	private String numeroConta;
	
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "transferenciaId")
	private List<Transferencia> transferencia;
	
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


	@Override
	public String toString() {
		return "Conta [id=" + id + ", version=" + version + ", cliente=" + cliente + ", tipoConta=" + tipoConta
				+ ", categoriaConta=" + categoriaConta + ", saldoConta=" + saldoConta + ", valorTransferencia="
				+ valorTransferencia + ", numeroConta=" + numeroConta + ", transferencia=" + transferencia + "]";
	}




}
