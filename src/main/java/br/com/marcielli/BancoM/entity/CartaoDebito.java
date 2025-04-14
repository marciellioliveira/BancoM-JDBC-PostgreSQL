package br.com.marcielli.BancoM.entity;

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
	
	private float limiteDiarioTransacao;
	private float totalGastoMes;
	
//	public CartaoDebito() {}
//
//	public CartaoDebito(String numeroCartao, TipoConta tipoConta, CategoriaConta categoriaConta, TipoCartao tipoCartao,
//			boolean status, String senha, Conta conta) {
//		super(numeroCartao, tipoConta, categoriaConta, tipoCartao, status, senha, conta);
//
//		if(categoriaConta.equals(CategoriaConta.COMUM)) {
//			this.limiteDiarioTransacao = 1000f;			
//		}
//		
//		if(categoriaConta.equals(CategoriaConta.SUPER)) {
//			this.limiteDiarioTransacao = 5000f;
//		}
//		
//		if(categoriaConta.equals(CategoriaConta.PREMIUM)) {
//			this.limiteDiarioTransacao = 10000f;	
//		}
//	}
//
//	public float getLimiteDiarioTransacao() {
//		return limiteDiarioTransacao;
//	}
//
//	public void setLimiteDiarioTransacao(float limiteDiarioTransacao) {
//		this.limiteDiarioTransacao = limiteDiarioTransacao;
//	}
//
//	public float getTotalGastoMes() {
//		return totalGastoMes;
//	}
//
//	public void setTotalGastoMes(float totalGastoMes) {
//		this.totalGastoMes = totalGastoMes;
//	}	
}
