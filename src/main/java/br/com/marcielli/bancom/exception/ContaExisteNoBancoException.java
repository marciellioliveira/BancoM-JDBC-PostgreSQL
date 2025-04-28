package br.com.marcielli.bancom.exception;

public class ContaExisteNoBancoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ContaExisteNoBancoException() { super("A conta já existe no Banco"); }
	
	public ContaExisteNoBancoException(String message) { super(message); }
}
