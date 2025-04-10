package br.com.marcielli.BancoM.entity;

import java.security.MessageDigest;
import java.util.Random;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;

public abstract class CartaoFactory {
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public static Cartao criarCartao(Cartao dadosCartao, Conta contaDoCartao) {
		
		String numCartao = gerarNumeroDoCartao();		
		
		if (dadosCartao.getTipoCartao() == TipoCartao.CREDITO) {
			
			String numCartaoCredito = numCartao.concat("-CC");		
			
			return new CartaoCredito(numCartaoCredito, contaDoCartao.getTipoConta(), contaDoCartao.getCategoriaConta(), TipoCartao.CREDITO, true, dadosCartao.getSenha(), contaDoCartao);
		
		} else if (dadosCartao.getTipoCartao() == TipoCartao.DEBITO) {
			
			String numCartaoDebito = numCartao.concat("-CD");
			return new CartaoDebito(numCartaoDebito, contaDoCartao.getTipoConta(), contaDoCartao.getCategoriaConta(), TipoCartao.DEBITO, true, dadosCartao.getSenha(), contaDoCartao);
		}
		
		return null;

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
