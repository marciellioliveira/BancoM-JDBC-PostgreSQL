package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dao.CartaoDao;
import br.com.marcielli.bancom.dao.ClienteDao;
import br.com.marcielli.bancom.dao.ContaDao;
import br.com.marcielli.bancom.dao.FaturaDao;
import br.com.marcielli.bancom.dao.PagamentoFaturaDao;
import br.com.marcielli.bancom.dao.TransferenciaDao;
import br.com.marcielli.bancom.dao.UserDao;
import br.com.marcielli.bancom.dto.CartaoCreateDTO;
import br.com.marcielli.bancom.dto.CartaoUpdateDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarLimiteCartaoCreditoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarLimiteCartaoDebitoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoPagCartaoDTO;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Fatura;
import br.com.marcielli.bancom.entity.PagamentoFatura;
import br.com.marcielli.bancom.entity.TaxaManutencao;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
import br.com.marcielli.bancom.exception.AcessoNegadoException;
import br.com.marcielli.bancom.exception.CartaoNaoEncontradoException;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoTemSaldoSuficienteException;
import br.com.marcielli.bancom.exception.ContaExisteNoBancoException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;
import br.com.marcielli.bancom.exception.FaturaNaoEncontradaException;
import br.com.marcielli.bancom.exception.FaturaNaoTemPermissaoException;
import br.com.marcielli.bancom.exception.PermissaoNegadaException;
import br.com.marcielli.bancom.exception.TransferenciaNaoRealizadaException;
import br.com.marcielli.bancom.utils.GerarNumeros;

@Service
public class UserCartaoService {

	private final ClienteDao clienteDao;
	private final UserDao userDao;
	private final TransferenciaDao transferenciaDao;
	private final ContaDao contaDao;
	private final CartaoDao cartaoDao;
	private final GerarNumeros gerarNumeros;
	private final BCryptPasswordEncoder passwordEncoder;
	private final FaturaDao faturaDao;
	private final PagamentoFaturaDao pagFaturaDao;

	private static final Logger logger = LoggerFactory.getLogger(UserCartaoService.class);

	public UserCartaoService(ClienteDao clienteDao, UserDao userDao, TransferenciaDao transferenciaDao,
			ContaDao contaDao, CartaoDao cartaoDao, GerarNumeros gerarNumeros, BCryptPasswordEncoder passwordEncoder,
			FaturaDao faturaDao, PagamentoFaturaDao pagFaturaDao) {
		this.clienteDao = clienteDao;
		this.userDao = userDao;
		this.transferenciaDao = transferenciaDao;
		this.contaDao = contaDao;
		this.cartaoDao = cartaoDao;
		this.gerarNumeros = gerarNumeros;
		this.passwordEncoder = passwordEncoder;
		this.faturaDao = faturaDao;
		this.pagFaturaDao = pagFaturaDao;
	}

