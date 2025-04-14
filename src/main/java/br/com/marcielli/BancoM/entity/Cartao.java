package br.com.marcielli.BancoM.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;
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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Cartao implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
//	@JsonIgnore
//	@Transient
//	private Long idCliente;	
//	
//	@JsonIgnore
//	@Transient
//	private Long idConta;	
	
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
}
