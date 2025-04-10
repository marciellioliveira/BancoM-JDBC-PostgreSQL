package br.com.marcielli.BancoM.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoCartao;
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
public class Cartao implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
	@Enumerated(EnumType.STRING)	
	@JsonIgnore
	private TipoConta tipoConta;
	
	@Enumerated(EnumType.STRING)
	@JsonIgnore
	private CategoriaConta categoriaConta;
	
	@Enumerated(EnumType.STRING)
	private TipoCartao tipoCartao;
	
	
	private String numeroCartao;
	
	@JsonIgnore
	private boolean status;
	
	public String senha;
	
	@ManyToOne
	@JoinColumn(name = "contaId")
	@JsonBackReference
	private Conta conta;
	
	public Cartao() {}

	public Cartao(String numeroCartao, TipoConta tipoConta, CategoriaConta categoriaConta, TipoCartao tipoCartao,
			boolean status, String senha, Conta conta) {
		super();
		this.numeroCartao = numeroCartao;
		this.tipoConta = tipoConta;
		this.categoriaConta = categoriaConta;
		this.tipoCartao = tipoCartao;
		this.status = status;
		this.senha = senha;
		this.conta = conta;
	}	
	
	public Cartao(String numeroCartao, TipoCartao tipoCartao, String senha, Conta conta) {
		super();
		this.numeroCartao = numeroCartao;
		this.tipoCartao = tipoCartao;
		this.senha = senha;
		this.conta = conta;
	
	}	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public String getNumeroCartao() {
		return numeroCartao;
	}

	public void setNumeroCartao(String numeroCartao) {
		this.numeroCartao = numeroCartao;
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

	public TipoCartao getTipoCartao() {
		return tipoCartao;
	}

	public void setTipoCartao(TipoCartao tipoCartao) {
		this.tipoCartao = tipoCartao;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public Conta getConta() {
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}

	@Override
	public String toString() {
		return "Cartao [numeroCartao=" + numeroCartao + ", tipoConta=" + tipoConta + ", categoriaConta="
				+ categoriaConta + ", tipoCartao=" + tipoCartao + ", status=" + status + ", senha=" + senha + ", conta="
				+ conta + "]";
	}	
	
	

}
