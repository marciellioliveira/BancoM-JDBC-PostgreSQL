package br.com.marcielli.bancom.exception;

public class FaturaNaoEncontradaException  extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public FaturaNaoEncontradaException() { super("Fatura não encontrada"); }
	
	public FaturaNaoEncontradaException(String message) { super(message); }

}
