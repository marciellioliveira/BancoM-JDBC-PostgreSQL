package br.com.marcielli.BancoM.entity;

import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;

public class ValidacaoUsuarioAtivo {
	
	public class ValidacaoUsuarioUtil {

	    public static void verificarUsuarioAtivo(User user) {
	        if (user == null || !user.isUserAtivo()) {
	            throw new ClienteNaoEncontradoException("Usuário inativo ou inexistente.");
	        }
	    }
	}


}
