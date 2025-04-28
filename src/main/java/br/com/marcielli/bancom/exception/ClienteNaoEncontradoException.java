package br.com.marcielli.bancom.exception;

public class ClienteNaoEncontradoException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClienteNaoEncontradoException() { super("Cliente não encontrado."); }
	
	public ClienteNaoEncontradoException(String message) { super(message); }

}
