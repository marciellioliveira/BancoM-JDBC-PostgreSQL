package br.com.marcielli.bancom.utils;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class GerarNumeros {
	
	Random random = new Random();
	
	public String gerarNumeroGeral() {

		int[] sequencia = new int[8];
		String meucartao = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			meucartao += Integer.toString(sequencia[i]);
		}

		return meucartao;
	}
	
//	public String gerarNumCartao() {
//
//		int[] sequencia = new int[8];
//		String meucartao = "";
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//		}
//
//		for (int i = 0; i < sequencia.length; i++) {
//			meucartao += Integer.toString(sequencia[i]);
//		}
//
//		return meucartao;
//	}
//	
//	//Conta
//	public String gerarNumeroDaConta() {
//		int[] sequencia = new int[8];
//		StringBuilder minhaConta = new StringBuilder();
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//			minhaConta.append(sequencia[i]);
//		}
//
//		return minhaConta.toString();
//	}
//
//	//Conta
//	public String gerarPixAleatorio() {
//		int[] sequencia = new int[8];
//		StringBuilder meuPix = new StringBuilder();
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//			meuPix.append(sequencia[i]);
//		}
//
//		return meuPix.toString();
//	}
//
//	//Conta
//	public String gerarCodigoTransferencia() {
//		int[] sequencia = new int[8];
//		StringBuilder codTransferencia = new StringBuilder();
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//			codTransferencia.append(sequencia[i]);
//		}
//
//		return codTransferencia.toString();
//	}



}
