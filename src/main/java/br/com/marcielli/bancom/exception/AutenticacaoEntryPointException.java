package br.com.marcielli.bancom.exception;

public class AutenticacaoEntryPointException extends RuntimeException {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AutenticacaoEntryPointException() { super("Autenticação negada."); }
	
	public AutenticacaoEntryPointException(String message) { super(message); }

}
