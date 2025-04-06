package br.com.marcielli.BancoM.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.ContaNaoFoiPossivelAlterarNumeroException;


public abstract class ContaFactory {
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public static Conta criarConta(Conta novaConta) {
		String numConta = gerarNumeroDaConta(novaConta);
		
		CategoriaConta categoriaConta = null;
		float taxaManutencaoMensalCC = 0;	
		float taxaAcrescRendPP1 = 0;
		float taxaMensalPP2 = 0;			
		Taxas taxasDaContaPP = null;
		Taxas taxasDaContaCC = null;

		if(novaConta.getSaldoConta() <= 1000) {
			
			//Conta Corrente
			taxaManutencaoMensalCC = 12.00f;
			
			//Todas
			categoriaConta = CategoriaConta.COMUM;
			
			//Conta Poupança
			taxaAcrescRendPP1 = 0.005f;	
			taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);
			
			
		}
		
		if(novaConta.getSaldoConta() > 1000 && novaConta.getSaldoConta() <= 5000) {
			
			//Conta Corrente
			taxaManutencaoMensalCC = 8.00f;
			
			//Todas
			categoriaConta = CategoriaConta.SUPER;
			
			//Conta Poupança
			taxaAcrescRendPP1 = 0.007f;
			taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);
		}
		
		if(novaConta.getSaldoConta() > 5000) {
			
			//Conta Corrente
			taxaManutencaoMensalCC = 0f;	
			
			//Todas
			categoriaConta = CategoriaConta.PREMIUM;
			
			//Conta Poupança
			taxaAcrescRendPP1 = 0.009f;	
			taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);				
		}	
		
		
		if (novaConta.getTipoConta() == TipoConta.CORRENTE) {	
			
			String numContaCorrente = numConta.concat("-CC");
			
			taxasDaContaCC = new Taxas(novaConta.getSaldoConta(), TipoConta.CORRENTE);
			List<Taxas> novaTaxaCC = new ArrayList<Taxas>();
			novaTaxaCC.add(taxasDaContaCC);
			
//			ContaCorrente contaCorrente = new ContaCorrente(novaConta.getCliente(), TipoConta.CORRENTE, categoriaConta, novaConta.getSaldoConta(), numContaCorrente,novaTaxaCC);
		
			Conta contaCorrente = new ContaCorrente(novaConta.getCliente(), TipoConta.CORRENTE, categoriaConta, novaConta.getSaldoConta(), numContaCorrente,novaTaxaCC);
			
			return contaCorrente;
			
			
		} else if (novaConta.getTipoConta() == TipoConta.POUPANCA) {
			
			String numContaPoupanca = numConta.concat("-PP");
			
			taxasDaContaPP = new Taxas(novaConta.getSaldoConta(), TipoConta.POUPANCA);
			List<Taxas> novaTaxaPP = new ArrayList<Taxas>();
			novaTaxaPP.add(taxasDaContaPP);
			
//			ContaPoupanca contaPoupanca = new ContaPoupanca(novaConta.getCliente(), TipoConta.POUPANCA, categoriaConta, novaConta.getSaldoConta(), numContaPoupanca,novaTaxaPP);
			Conta contaPoupanca = new ContaPoupanca(novaConta.getCliente(), TipoConta.POUPANCA, categoriaConta, novaConta.getSaldoConta(), numContaPoupanca,novaTaxaPP);
								
			return contaPoupanca;
			
	
		} else {
			return null;
		}
		
	}
	
	

//	public static Conta atualizarConta(Conta contaInserir, Conta contaAtualizar, List<Conta> todasAsContasH2) {
//
//		
//	
//		
//			return contaAtualizar;	
//		
//	}
//	
	private static String gerarNumeroDaConta(Conta conta) {

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
	
	private static String atualizarNumeroDaConta(String numeroConta) {

		String doisUltimosDigitos = null;
		String novoNumConta = null;

		if (numeroConta.length() > 2) {

			doisUltimosDigitos = numeroConta.substring(numeroConta.length() - 2);

			if (numeroConta.equalsIgnoreCase("CC")) {

				novoNumConta = numeroConta.replaceAll("CC", "PP");

			} else if (numeroConta.equalsIgnoreCase("PP")) {

				novoNumConta = numeroConta.replaceAll("PP", "CC");

			} else {
				throw new ContaNaoFoiPossivelAlterarNumeroException(
						"Não foi possível alterar o número da conta no momento.");
			}

		} else {
			doisUltimosDigitos = null;
		}

		return novoNumConta;
	}		

}

