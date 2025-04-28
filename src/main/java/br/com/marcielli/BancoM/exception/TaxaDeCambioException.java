package br.com.marcielli.BancoM.exception;

public class TaxaDeCambioException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TaxaDeCambioException() { super("Erro ao obter a taxa de câmbio."); }
	
	public TaxaDeCambioException(String message) { super(message); }

}
