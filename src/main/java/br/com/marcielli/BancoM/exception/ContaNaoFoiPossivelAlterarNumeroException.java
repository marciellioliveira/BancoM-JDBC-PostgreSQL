package br.com.marcielli.BancoM.exception;

public class ContaNaoFoiPossivelAlterarNumeroException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContaNaoFoiPossivelAlterarNumeroException() { super("Não foi possível alterar o número da conta."); }
	
	public ContaNaoFoiPossivelAlterarNumeroException(String message) { super(message); }

}
