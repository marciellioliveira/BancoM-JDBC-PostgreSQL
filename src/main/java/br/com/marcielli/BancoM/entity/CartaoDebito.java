package br.com.marcielli.BancoM.entity;

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
		
		this.totalGastoMes = totalGastoMes.add(valor);
		
	}

	public void atualizarLimiteDiarioTransacao(BigDecimal valor) {
		
		this.limiteDiarioTransacao = limiteDiarioTransacao.subtract(valor);
		
	}
}
