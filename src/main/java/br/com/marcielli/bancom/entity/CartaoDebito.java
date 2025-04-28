package br.com.marcielli.bancom.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@PrimaryKeyJoinColumn(name = "idCartao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartaoDebito extends Cartao {	
	
	private static final long serialVersionUID = 1L;
	
	private BigDecimal limiteDiarioTransacao = new BigDecimal("600");
	private BigDecimal totalGastoMes = BigDecimal.ZERO;
	
	public void atualizarTotalGastoMes(BigDecimal valor) {
		if (this.totalGastoMes == null) {
	        this.totalGastoMes = BigDecimal.ZERO; 
	    }
		this.totalGastoMes = this.totalGastoMes.add(valor);
		
	}

	public void atualizarLimiteDiarioTransacao(BigDecimal valor) {	
		if (this.limiteDiarioTransacao == null) {
	        this.limiteDiarioTransacao = BigDecimal.ZERO; 
	    }
		
		this.limiteDiarioTransacao = this.limiteDiarioTransacao.subtract(valor);		
	}
	
	public void alterarLimiteDiarioTransacao(BigDecimal valor) {
		if (this.limiteDiarioTransacao == null) {
	        this.limiteDiarioTransacao = BigDecimal.ZERO; 
	    }
		
		this.limiteDiarioTransacao = valor;
	}
}
