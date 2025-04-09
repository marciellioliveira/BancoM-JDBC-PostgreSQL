package br.com.marcielli.BancoM.entity;

import java.security.MessageDigest;
import java.util.Random;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoCartao;

public abstract class CartaoFactory {
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public static Cartao criarCartao(Conta contaDoCartao, Cartao novoCartao) {
		
		String numCartao = gerarNumeroDoCartao();
		
		CategoriaConta categoriaConta = null;

		if(contaDoCartao.getSaldoConta() <= 1000) {
			categoriaConta = CategoriaConta.COMUM;
		}
		
		if(contaDoCartao.getSaldoConta() > 1000 && contaDoCartao.getSaldoConta() <= 5000) {
			categoriaConta = CategoriaConta.SUPER;
		}
		
		if(contaDoCartao.getSaldoConta() > 5000) {
			categoriaConta = CategoriaConta.PREMIUM;			
		}			
		
		if (novoCartao.getTipoCartao() == TipoCartao.CREDITO) {	
			
			String numCartaoCredito = numCartao.concat("-CC");									
			return new CartaoCredito(numCartaoCredito, contaDoCartao.getTipoConta(), categoriaConta, TipoCartao.CREDITO, true, novoCartao.getSenha(), contaDoCartao);
			
			
		} else if (novoCartao.getTipoCartao() == TipoCartao.DEBITO) {
			
			String numCartaoDebito = numCartao.concat("-CD");
			return new CartaoDebito(numCartaoDebito, contaDoCartao.getTipoConta(), categoriaConta, TipoCartao.DEBITO, true, novoCartao.getSenha(), contaDoCartao);
	
		} else {
			return null;
		}
		
	}
	
	private static String gerarNumeroDoCartao() {

		int[] sequencia = new int[8];
		Random random = new Random();
		String meuCartao = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			meuCartao += Integer.toString(sequencia[i]);
		}

		return meuCartao;
	}

}
