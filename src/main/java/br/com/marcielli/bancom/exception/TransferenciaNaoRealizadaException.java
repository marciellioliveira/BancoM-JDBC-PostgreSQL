package br.com.marcielli.bancom.exception;

public class TransferenciaNaoRealizadaException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransferenciaNaoRealizadaException() { super("A transferência não foi realizada."); }
	
	public TransferenciaNaoRealizadaException(String message) { super(message); }

}
