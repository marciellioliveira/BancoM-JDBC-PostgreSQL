package br.com.marcielli.BancoM.entity;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import br.com.marcielli.BancoM.exception.ClienteEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.repository.UserRepository;

public class ValidacaoUsuarioAtivo {

	private final UserRepository userRepository;

	public ValidacaoUsuarioAtivo(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public static void verificarUsuarioAtivo(User user) {
		if (user == null || !user.isUserAtivo()) {
			throw new ClienteNaoEncontradoException("Usuário inativo ou inexistente.");
		}
	}

	public static User validarUsuarioAdmin(UserRepository userRepository, JwtAuthenticationToken token) {
		if (token == null) {
			return null;
		}

		Integer userId = Integer.parseInt(token.getName());
		User currentUser = userRepository.findById(userId)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado"));

		verificarUsuarioAtivo(currentUser);
		return currentUser;
	}

	public static boolean isAdmin(User user) {
		if (user == null)
			return false;

		return user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
	}

	public static void validarPermissoesCriacao(User currentUser, boolean isAdminFromController, String username) {
		if (currentUser == null)
			return; // Criação sem autenticação (registro inicial)

		boolean isActuallyAdmin = isAdmin(currentUser);

		// Confirmando se no controller disse que é admin mas não é
		if (isAdminFromController && !isActuallyAdmin) {
			throw new ClienteEncontradoException("Tentativa de fraude detectada");
		}

		// Se não é admin e está tentando criar contas para outros usuários
		if (!isActuallyAdmin && !username.equals(currentUser.getUsername())) {
			throw new ClienteEncontradoException("Apenas administradores podem criar contas para outros usuários");
		}
	}
}