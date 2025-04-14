package br.com.marcielli.BancoM.entity;

import java.math.BigDecimal;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@PrimaryKeyJoinColumn(name = "idConta")
@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
@ToString
public class ContaPoupanca extends Conta {
	
	private static final long serialVersionUID = 1L;
	
	private BigDecimal taxaAcrescRend;
	private BigDecimal taxaMensal;
	
	public ContaPoupanca(BigDecimal taxaAcrescRend, BigDecimal taxaMensal) {
		super();
		this.taxaAcrescRend = taxaAcrescRend;	
		this.taxaMensal = taxaMensal;		
	}
	
//	public ContaPoupanca() {}	

//	public ContaPoupanca(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta, String numeroConta, List<Taxas> taxas){
//		super(cliente, tipoConta,categoriaConta, saldoConta, numeroConta, taxas);	
//		
//		String pixAleatorio = gerarPixAleatorio();		
//		super.setPixAleatorio(pixAleatorio);
//		super.setStatus(true);
//		
//		for(Taxas taxasPoupanca : taxas) {
//			if(taxasPoupanca.getTipoConta() == TipoConta.POUPANCA) {
//				this.taxaAcrescRend = taxasPoupanca.getTaxaAcrescRend();
//				this.taxaMensal = taxasPoupanca.getTaxaMensal();				
//			}
//		}		
//	}

//	public float getTaxaAcrescRend() {
//		return taxaAcrescRend;
//	}
//	
//	public void setTaxaAcrescRend(float taxaAcrescRend) {
//		this.taxaAcrescRend = taxaAcrescRend;
//	}
//
//	public float getTaxaMensal() {
//		return taxaMensal;
//	}
//
//	public void setTaxaMensal(float taxaMensal) {
//		this.taxaMensal = taxaMensal;
//	}
	
	public String gerarPixAleatorio() {

		int[] sequencia = new int[8];
		Random random = new Random();
		String minhaConta = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			minhaConta += Integer.toString(sequencia[i]);
		}

		return minhaConta;
	}

//	@Override
//	public String toString() {
//		return "ContaPoupanca [taxaAcrescRend=" + taxaAcrescRend + ", taxaMensal=" + taxaMensal + "]";
//	}
}
