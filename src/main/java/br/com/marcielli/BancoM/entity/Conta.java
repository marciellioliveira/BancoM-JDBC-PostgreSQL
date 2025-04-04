package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
	
	private float saldoConta;
	
	private String numeroConta;
	
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "transferenciaId")
	private List<Transferencia> transferencia;
	
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
	
	public Conta(Cliente cliente, TipoConta tipoConta,float saldoConta, String numeroConta) {
		super();	
		this.cliente = cliente;
		this.tipoConta = tipoConta;	
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


	public void setCategoriaConta(CategoriaConta categoriaConta) {
		this.categoriaConta = categoriaConta;
	}	

	public float getSaldoConta() {
		return saldoConta;
	}
	
	public void setSaldoConta(float saldo, Funcao funcao) {
		
		if(funcao.getDescricao().equalsIgnoreCase("PAGADOR")) {
			
			this.saldoConta = getSaldoConta() - saldo;
			
		} else if(funcao.getDescricao().equalsIgnoreCase("RECEBEDOR")) {
			
			this.saldoConta = getSaldoConta() + saldo;
			
		}
		
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
