package br.com.marcielli.BancoM.entity;

import java.util.List;

public interface TransferenciaContrato {
		
	public String gerarCodigoTransferencia();
	
	public List<Conta> transferirTed(Conta enviar, float valor, Conta receber);
	
	public List<Conta> transferirPix(Conta enviar, float valor, Conta receber);
	
	public List<Conta> depositar(float valorTransferencia, Conta receber);
	
	public List<Conta> sacar(float valorTransferencia, Conta receber);
	
	public float exibirSaldo(Conta conta);
	
}
