package br.com.marcielli.bancom.exception;

public class FaturaNaoTemPermissaoException  extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FaturaNaoTemPermissaoException() { super("Não tem permissão para acessar a fatura."); }
	
	public FaturaNaoTemPermissaoException(String message) { super(message); }
}
