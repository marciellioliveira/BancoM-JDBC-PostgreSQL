package br.com.marcielli.BancoM.entity;

import java.math.BigDecimal;
import java.math.MathContext;

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
public class TaxaManutencao {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	private Long version;
	
	private TipoConta tipoConta;
	
	private CategoriaConta categoria; 
	
	//Taxas Conta Corrente
	private BigDecimal taxaManutencaoMensal;
	
	//Taxas Conta Poupanca
	private BigDecimal taxaAcrescRend;
	private BigDecimal taxaMensal;
	
	public TaxaManutencao(BigDecimal saldoConta, TipoConta tipoConta) {

		if (tipoConta == TipoConta.CORRENTE) {

			this.tipoConta = TipoConta.CORRENTE;

			if (saldoConta.compareTo(new BigDecimal("1000")) <= 0) { 

				this.categoria = CategoriaConta.COMUM;
				this.taxaManutencaoMensal = new BigDecimal("12.00");

			}

			if (saldoConta.compareTo(new BigDecimal("1000")) > 0 && saldoConta.compareTo(new BigDecimal("5000")) <= 0) {

				this.categoria = CategoriaConta.SUPER;
				this.taxaManutencaoMensal = new BigDecimal("8.00");

			}

			if (saldoConta.compareTo(new BigDecimal("5000")) > 0) {

				this.categoria = CategoriaConta.PREMIUM;
				this.taxaManutencaoMensal = new BigDecimal("0");

			}
		}

		if (tipoConta == TipoConta.POUPANCA) {
			
			MathContext mc = new MathContext(20);

			this.tipoConta = TipoConta.POUPANCA;

			if (saldoConta.compareTo(new BigDecimal("1000")) <= 0) {

				this.categoria = CategoriaConta.COMUM;
				this.taxaAcrescRend = new BigDecimal("0.005");
				
				 // (1 + taxaAcrescRend)
				BigDecimal base = BigDecimal.ONE.add(taxaAcrescRend);
				
				 // (1 + taxaAcrescRend) ^ (1/12)
				BigDecimal exponente = BigDecimal.ONE.divide(new BigDecimal("12"), mc);
				
				this.taxaMensal = BigDecimalMath.pow(base, exponente, mc).subtract(BigDecimal.ONE, mc);

			}

			if (saldoConta.compareTo(new BigDecimal("1000")) > 0 && saldoConta.compareTo(new BigDecimal("5000")) <= 0) {

				this.categoria = CategoriaConta.SUPER;
				this.taxaAcrescRend = new BigDecimal("0.007");
				
				 // (1 + taxaAcrescRend)
				BigDecimal base = BigDecimal.ONE.add(taxaAcrescRend);
				
				 // (1 + taxaAcrescRend) ^ (1/12)
				BigDecimal exponente = BigDecimal.ONE.divide(new BigDecimal("12"), mc);
				
				this.taxaMensal = BigDecimalMath.pow(base, exponente, mc).subtract(BigDecimal.ONE, mc);

			}

			if (saldoConta.compareTo(new BigDecimal("5000")) > 0) {

				this.categoria = CategoriaConta.PREMIUM;
				this.taxaAcrescRend = new BigDecimal("0.009");

				 // (1 + taxaAcrescRend)
				BigDecimal base = BigDecimal.ONE.add(taxaAcrescRend);
				
				 // (1 + taxaAcrescRend) ^ (1/12)
				BigDecimal exponente = BigDecimal.ONE.divide(new BigDecimal("12"), mc);
				
				this.taxaMensal = BigDecimalMath.pow(base, exponente, mc).subtract(BigDecimal.ONE, mc);

			}

		}

	}

}
