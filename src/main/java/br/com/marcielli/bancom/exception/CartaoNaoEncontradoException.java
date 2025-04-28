package br.com.marcielli.bancom.exception;

public class CartaoNaoEncontradoException  extends RuntimeException  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CartaoNaoEncontradoException() { super("Cartão não encontrado."); }
	
	public CartaoNaoEncontradoException(String message) { super(message); }

}
