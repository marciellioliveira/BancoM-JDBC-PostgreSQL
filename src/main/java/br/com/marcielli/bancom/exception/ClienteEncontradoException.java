package br.com.marcielli.bancom.exception;

public class ClienteEncontradoException extends RuntimeException  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClienteEncontradoException() { super("Cliente já existe no banco."); }
	
	public ClienteEncontradoException(String message) { super(message); }


}
