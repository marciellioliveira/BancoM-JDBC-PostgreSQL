package br.com.marcielli.BancoM.exception;

public class PermissaoNegadaException  extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public PermissaoNegadaException() { super("Você não tem permissão para atualizar este cartão"); }
	
	public PermissaoNegadaException(String message) { super(message); }


}
