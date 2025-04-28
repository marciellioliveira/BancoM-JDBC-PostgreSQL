package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
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
import jakarta.persistence.OneToOne;
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
	
//	@Version
//	private Long version;
	
	@Enumerated(EnumType.STRING)	
	@JsonIgnore
	private TipoConta tipoConta;
	
	@Enumerated(EnumType.STRING)
	@JsonIgnore
	private CategoriaConta categoriaConta;
	
	@Enumerated(EnumType.STRING)
	private TipoCartao tipoCartao;
	
	private String numeroCartao;
	
	private boolean status;
	
	public String senha;
	
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "contaId")
	@JsonBackReference
	private Conta conta;
	
	@OneToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "faturaId", referencedColumnName = "id")
	@JsonManagedReference
	private Fatura fatura;
	
	@OneToMany(mappedBy = "cartao", cascade = {CascadeType.ALL})
	@JsonManagedReference
	private List<Seguro> seguros;
	
	
}
