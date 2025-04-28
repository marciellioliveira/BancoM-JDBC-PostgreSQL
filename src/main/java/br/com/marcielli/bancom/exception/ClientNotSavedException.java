package br.com.marcielli.bancom.exception;

public class ClientNotSavedException  extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClientNotSavedException() { super("Cliente não foi cadastrado"); }
	
	public ClientNotSavedException(String message) { super(message); }

}
