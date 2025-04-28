//package br.com.marcielli.BancoM.entity;
//
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//
//public class ValidacaoUsuarioAtivo {
//
//	private static void validarTransferencia(JwtAuthenticationToken token) {
//		
//		
//		
//		
//	}
////		
////		// 1. Obter os clientes das contas
////		Conta contaOrigem = contaRepository.findById(idContaOrigem)
////				.orElseThrow(() -> new ContaNaoEncontradaException("Conta origem não encontrada"));
////
////		Conta contaDestino = contaRepository.findById(idContaDestino)
////				.orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));
////
////		Long idClienteLogado = usuarioLogado.getCliente().getId();
//
//		// 2. Regras para ADMIN
////		if (isAdmin(usuarioLogado)) {
////			// Admin pode transferir entre contas de terceiros
////			// Mas não pode ser o beneficiário (destino)
////			if (contaDestino.getCliente().getId().equals(idClienteLogado)) {
////				throw new ClienteEncontradoException(
////						"Admin não pode realizar transferências da conta de clientes para a própria conta");
////			}
////
////			// Opcional: Validar se a conta origem pertence ao admin
////			if (!contaOrigem.getCliente().getId().equals(idClienteLogado)) {
////				throw new ClienteEncontradoException("Admin só pode transferir da própria conta");
////			}
////		}
////
////		// 3. Regras gerais (aplicam-se a todos os usuários)
////		if (contaOrigem.getCliente().getId().equals(contaDestino.getCliente().getId())) {
////			throw new ClienteEncontradoException("Não é permitido transferência para a mesma conta");
////		}
////
////		if (!contaOrigem.getStatus()) {
////			throw new ClienteEncontradoException("Conta origem está desativada");
////		}
////
////		if (!contaDestino.getStatus()) {
////			throw new ClienteEncontradoException("Conta destino está desativada");
////		}
////	}
//
//}
//
////package br.com.marcielli.BancoM.entity;
////
////import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
////
////import br.com.marcielli.BancoM.enuns.TipoTransferencia;
////import br.com.marcielli.BancoM.exception.ClienteEncontradoException;
////import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
////import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
////import br.com.marcielli.BancoM.repository.CartaoRepository;
////import br.com.marcielli.BancoM.repository.ContaRepository;
////import br.com.marcielli.BancoM.repository.UserRepository;
////
////public class ValidacaoUsuarioAtivo {
////
////	private final UserRepository userRepository;
////	private final ContaRepository contaRepositoy;
////
////	public ValidacaoUsuarioAtivo(UserRepository userRepository, ContaRepository contaRepositoy) {
////		super();
////		this.userRepository = userRepository;
////		this.contaRepositoy = contaRepositoy;
////	}
////
////	public static void verificarUsuarioAtivo(User user) {
////		if (user == null || !user.isUserAtivo()) {
////			throw new ClienteNaoEncontradoException("Usuário inativo ou inexistente.");
////		}
////	}
////
////	public static User validarUsuarioAdmin(UserRepository userRepository, JwtAuthenticationToken token) {
////		if (token == null) {
////			return null;
////		}
////
////		Integer userId = Integer.parseInt(token.getName());
////		User currentUser = userRepository.findById(userId)
////				.orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado"));
////
////		verificarUsuarioAtivo(currentUser);
////		return currentUser;
////	}
////
////	public static void validarPermissoesCriacao(User currentUser, boolean isAdminFromController, String username) {
////		
////		if (!isAdminFromController) {
////	        return; // Permite cadastro sem validação para quem não é admin
////	    }
////		
////		if (currentUser == null || !isAdmin(currentUser)) {
////	        throw new ClienteEncontradoException("Apenas administradores podem criar contas para outros usuários");
////	    }
////		
////	}
////
////	public static boolean isAdmin(User user) {
////		if (user == null)
////			return false;
////
////		return user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
////	}
////
////	// Validando admin
////	public static void validarOperacaoAdmin(User usuarioLogado, Long idContaOrigem, Long idContaDestino,
////			TipoTransferencia tipo, ContaRepository contaRepository) {
////		if (!isAdmin(usuarioLogado))
////			return;
////
////		switch (tipo) {
////		case TED:
////		case PIX:
////			validarTransferencia(usuarioLogado, idContaOrigem, idContaDestino, contaRepository);
////			break;
////		case DEPOSITO:
////			validarDeposito(usuarioLogado, idContaDestino, contaRepository);
////			break;
////		case SAQUE:
////			validarSaque(usuarioLogado, idContaOrigem, contaRepository);
////			break;
////		case CARTAO_DEBITO:
////		case CARTAO_CREDITO:
////			validarCartao(usuarioLogado, idContaOrigem, contaRepository);
////			break;
////		}
////	}
////
////	private static void validarTransferencia(User usuarioLogado, Long idContaOrigem, Long idContaDestino,
////			ContaRepository contaRepository) {
////		// 1. Obter os clientes das contas
////		Conta contaOrigem = contaRepository.findById(idContaOrigem)
////				.orElseThrow(() -> new ContaNaoEncontradaException("Conta origem não encontrada"));
////
////		Conta contaDestino = contaRepository.findById(idContaDestino)
////				.orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));
////
////		Long idClienteLogado = usuarioLogado.getCliente().getId();
////
////		// 2. Regras para ADMIN
////		if (isAdmin(usuarioLogado)) {
////			// Admin pode transferir entre contas de terceiros
////			// Mas não pode ser o beneficiário (destino)
////			if (contaDestino.getCliente().getId().equals(idClienteLogado)) {
////				throw new ClienteEncontradoException("Admin não pode realizar transferências da conta de clientes para a própria conta");
////			}
////
////			// Opcional: Validar se a conta origem pertence ao admin
////			if (!contaOrigem.getCliente().getId().equals(idClienteLogado)) {
////				throw new ClienteEncontradoException("Admin só pode transferir da própria conta");
////			}
////		}
////
////		// 3. Regras gerais (aplicam-se a todos os usuários)
////		if (contaOrigem.getCliente().getId().equals(contaDestino.getCliente().getId())) {
////			throw new ClienteEncontradoException("Não é permitido transferência para a mesma conta");
////		}
////
////		if (!contaOrigem.getStatus()) {
////			throw new ClienteEncontradoException("Conta origem está desativada");
////		}
////
////		if (!contaDestino.getStatus()) {
////			throw new ClienteEncontradoException("Conta destino está desativada");
////		}
////	}
////
////	private static void validarDeposito(User usuarioLogado, Long idContaDestino, ContaRepository contaRepository) {
////		Conta contaDestino = contaRepository.findById(idContaDestino)
////				.orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));
////
////		if (isAdmin(usuarioLogado)) {
////			// Admin só pode depositar na própria conta
////			if (!contaDestino.getCliente().getId().equals(usuarioLogado.getCliente().getId())) {
////				throw new ClienteEncontradoException("Admin só pode depositar na própria conta");
////			}
////		}
////
////		if (!contaDestino.getStatus()) {
////			throw new ClienteEncontradoException("Conta destino está desativada");
////		}
////	}
////
////	private static void validarSaque(User usuarioLogado, Long idContaOrigem, ContaRepository contaRepository) {
////		Conta contaOrigem = contaRepository.findById(idContaOrigem)
////				.orElseThrow(() -> new ContaNaoEncontradaException("Conta origem não encontrada"));
////
////		if (isAdmin(usuarioLogado)) {
////			// Admin só pode sacar da própria conta
////			if (!contaOrigem.getCliente().getId().equals(usuarioLogado.getCliente().getId())) {
////				throw new ClienteEncontradoException("Admin só pode sacar da própria conta");
////			}
////		}
////
////		if (!contaOrigem.getStatus()) {
////			throw new ClienteEncontradoException("Conta origem está desativada");
////		}
////	}
////	
////	private static void validarCartao(User usuarioLogado, Long idConta, ContaRepository contaRepository) {
////	Conta conta = contaRepository.findById(idConta)
////			.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
////
////	if (isAdmin(usuarioLogado)) {
////		// Admin só pode operar cartões da própria conta
////		if (!conta.getCliente().getId().equals(usuarioLogado.getCliente().getId())) {
////			throw new ClienteEncontradoException("Admin só pode operar cartões da própria conta");
////		}
////	}
////
////	if (!conta.getStatus()) {
////		throw new ClienteEncontradoException("Conta está desativada");
////	}
////}
////	
////}
////
////
////
