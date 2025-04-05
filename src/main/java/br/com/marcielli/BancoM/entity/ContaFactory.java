package br.com.marcielli.BancoM.entity;

import java.util.Random;

import br.com.marcielli.BancoM.enuns.TipoConta;


public abstract class ContaFactory {
	
	public static Conta criarConta(Conta novaConta) {
		String numConta = gerarNumeroDaConta(novaConta);
		
		if (novaConta.getTipoConta() == TipoConta.CORRENTE) {	
			
			String numContaCorrente = numConta.concat("-CC");
			
			ContaCorrente contaCorrente = new ContaCorrente(novaConta.getCliente(), TipoConta.CORRENTE, novaConta.getSaldoConta(), numContaCorrente);	
			
			return contaCorrente;
			
			
		} else if (novaConta.getTipoConta() == TipoConta.POUPANCA) {
			
			String numContaPoupanca = numConta.concat("-PP");
			ContaPoupanca contaPoupanca = new ContaPoupanca(novaConta.getCliente(), TipoConta.POUPANCA, novaConta.getSaldoConta(), numContaPoupanca);

			return contaPoupanca;
			
	
		} else {
			return null;
		}
		
	}
	
	public static String gerarNumeroDaConta(Conta conta) {

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

}

