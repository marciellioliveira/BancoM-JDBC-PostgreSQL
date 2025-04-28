package br.com.marcielli.bancom.exception;

public class ClienteCpfInvalidoException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClienteCpfInvalidoException() { super("Cpf inválido."); }
	
	public ClienteCpfInvalidoException(String message) { super(message); }

}
