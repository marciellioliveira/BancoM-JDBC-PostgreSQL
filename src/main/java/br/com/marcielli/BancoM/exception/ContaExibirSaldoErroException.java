package br.com.marcielli.BancoM.exception;

public class ContaExibirSaldoErroException  extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContaExibirSaldoErroException() { super("Não foi possível exibir o saldo da conta no momento. Revise seus dados"); }
	
	public ContaExibirSaldoErroException(String message) { super(message); }
}
