package br.com.marcielli.BancoM.exception;

public class ContaNaoRealizouTransferenciaException  extends RuntimeException {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContaNaoRealizouTransferenciaException() { super("A transferência não foi realizada."); }
	
	public ContaNaoRealizouTransferenciaException(String message) { super(message); }

}
