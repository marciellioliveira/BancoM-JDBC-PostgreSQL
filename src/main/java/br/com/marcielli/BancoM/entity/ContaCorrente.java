package br.com.marcielli.BancoM.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;


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
public class ContaCorrente extends Conta {
	
	private static final long serialVersionUID = 1L;	
	
	private BigDecimal taxaManutencaoMensal;
	
	public ContaCorrente(BigDecimal taxaManutencaoMensal) {
		super();
		this.taxaManutencaoMensal = taxaManutencaoMensal;		
	}
	
//	public ContaCorrente() {}	
	
	//Testando esse
//	public ContaCorrente(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta, String numeroConta, List<Taxas> taxas) {
//		super(cliente, tipoConta, categoriaConta, saldoConta, numeroConta, taxas);
//		
//		String pixAleatorio = gerarPixAleatorio();		
//		super.setPixAleatorio(pixAleatorio);
//		super.setStatus(true);
//		
//		for(Taxas taxasCorrente : taxas) {
//			if(taxasCorrente.getTipoConta() == TipoConta.CORRENTE) {
//				this.taxaManutencaoMensal = taxasCorrente.getTaxaManutencaoMensal();
//			} 
//		}
//		
//	}
	
//	public float getTaxaManutencaoMensal() {
//		return taxaManutencaoMensal;
//	}
//
//	public void setTaxaManutencaoMensal(float taxaManutencaoMensal) {
//		this.taxaManutencaoMensal = taxaManutencaoMensal;
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
//		return "ContaCorrente [taxaManutencaoMensal=" + taxaManutencaoMensal + "]";
//	}
}
