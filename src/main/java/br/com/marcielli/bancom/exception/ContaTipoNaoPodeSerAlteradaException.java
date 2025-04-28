package br.com.marcielli.bancom.exception;

public class ContaTipoNaoPodeSerAlteradaException  extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContaTipoNaoPodeSerAlteradaException() { super("Cpf inválido."); }
	
	public ContaTipoNaoPodeSerAlteradaException(String message) { super(message); }

}
