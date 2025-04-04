package br.com.marcielli.BancoM.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
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
	
	@ManyToOne(cascade = CascadeType.ALL) 
	@JoinColumn(name = "clienteId")
	@JsonBackReference
	private Cliente cliente;
	
	@Enumerated(EnumType.STRING)
	private TipoConta tipoConta;
	
	@Enumerated(EnumType.STRING)
	private CategoriaConta categoriaConta;
	
	private float saldoConta;
	
	private String numeroConta;
	
	
	public Conta() {}


	public Conta(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta,
			float saldoConta, String numeroConta) {
		super();	
		this.cliente = cliente;
		this.tipoConta = tipoConta;
		this.categoriaConta = categoriaConta;
		this.saldoConta = saldoConta;
		this.numeroConta = numeroConta;
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


	public void setCategoriaConta(float saldo) {
		
		if(saldo <= 1000f) {
			this.categoriaConta = CategoriaConta.COMUM;
		}
		
		if(saldo > 1000f && saldo <= 5000f) {
			this.categoriaConta = CategoriaConta.SUPER;
		}
		
		if(saldo > 5000f) {
			this.categoriaConta = CategoriaConta.PREMIUM;	
		}	
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
}
