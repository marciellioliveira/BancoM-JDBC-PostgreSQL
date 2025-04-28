package br.com.marcielli.bancom.exception;

public class RolePersistenceException  extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RolePersistenceException() { super("Usuário não encontrado."); }

    public RolePersistenceException(String message) { super(message); }

}
