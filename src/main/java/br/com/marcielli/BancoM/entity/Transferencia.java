package br.com.marcielli.BancoM.entity;

import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;

@Entity
public class Transferencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	private Long version;

	private Conta contaDestino;
	
	private Conta contaOrigem;

	private float valor;

	private Date data;

	private String codigoOperacao;
	
	@ManyToOne
	private Conta conta;

	public Transferencia(Long id, Long version, Conta contaDestino, Conta contaOrigem, float valor, Date data,
			String codigoOperacao, Conta conta) {
		super();
		this.id = id;
		this.version = version;
		this.contaDestino = contaDestino;
		this.contaOrigem = contaOrigem;
		this.valor = valor;
		this.data = data;
		this.codigoOperacao = codigoOperacao;
		this.conta = conta;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Conta getContaDestino() {
		return contaDestino;
	}

	public void setContaDestino(Conta contaDestino) {
		this.contaDestino = contaDestino;
	}	

	public Conta getContaOrigem() {
		return contaOrigem;
	}

	public void setContaOrigem(Conta contaOrigem) {
		this.contaOrigem = contaOrigem;
	}

	public float getValor() {
		return valor;
	}

	public void setValor(float valor) {
		this.valor = valor;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public String getCodigoOperacao() {
		return codigoOperacao;
	}

	public void setCodigoOperacao(String codigoOperacao) {
		this.codigoOperacao = codigoOperacao;
	}

	public Conta getConta() {
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}
	
	


}
