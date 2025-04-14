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
public class CartaoCredito extends Cartao {

	private static final long serialVersionUID = 1L;

	private float limiteCreditoPreAprovado;
	
	private float taxaUtilizacao;

	private float taxaSeguroViagem;

	private float totalGastoMesCredito;
	
	//public CartaoCredito() {}	
	
	
//	public CartaoCredito(String numeroCartao, TipoConta tipoConta, CategoriaConta categoriaConta, TipoCartao tipoCartao,
//			boolean status, String senha, Conta conta) {
//		super(numeroCartao, tipoConta, categoriaConta, tipoCartao, status, senha, conta);	
//	
//		if(categoriaConta.equals(CategoriaConta.COMUM)) {
//			this.limiteCreditoPreAprovado = 1000f;		
//		}
//		
//		if(categoriaConta.equals(CategoriaConta.SUPER)) {
//			this.limiteCreditoPreAprovado = 5000f;
//		}
//		
//		if(categoriaConta.equals(CategoriaConta.PREMIUM)) {
//			this.limiteCreditoPreAprovado = 10000f;		
//		}		
//	}
	
//	public CartaoCredito(String numeroCartao, TipoCartao tipoCartao, String senha, Conta conta) {
//		super(numeroCartao,tipoCartao,senha, conta);	
//	
//		if(conta.getCategoriaConta().equals(CategoriaConta.COMUM)) {
//			this.limiteCreditoPreAprovado = 1000f;		
//		}
//		
//		if(conta.getCategoriaConta().equals(CategoriaConta.SUPER)) {
//			this.limiteCreditoPreAprovado = 5000f;
//		}
//		
//		if(conta.getCategoriaConta().equals(CategoriaConta.PREMIUM)) {
//			this.limiteCreditoPreAprovado = 10000f;		
//		}		
//	}
//	
	

}
