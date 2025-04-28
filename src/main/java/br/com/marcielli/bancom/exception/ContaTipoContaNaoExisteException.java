package br.com.marcielli.bancom.exception;

public class ContaTipoContaNaoExisteException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContaTipoContaNaoExisteException() { super("Cpf inválido."); }
	
	public ContaTipoContaNaoExisteException(String message) { super(message); }


}
