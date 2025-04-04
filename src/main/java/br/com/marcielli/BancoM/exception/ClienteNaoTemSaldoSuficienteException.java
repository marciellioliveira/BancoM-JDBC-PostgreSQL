package br.com.marcielli.BancoM.exception;

public class ClienteNaoTemSaldoSuficienteException  extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClienteNaoTemSaldoSuficienteException() { super("Cliente não tem saldo suficiente para realizar essa ação."); }
	
	public ClienteNaoTemSaldoSuficienteException(String message) { super(message); }

}
