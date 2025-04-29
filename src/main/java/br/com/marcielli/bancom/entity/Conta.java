package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Conta implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(Conta.class);

	private Long id;

	@JsonBackReference
	private Cliente cliente;

	private TipoConta tipoConta;

	private CategoriaConta categoriaConta;

	@JsonIgnore
	private List<TaxaManutencao> taxas;

	private BigDecimal saldoConta;

	@JsonIgnore
	@Transient
	private BigDecimal valorTransferencia;

	private String numeroConta;

	private String pixAleatorio;

	private List<Transferencia> transferencia;

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
