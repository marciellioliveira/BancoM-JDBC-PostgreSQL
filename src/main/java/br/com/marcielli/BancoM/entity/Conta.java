package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Transient;
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
public class Conta implements Serializable { 
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "clienteId")
	@JsonBackReference
	private Cliente cliente;
	
	@Enumerated(EnumType.STRING)
	private TipoConta tipoConta;
	
	@Enumerated(EnumType.STRING)
	private CategoriaConta categoriaConta;
	
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "taxasId")
	private List<TaxaManutencao> taxas;
	
	private BigDecimal saldoConta;	
	
	@JsonIgnore
	@Transient
	private BigDecimal valorTransferencia;
	
	private String numeroConta;	
	
	@Column(name = "chave_pix")
	private String pixAleatorio;
	
	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "transferenciaId")
	private List<Transferencia> transferencia;	
	
	@OneToMany(mappedBy = "conta", cascade = {CascadeType.ALL})
	@JsonManagedReference
	private List<Cartao> cartoes;
	
	private boolean status;
	
	
	public void pagarFatura(BigDecimal valor) {
		if(saldoConta == null) {
			this.saldoConta = BigDecimal.ZERO;
		}
		
		this.saldoConta = this.saldoConta.subtract(valor);
	}
	

	
	
}


