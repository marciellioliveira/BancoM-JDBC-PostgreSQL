package br.com.marcielli.BancoM.exception;

public class ClienteCpfInvalidoException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClienteCpfInvalidoException() { super("Cpf inválido."); }
	
	public ClienteCpfInvalidoException(String message) { super(message); }

}
