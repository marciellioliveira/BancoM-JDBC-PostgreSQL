package br.com.marcielli.bancom.exception;

public class ChavePixNaoEncontradaException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChavePixNaoEncontradaException() { super("Chave PIX não encontrada."); }
	
	public ChavePixNaoEncontradaException(String message) { super(message); }

}
