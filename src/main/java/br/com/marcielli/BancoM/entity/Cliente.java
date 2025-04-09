package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;

@Entity
public class Cliente implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
	private String nome;
	
	private Long cpf;
	
	@OneToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "enderecoId")
	@JsonManagedReference
	private Endereco endereco;
	
	@OneToMany(mappedBy = "cliente", cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
	@JsonManagedReference	
	private List<Conta> contas;
	
	
	
	public Cliente(String nome, Long cpf, Endereco endereco, List<Conta> contas) {
		super();
		this.nome = nome;
		this.cpf = cpf;
		this.endereco = endereco;
		this.contas = contas;
	}

	public Cliente() {}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Long getCpf() {
		return cpf;
	}

	public void setCpf(Long cpf) {
		this.cpf = cpf;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public List<Conta> getContas() {
		return contas;
	}

	public void setContas(List<Conta> contas) {
		this.contas = contas;
	}

	@Override
	public String toString() {
		return "Cliente [id=" + id + ", version=" + version + ", nome=" + nome + ", cpf=" + cpf + ", endereco="
				+ endereco + ", contas=" + contas + "]";
	}

	
	

}
