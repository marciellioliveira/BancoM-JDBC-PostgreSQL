package br.com.marcielli.BancoM.entity;

import java.text.DecimalFormat;
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
		
		//TaxaManutencao taxa = new TaxaManutencao(novaConta.getSaldoConta(), novaConta.getTipoConta());
		//List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		//novaTaxa.add(taxa);
		//novaConta.setTaxas(novaTaxa);		
		
		if (novaConta.getTipoConta() == TipoConta.CORRENTE) {	
			
			String numContaCorrente = numConta.concat("-CC");
			
//			taxasDaContaCC = new Taxas(novaConta.getSaldoConta(), TipoConta.CORRENTE);
//			List<Taxas> novaTaxaCC = new ArrayList<Taxas>();
//			novaTaxaCC.add(taxasDaContaCC);
//			
		//	novaConta.setNumeroConta(numContaCorrente);
			
			TaxaManutencao taxa = new TaxaManutencao(novaConta.getSaldoConta(), novaConta.getTipoConta());
			List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
			novaTaxa.add(taxa);
			
			Conta contaCorrente = new ContaCorrente(taxa.getTaxaManutencaoMensal());
			
			contaCorrente.setTaxas(novaTaxa);	
			contaCorrente.setNumeroConta(numContaCorrente);
			
					
			//Conta contaCorrente = new ContaCorrente(novaConta.getCliente(), TipoConta.CORRENTE, categoriaConta, novaConta.getSaldoConta(), numContaCorrente,novaTaxaCC);
			
			return contaCorrente;
			
			
		} else if (novaConta.getTipoConta() == TipoConta.POUPANCA) {
			
			String numContaPoupanca = numConta.concat("-PP");
			
			novaConta.setNumeroConta(numContaPoupanca);
			
//			taxasDaContaPP = new Taxas(novaConta.getSaldoConta(), TipoConta.POUPANCA);
//			List<Taxas> novaTaxaPP = new ArrayList<Taxas>();
//			novaTaxaPP.add(taxasDaContaPP);
			
		//	Conta contaPoupanca = new ContaPoupanca(novaConta.getCliente(), TipoConta.POUPANCA, categoriaConta, novaConta.getSaldoConta(), numContaPoupanca,novaTaxaPP);
								
			return new ContaPoupanca();
	
		} else {
			return null;
		}
		
	}
	
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

