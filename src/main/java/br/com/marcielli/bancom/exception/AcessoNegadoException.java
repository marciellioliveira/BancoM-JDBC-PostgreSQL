package br.com.marcielli.bancom.exception;

public class AcessoNegadoException extends RuntimeException  {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AcessoNegadoException() { super("Acesso negado."); }
	
	public AcessoNegadoException(String message) { super(message); }

}
