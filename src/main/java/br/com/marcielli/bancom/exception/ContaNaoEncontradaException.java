package br.com.marcielli.bancom.exception;

public class ContaNaoEncontradaException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContaNaoEncontradaException() { super("Conta não encontrada."); }
	
	public ContaNaoEncontradaException(String message) { super(message); }

}
