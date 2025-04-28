package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;
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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger log = LoggerFactory.getLogger(Conta.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

//	@Version
//	private Long version;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH })
	@JoinColumn(name = "clienteId")
	@JsonBackReference
	private Cliente cliente;

	@Enumerated(EnumType.STRING)
	private TipoConta tipoConta;

	@Enumerated(EnumType.STRING)
	private CategoriaConta categoriaConta;

	@JsonIgnore
	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumn(name = "taxasId")
	private List<TaxaManutencao> taxas;

	@Column(nullable = false)
	private BigDecimal saldoConta;

	@JsonIgnore
	@Transient
	private BigDecimal valorTransferencia;

	private String numeroConta;

	@Column(name = "chave_pix")
	private String pixAleatorio;

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumn(name = "transferenciaId")
	private List<Transferencia> transferencia;

	@OneToMany(mappedBy = "conta", cascade = { CascadeType.ALL })
	@JsonManagedReference
	private List<Cartao> cartoes;

	private Boolean status = true;

	public void pagarFatura(BigDecimal valor) {
		if (saldoConta == null) {
			this.saldoConta = BigDecimal.ZERO;
		}

		this.saldoConta = this.saldoConta.subtract(valor);
	}

	public void creditar(BigDecimal valor) {
        if (valor == null) {
            log.warn("Tentativa de crédito com valor nulo");
            return;
        }
        
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Tentativa de crédito com valor não positivo: {}", valor);
            return;
        }
        
        if (this.saldoConta == null) {
            this.saldoConta = BigDecimal.ZERO;
        }
        
        this.saldoConta = this.saldoConta.add(valor);
        log.info("Crédito de {} realizado. Novo saldo: {}", valor, this.saldoConta);
    }
	
	public void debitar(BigDecimal valor) {
        if (valor == null) {
            log.warn("Tentativa de débito com valor nulo");
            return;
        }
        
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Tentativa de débito com valor não positivo: {}", valor);
            return;
        }
        
        if (this.saldoConta == null) {
            this.saldoConta = BigDecimal.ZERO;
        }
        
        if (this.saldoConta.compareTo(valor) < 0) {
            log.warn("Saldo insuficiente para débito. Saldo: {}, Tentativa: {}", this.saldoConta, valor);
            return;
        }
        
	}

}
