package br.com.marcielli.bancom.exception;

public class ClienteNomeInvalidoException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClienteNomeInvalidoException() { super("Nome inválido."); }
	
	public ClienteNomeInvalidoException(String message) { super(message); }
}