	@Transactional
	public Cartao salvar(CartaoCreateDTO dto, Authentication authentication) {
		logger.info("Criando cartão com DTO: {}", dto);

		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		User loggedInUser = userDao.findByUsername(authentication.getName())
				.orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

		Conta conta = contaDao.findById(dto.getIdConta())
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
		
		if(!conta.getStatus()) {
			throw new ClienteNaoEncontradoException("Não é possível criar um cartão para conta desativada.");
		}

		// Se for BASIC e está tentando criar cartão para outro usuário, bloqueia
		if ("ROLE_BASIC".equals(role) && !dto.getIdCliente().equals(loggedInUser.getId().longValue())) {
			throw new ClienteNaoEncontradoException(
					"Usuário BASIC não tem permissão para criar cartão para outro usuário.");
		}

		Cliente clienteAlvo = clienteDao.findById(dto.getIdCliente().longValue())
				.orElseThrow(() -> new ContaNaoEncontradaException("Cliente não encontrado"));

		Conta contaDoUser = contaDao.findById(dto.getIdConta())
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));

		if (!contaDoUser.getCliente().getId().equals(clienteAlvo.getId())) {
			throw new ContaNaoEncontradaException("A conta informada não pertence ao cliente.");
		}

		if (!clienteAlvo.isClienteAtivo()) {
			throw new ClienteNaoEncontradoException("O cliente está desativado.");
		}

		String numCartao = gerarNumeros.gerarNumeroGeral();
		
		Cartao cartao = (dto.getTipoCartao() == TipoCartao.CREDITO) ? new CartaoCredito() : new CartaoDebito();

		String sufixo = (dto.getTipoCartao() == TipoCartao.CREDITO) ? "-CC" : "-CD";

		cartao.setTipoCartao(dto.getTipoCartao());
		cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
		cartao.setNumeroCartao(numCartao + sufixo);
		cartao.setStatus(true);
		cartao.setConta(conta);
		cartao.setTipoConta(contaDoUser.getTipoConta());
		cartao.setCategoriaConta(contaDoUser.getCategoriaConta());

		if (cartao instanceof CartaoCredito cc) {
			cc.setLimiteCreditoPreAprovado(
					contaDoUser.getCategoriaConta() == CategoriaConta.PREMIUM ? new BigDecimal("10000.00")
							: new BigDecimal("5000.00"));
		} else if (cartao instanceof CartaoDebito cd) {
			cd.setLimiteDiarioTransacao(new BigDecimal("2000.00"));
		}

		return cartaoDao.saveWithRelations(cartao);
	}

	@Transactional
	public List<Cartao> getCartoes(Authentication authentication) {

		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		if ("ROLE_ADMIN".equals(role)) {
			// Admin pode ver todos os cartões
			return cartaoDao.findAll();
		} else if ("ROLE_BASIC".equals(role)) {
			String username = authentication.getName();
			return cartaoDao.findByUsername(username); // Retorna todos os cartões desse usuário
		} else {
			throw new RuntimeException("Você não tem permissão para acessar a lista de cartões.");
		}
	}

	@Transactional
	public Cartao getCartoesById(Long id, Authentication authentication) throws AccessDeniedException {
		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		String username = authentication.getName(); // Pegando o username do logado

		if ("ROLE_ADMIN".equals(role)) {
			// Admin pode acessar qualquer cartão por ID
			return cartaoDao.findById(id).orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
		} else if ("ROLE_BASIC".equals(role)) {
			// Basic só pode acessar o cartão dele mesmo
			return cartaoDao.findByIdAndUsername(id, username).orElseThrow(() -> new CartaoNaoEncontradoException(
					"Cartão não encontrado ou você não tem permissão para acessá-lo."));
		} else {
			throw new AccessDeniedException("Você não tem permissão para acessar esse cartão.");
		}
	}

	
	@Transactional
	public Cartao updateSenha(Long cartaoId, CartaoUpdateDTO dto, Authentication authentication)
	        throws AccessDeniedException {
	    
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");
	    
	    String username = authentication.getName();
	    
//	    if(cartaoDao.findByUsername(username).isEmpty()) {
//	        throw new CartaoNaoEncontradoException("Nenhum cartão encontrado para o usuário: " + username);
//	    }

	    if ("ROLE_ADMIN".equals(role)) {
	        // ADMIN pode alterar qualquer cartão
	        Cartao cartao = cartaoDao.findById(cartaoId)
	                .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado"));
	        
	        // Verifica se o cartão está ativo
	        if (!cartao.isStatus()) {
	            throw new PermissaoNegadaException("Não é possível alterar senha de cartão desativado");
	        }
	        
	        cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
	        return cartaoDao.save(cartao);

	    } else if ("ROLE_BASIC".equals(role)) {
	        // BASIC só pode alterar o próprio cartão
	        
	        Cliente cliente = clienteDao.findById(dto.getIdCliente())
	                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
	        
	        // Verifica se o cartão pertence ao cliente
	        Cartao cartao = cartaoDao.findByIdAndClienteId(cartaoId, dto.getIdCliente())
	                .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado ou não pertence a você"));
	        
	        // Verifica se o cliente é o mesmo do usuário logado
	        if (!cliente.getUser().getUsername().equals(username)) {
	            throw new AccessDeniedException("Você só pode alterar a senha do seu próprio cartão");
	        }
	        
	        // Verifica se o cartão está ativo
	        if (!cartao.isStatus()) {
	            throw new PermissaoNegadaException("Não é possível alterar senha de cartão desativado");
	        }
	        
	        cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
	        return cartaoDao.save(cartao);

	    } else {
	        throw new AccessDeniedException("Você não tem permissão para esta operação");
	    }
	}

	

	//Ativar cartão
	@Transactional
	public boolean ativarCartao(Long idCartao, Authentication authentication) throws ClienteEncontradoException {
		
	 String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    if (!"ROLE_ADMIN".equals(role)) {
	        // Somente admin pode ativar cliente
	    	throw new ClienteEncontradoException("Somente administradores podem ativar o cartão");	        
	    } 
	    
	    if (!cartaoDao.existeCartao(idCartao)) {
	        throw new CartaoNaoEncontradoException("Cartão não encontrado.");
	    }
	    
	    return cartaoDao.ativarCartao(idCartao);	    
	}
	
	@Transactional
	public boolean delete(Long idCartao, Authentication authentication) {
		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		String username = authentication.getName();
		
		Conta contaAssociada = verificarContaAssociadaAoCartao(idCartao);

		if ("ROLE_ADMIN".equals(role)) {
			logger.info("Conta carregada: {}", contaAssociada);

			if (contaAssociada.getSaldoConta().compareTo(BigDecimal.ZERO) > 0) {
				throw new IllegalArgumentException(
						"A conta associada ao cartão possui saldo positivo. Faça o saque antes de remover o cartão.");
			} else if (contaAssociada.getSaldoConta().compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException(
						"A conta associada ao cartão está com saldo negativo. Regularize antes de remover o cartão.");
			}

			if (!contaAssociada.getStatus()) {
				throw new IllegalArgumentException("A conta associada ao cartão já está desativada.");
			}
		} else if ("ROLE_BASIC".equals(role)) {
			
			if (!contaAssociada.getCliente().getUser().getUsername().equals(username)) {
				throw new IllegalArgumentException("Você não tem permissão para excluir este cartão");
			}

			if (contaAssociada.getSaldoConta().compareTo(BigDecimal.ZERO) > 0) {
				throw new IllegalArgumentException(
						"A conta associada ao cartão possui saldo positivo. Faça o saque antes de remover o cartão.");
			} else if (contaAssociada.getSaldoConta().compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException(
						"A conta associada ao cartão está com saldo negativo. Regularize antes de remover o cartão.");
			}

			if (!contaAssociada.getStatus()) {
				throw new IllegalArgumentException("A conta associada ao cartão já está desativada.");
			}
		}
		// Caso a role não seja nem ADMIN nem BASIC
		else {
			logger.error("Você não tem permissão para deletar este cartão - Role: " + role);
			throw new IllegalArgumentException("Role não autorizada para deletar cartões: " + role);
		}

		cartaoDao.desativarCartao(idCartao);
		return true;
	}

	public Conta verificarContaAssociadaAoCartao(Long idCartao) {
		Cartao cartaoExistente = cartaoDao.findById(idCartao)
				.orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado"));

		if (cartaoExistente.getConta() == null) {
			throw new IllegalArgumentException("Conta associada ao cartão não encontrada");
		}

		Long idConta = cartaoExistente.getConta().getId();
		logger.info("cartao: {}, contaId: {}", cartaoExistente.getId(), idConta);

		// Agora busca a CONTA COMPLETA usando o contaDao:
		Conta contaAssociada = contaDao.findById(idConta)
				.orElseThrow(() -> new IllegalArgumentException("Conta não encontrada pelo ID"));

		logger.info("Conta completa carregada: {}", contaAssociada);

		List<Cartao> cartoesDaConta = cartaoDao.findByContaId(idConta);
		if (cartoesDaConta.size() <= 1) {
			throw new IllegalArgumentException(
					"Não é possível excluir este cartão, pois é o único cartão associado à conta.");
		}

		return contaAssociada;
	}

	@Transactional
	public boolean pagCartao(Long idContaReceber, UserCartaoPagCartaoDTO dto, Authentication authentication) {

		// Validando inputs
		if (dto.idCartao() == null) {
			throw new IllegalArgumentException("ID do cartão é obrigatório");
		}
		if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("O valor deve ser maior que zero");
		}
		if (dto.senha() == null || dto.senha().isEmpty()) {
			throw new IllegalArgumentException("Senha do cartão é obrigatória");
		}

		// Cartão que vai fazer o pagamento
		Cartao cartaoOrigem = cartaoDao.findById(dto.idCartao())
				.orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado"));
		logger.info("Cartão nulo?: {}", cartaoOrigem);
		// Validando a senha do cartão
		if (!passwordEncoder.matches(dto.senha(), cartaoOrigem.getSenha())) {
			throw new IllegalArgumentException("Senha do cartão incorreta");
		}

		// Validando o status do cartão
		if (!cartaoOrigem.isStatus()) {
			throw new PermissaoNegadaException("Cartão está desativado");
		}

		// Conta que vai fazer o pagamento
		Conta contaOrigem = contaDao.findById(cartaoOrigem.getConta().getId())
				.orElseThrow(() -> new IllegalArgumentException("Conta associada ao cartão não encontrada"));

		// Conta que vai receber o valor
		Conta contaDestino = contaDao.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));

		// Validando a conta que vai receber o valor
		if (!contaOrigem.getStatus() || !contaDestino.getStatus()) {
			throw new PermissaoNegadaException("Uma das contas está desativada");
		}

		// Validando que a conta de origem não é a mesma que a conta de destino
		if (contaOrigem.getId().equals(contaDestino.getId())) {
		    throw new PermissaoNegadaException("A conta de origem e a conta de destino não podem ser a mesma");
		}
		
		// Cliente que vai enviar o valor
		Cliente clienteOrigem = clienteDao.findById(contaOrigem.getCliente().getId())
				.orElseThrow(() -> new IllegalArgumentException("Cliente da conta de origem não encontrado"));

		// Usuario que está logado - aqui preciso disso porque se for ADMIN preciso
		// autenticar se ele está enviando da conta dele ou da conta de outra pessoa
		User usuarioLogado = userDao.findByUsername(authentication.getName())
				.orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

		// se for user basic e ele estiver tentando enviar de outro usuario para outra
		// pessoa, vai barrar
		if (!isAdmin && !clienteOrigem.getId().equals(usuarioLogado.getId().longValue())) {
			throw new AcessoNegadoException("Você só pode pagar com cartão vinculado à sua própria conta");
		}
		Long faturaId = null;
		// transferencia - debito ou credito
		// se o cartão de origem for credito(não vai retirar valor do saldo), vai passar
		// como credito e precisa entrar nas duas listas: transferenciasCredito (pq ele
		// não ta tirando do dinheiro dele no
		// momento, está pagando no credito e transferenciasEnviadas porque foi um tipo
		// de transferencia enviada

		// se for debito, precisa descontar do valor da conta e somente entrar em
		// transferenciasEnviadas
		Transferencia transferencia = new Transferencia(contaOrigem, dto.valor(), contaDestino,
				(cartaoOrigem instanceof CartaoCredito) ? TipoTransferencia.CARTAO_CREDITO
						: TipoTransferencia.CARTAO_DEBITO,
				cartaoOrigem.getTipoCartao());
		transferencia.setIdCartao(cartaoOrigem.getId());
		transferencia.setIdClienteOrigem(clienteOrigem.getId());
		transferencia.setIdClienteDestino(contaDestino.getCliente().getId());
		transferencia.setIdContaOrigem(contaOrigem.getId());
		transferencia.setIdContaDestino(contaDestino.getId());
		transferencia.setData(LocalDateTime.now());
		transferencia.setCodigoOperacao(gerarNumeros.gerarNumeroGeral());

		if (contaOrigem.getTransferencias() == null) {
			contaOrigem.setTransferencias(new ArrayList<>());
		}

		if (cartaoOrigem instanceof CartaoCredito cartaoCredito) {
			if (dto.valor().compareTo(cartaoCredito.getLimiteCreditoPreAprovado()) > 0) {
				throw new TransferenciaNaoRealizadaException("Limite de crédito insuficiente");
			}

			BigDecimal novoLimite = cartaoCredito.getLimiteCreditoPreAprovado().subtract(dto.valor());
			BigDecimal novoTotalGasto = cartaoCredito.getTotalGastoMesCredito() != null
					? cartaoCredito.getTotalGastoMesCredito().add(dto.valor())
					: dto.valor();
			cartaoCredito.setLimiteCreditoPreAprovado(novoLimite);
			cartaoCredito.setTotalGastoMesCredito(novoTotalGasto);
			 
			
			//Se tiver fatura, salva nela, se não tiver cria uma e salva na nova
			Optional<Fatura> faturaOptional = faturaDao.findByCartaoId(cartaoCredito.getId());
			Fatura fatura;
			
			if (faturaOptional.isPresent()) { //fatura existe, então atualiza
			    
			    fatura = faturaOptional.get();
			    fatura.setValorTotal(fatura.getValorTotal().add(dto.valor()));
			    
			    faturaId = fatura.getId();
			    
			    faturaDao.update(fatura);
			    
			} else { //não existe fatura, então cria uma nova
			    
			    fatura = new Fatura();
			    fatura.setCartao(cartaoCredito);
			    fatura.setDataVencimento(LocalDateTime.now().plusDays(10));
			    fatura.setValorTotal(dto.valor());
			    fatura.setStatus(true); //será setado como false quando a fatura for paga
			    
			    faturaId = faturaDao.save(fatura);
			    logger.info("Antes de faturaId != null: {}", faturaId);
			    if (faturaId != null) {
			    	logger.info("Setando fatura id: {}", faturaId);
			        fatura.setId(faturaId);
			    } else {
			    	logger.info("Fatura id não gerado: {}", faturaId);
			        throw new RuntimeException("Erro ao salvar a fatura. ID não gerado.");
			    }
			}
			
			transferencia.setFatura(fatura);
			

			Long transferenciaId = transferenciaDao.save(transferencia);
			transferencia.setId(transferenciaId);

			transferenciaDao.associarTransferenciaAFatura(faturaId, transferenciaId);
			cartaoDao.associarFaturaAoCartao(cartaoCredito.getId(), faturaId);

			cartaoCredito.setFaturaId(faturaId);
			cartaoDao.update(cartaoCredito);

			contaOrigem.getTransferencias().add(transferencia);
			fatura.getTransferenciasCredito().add(transferencia);

		} else if (cartaoOrigem instanceof CartaoDebito cartaoDebito) {

			if (dto.valor().compareTo(cartaoDebito.getLimiteDiarioTransacao()) > 0) {
				throw new TransferenciaNaoRealizadaException("Limite diário excedido");
			}
			if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
				throw new TransferenciaNaoRealizadaException("Saldo insuficiente");
			}

			contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
			cartaoDebito.setLimiteDiarioTransacao(cartaoDebito.getLimiteDiarioTransacao().subtract(dto.valor()));
			BigDecimal novoTotalGastoMes = cartaoDebito.getTotalGastoMes() != null
					? cartaoDebito.getTotalGastoMes().add(dto.valor())
					: dto.valor();
			cartaoDebito.setTotalGastoMes(novoTotalGastoMes);

			contaOrigem.getTransferencias().add(transferencia);
		}

		contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
		TaxaManutencao taxaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());
		contaDestino.setCategoriaConta(taxaDestino.getCategoria());
		contaDestino.setTaxas(List.of(taxaDestino));

		if (!(cartaoOrigem instanceof CartaoCredito)) {
			Long transferenciaId = transferenciaDao.save(transferencia);
			transferencia.setId(transferenciaId);
		}

		contaDao.update(contaOrigem);
		contaDao.update(contaDestino);

		return true;
	}
	
	
	@Transactional
	public Cartao alterarLimiteCartaoCredito(Long cartaoId, UserCartaoAlterarLimiteCartaoCreditoDTO dto, Authentication authentication) {

	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    if (!"ROLE_ADMIN".equals(role)) {
	        throw new ClienteEncontradoException("Somente administradores podem alterar o limite do cartão.");
	    }
	 
	    Cartao cartao = cartaoDao.findById(cartaoId)
	        .orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));

	    if (!cartao.isStatus()) {
	        throw new CartaoNaoEncontradoException("Não é possível alterar limite de cartão desativado.");
	    }

	    if (dto.novoLimite() == null) {
	        throw new CartaoNaoEncontradoException("Você precisa digitar um valor para o novo limite do cartão.");
	    }

	    if (cartao instanceof CartaoCredito cartaoCredito) {
	        cartaoCredito.alterarLimiteCreditoPreAprovado(dto.novoLimite());
	    }

	    cartaoDao.alterarLimiteCartaoCredito(cartao);
	    return cartao;
	}

	@Transactional
	public Cartao alterarLimiteCartaoDebito(Long cartaoId, UserCartaoAlterarLimiteCartaoDebitoDTO dto, Authentication authentication) {
		
		 String role = authentication.getAuthorities().stream()
			        .map(GrantedAuthority::getAuthority)
			        .findFirst()
			        .orElse("");

	    if (!"ROLE_ADMIN".equals(role)) {
	        throw new ClienteEncontradoException("Somente administradores podem alterar o limite do cartão.");
	    }
	    
		Cartao cartao = cartaoDao.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		if(cartao.isStatus() == false) {
			throw new CartaoNaoEncontradoException("Não é possível alterar limite de cartão desativado.");
		}

		BigDecimal novoLimite = dto.novoLimite();

		if (dto.novoLimite() == null) {
			throw new CartaoNaoEncontradoException("Você precisa digitar um valor para o novo limite do cartão");
		}

		if (cartao instanceof CartaoDebito cartaoDebito) {
			cartaoDebito.alterarLimiteDiarioTransacao(novoLimite);
		}

		cartaoDao.alterarLimiteCartaoDebito(cartao);
		return cartao;
	}
	
	@Transactional
	public Fatura verFaturaCartaoCredito(Long cartaoId, Authentication authentication) {
	    
	    logger.info("Cartão Id dto {}", cartaoId);
	    
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    User loggedInUser = userDao.findByUsername(authentication.getName())
	            .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

	    Cartao cartao = cartaoDao.findById(cartaoId)
	            .orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));

	    logger.info("cartao.getid: {}", cartao.getId());
	    
	    if (!(cartao instanceof CartaoCredito)) {
	        logger.info("Tentativa de consultar fatura para cartão de débito ID: {}", cartaoId);
	        throw new IllegalArgumentException("Faturas só estão disponíveis para cartões de crédito.");
	    }

	    Conta conta = cartao.getConta();
	    if (conta == null || conta.getCliente() == null) {
	        logger.error("Cartão ID {} tem conta ou cliente nulo: conta={}, cliente={}", 
	            cartaoId, conta, conta != null ? conta.getCliente() : null);
	        throw new FaturaNaoTemPermissaoException("O cartão não está associado a uma conta ou cliente válido.");
	    }

	    if ("ROLE_BASIC".equals(role)) {
	        Long idClienteCartao = conta.getCliente().getId();
	        logger.info("Cliente basic idClienteCartao: {}", idClienteCartao);
	        logger.info("loggedInUser.getId().longValue(): {}", loggedInUser.getId().longValue());
	        if (!idClienteCartao.equals(loggedInUser.getId().longValue())) {
	            throw new FaturaNaoTemPermissaoException("Você não tem permissão para visualizar esta fatura.");
	        }
	    }

	    Fatura fatura = cartaoDao.buscarFaturaComTransferenciasPorCartaoId(cartaoId);
	    if (fatura == null) {
	        logger.info("Nenhuma fatura encontrada para cartão ID: {}", cartaoId);
	        throw new FaturaNaoEncontradaException("Nenhuma fatura encontrada para o cartão ID: " + cartaoId);
	    }
	    
	    return fatura;
	}
	
	
	
	@Transactional
	public boolean pagFaturaCartaoC(Long idCartao, Authentication authentication) {
	    logger.info("Iniciando pagamento da fatura para cartão ID: {}", idCartao);
	    
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    User loggedInUser = userDao.findByUsername(authentication.getName())
	            .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

	    Cartao cartaoOrigem = cartaoDao.findById(idCartao)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("O cartão não existe no banco."));

	    logger.info("Cartão encontrado ID: {}", cartaoOrigem.getId());
	    
	    if (!(cartaoOrigem instanceof CartaoCredito)) {
	        logger.error("Tentativa de pagar fatura com cartão de débito ID: {}", idCartao);
	        throw new IllegalArgumentException("Pagamento de fatura só está disponível para cartões de crédito.");
	    }

	    Conta contaOrigem = cartaoOrigem.getConta();
	    if (contaOrigem == null || contaOrigem.getCliente() == null) {
	        logger.error("Cartão ID {} tem conta ou cliente nulo: conta={}, cliente={}", 
	            idCartao, contaOrigem, contaOrigem != null ? contaOrigem.getCliente() : null);
	        throw new FaturaNaoTemPermissaoException("O cartão não está associado a uma conta ou cliente válido.");
	    }

	    if ("ROLE_BASIC".equals(role)) {
	        Long idClienteCartao = contaOrigem.getCliente().getId();
	        logger.info("Verificando permissão BASIC - Cliente do cartão: {}, Usuário logado: {}", 
	            idClienteCartao, loggedInUser.getId());
	        
	        if (!idClienteCartao.equals(loggedInUser.getId().longValue())) {
	            throw new FaturaNaoTemPermissaoException("Você não tem permissão para realizar pagamentos nesta fatura.");
	        }
	    }

	    if (!cartaoOrigem.isStatus()) {
	        throw new CartaoNaoEncontradoException("Não é possível pagar fatura através de um cartão desativado.");
	    }
	    
	    if (!contaOrigem.getStatus()) {
	        throw new CartaoNaoEncontradoException("Não é possível pagar fatura através de uma conta desativada.");
	    }

	    CartaoCredito cc = (CartaoCredito) cartaoOrigem;
	    
	    if (cc.getTotalGastoMesCredito().compareTo(contaOrigem.getSaldoConta()) > 0) {
	        throw new ClienteNaoTemSaldoSuficienteException("Você não tem saldo suficiente para realizar o pagamento.");
	    }

	    Fatura fatura = cartaoDao.buscarFaturaComTransferenciasPorCartaoId(idCartao);
	    if (fatura == null) {
	        throw new FaturaNaoEncontradaException("Nenhuma fatura encontrada para pagamento.");
	    }
	    
	    if (fatura.getTransferenciasCredito() == null || fatura.getTransferenciasCredito().isEmpty()) {
	        logger.warn("Fatura ID {} não possui transferências de crédito vinculadas. Nada a pagar.", fatura.getId());
	        throw new FaturaNaoEncontradaException("Nenhuma fatura disponível para pagamento.");
	    }
	    
	    logger.info("Saldo da conta {} antes do pagamento", contaOrigem.getSaldoConta());
	    logger.info("Fatura valor total {} antes do pagamento", fatura.getValorTotal());
	    contaOrigem.pagarFatura(cc.getTotalGastoMesCredito());
	    
	    for(Transferencia transf : fatura.getTransferenciasCredito()) {
	    	Transferencia transferenciaCompleta = transferenciaDao.findById(transf.getId());
	    	
	    	logger.info("Transferencia id: {}", transferenciaCompleta.getId());
	    	
	    	
	    	//uso isso porque tenho uma tabela de associação no rowmapper que precisa remover o vinculo antes
	    	faturaDao.removerVinculoFaturaTransferencia(fatura.getId(), transferenciaCompleta.getId());	    	
	    	
	        transferenciaCompleta.setFaturaId(null); //ao setar como null,eu consigo desvincular as transferencias já pagas no banco
	    	transferenciaDao.update(transferenciaCompleta);
	    }
	    
	    fatura.getTransferenciasCredito().clear();
	    
	    fatura.setStatus(true);
	   
	    
	    //Registro de Pagamento de Fatura para ter um histórico quando precisar.
	    PagamentoFatura historicoPagamento = new PagamentoFatura(fatura.getId(), cc.getId(),contaOrigem.getId(), cc.getTotalGastoMesCredito());
	    pagFaturaDao.salvar(historicoPagamento);
	    
	    fatura.setValorTotal(BigDecimal.ZERO);  
	    cc.setTotalGastoMesCredito(BigDecimal.ZERO);
	  
	    
	    faturaDao.update(fatura);
	    
	    logger.info("Pagamento da fatura ID {} realizado com sucesso", fatura.getId());
	    logger.info("Saldo da conta {} depois do pagamento", contaOrigem.getSaldoConta());
	    logger.info("Fatura valor total {} depois do pagamento", fatura.getValorTotal());
	    return true;
	}	
}
