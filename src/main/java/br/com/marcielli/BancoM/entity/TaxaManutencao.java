package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;

import com.fasterxml.jackson.annotation.JsonGetter;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import ch.obermuhlner.math.big.BigDecimalMath;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TaxaManutencao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	private Long version;

	private TipoConta tipoConta;
	private CategoriaConta categoria;

	// Taxas Conta Corrente
	private BigDecimal taxaManutencaoMensal;

	// Taxas Conta Poupança
	private BigDecimal taxaAcrescRend; // Taxa anual
	private BigDecimal taxaMensal; // Taxa mensal em decimal (ex: 0.00075)

	// Constantes para os limites
	private static final BigDecimal LIMITE_COMUM = new BigDecimal("1000");
	private static final BigDecimal LIMITE_SUPER = new BigDecimal("5000");

	public TaxaManutencao(BigDecimal saldoConta, TipoConta tipoConta) {

		this.tipoConta = tipoConta;
		MathContext mc = new MathContext(10);

		if (tipoConta == TipoConta.CORRENTE) {
			configurarTaxaContaCorrente(saldoConta);
		} else if (tipoConta == TipoConta.POUPANCA) {
			configurarTaxaContaPoupanca(saldoConta, mc);
		}

//    	
//    	if (saldoConta.compareTo(LIMITE_COMUM) <= 0) {
//    	    this.taxaAcrescRend = new BigDecimal("0.01"); // 1%
//    	} else if (saldoConta.compareTo(LIMITE_SUPER) <= 0) {
//    	    this.taxaAcrescRend = new BigDecimal("0.007"); // 0.7%
//    	} else {
//    	    this.taxaAcrescRend = new BigDecimal("0.009"); // 0.9%
//    	}
//    	
	}

	private void configurarTaxaContaCorrente(BigDecimal saldoConta) {
		if (saldoConta.compareTo(LIMITE_COMUM) <= 0) {
			this.categoria = CategoriaConta.COMUM;
			this.taxaManutencaoMensal = new BigDecimal("12.00");
		} else if (saldoConta.compareTo(LIMITE_SUPER) <= 0) {
			this.categoria = CategoriaConta.SUPER;
			this.taxaManutencaoMensal = new BigDecimal("8.00");
		} else {
			this.categoria = CategoriaConta.PREMIUM;
			this.taxaManutencaoMensal = BigDecimal.ZERO;
		}
	}

	private void configurarTaxaContaPoupanca(BigDecimal saldoConta, MathContext mc) {
		if (saldoConta.compareTo(LIMITE_COMUM) <= 0) {
			this.categoria = CategoriaConta.COMUM;
			this.taxaAcrescRend = new BigDecimal("0.01"); // 1% ao ano
		} else if (saldoConta.compareTo(LIMITE_SUPER) <= 0) {
			this.categoria = CategoriaConta.SUPER;
			this.taxaAcrescRend = new BigDecimal("0.007"); // 0.7% ao ano
		} else {
			this.categoria = CategoriaConta.PREMIUM;
			this.taxaAcrescRend = new BigDecimal("0.009"); // 0.9% ao ano
		}

		this.taxaMensal = calcularTaxaMensal(this.taxaAcrescRend, mc);
	}

	@JsonGetter("taxaMensal")
	public BigDecimal getTaxaMensalPercentual() {
		return taxaMensal.multiply(new BigDecimal("100"));
	}

	private BigDecimal calcularTaxaMensal(BigDecimal taxaAnual, MathContext mc) {
		BigDecimal base = BigDecimal.ONE.add(taxaAnual);
		BigDecimal exponente = BigDecimal.ONE.divide(new BigDecimal("12"), mc);
		return BigDecimalMath.pow(base, exponente, mc).subtract(BigDecimal.ONE);
	}
}