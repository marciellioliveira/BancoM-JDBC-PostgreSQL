package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import br.com.marcielli.bancom.dao.TransferenciaDao;
import br.com.marcielli.bancom.dao.UserDao;
import br.com.marcielli.bancom.dto.CartaoCreateDTO;
import br.com.marcielli.bancom.dto.CartaoUpdateDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoPagCartaoDTO;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Fatura;
import br.com.marcielli.bancom.entity.TaxaManutencao;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
import br.com.marcielli.bancom.exception.AcessoNegadoException;
import br.com.marcielli.bancom.exception.CartaoNaoEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;
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
	
	private static final Logger logger = LoggerFactory.getLogger(UserCartaoService.class);	
	
	
	public UserCartaoService(ClienteDao clienteDao, UserDao userDao, TransferenciaDao transferenciaDao, ContaDao contaDao, CartaoDao cartaoDao, GerarNumeros gerarNumeros, BCryptPasswordEncoder passwordEncoder, FaturaDao faturaDao) {
		this.clienteDao = clienteDao;
		this.userDao = userDao;
		this.transferenciaDao = transferenciaDao;
		this.contaDao = contaDao;
		this.cartaoDao = cartaoDao;
		this.gerarNumeros = gerarNumeros;
		this.passwordEncoder = passwordEncoder;
		this.faturaDao = faturaDao;
	}

	@Transactional
    public Cartao salvar(CartaoCreateDTO dto, Authentication authentication) {
		logger.info("Criando cartão com DTO: {}", dto);

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        
        User loggedInUser = userDao.findByUsername(authentication.getName())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));
        
        Conta conta = contaDao.findById(dto.getIdConta())
                .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));

        // Se for BASIC e está tentando criar cartão para outro usuário, bloqueia
        if ("ROLE_BASIC".equals(role) && !dto.getIdCliente().equals(loggedInUser.getId().longValue())) {
            throw new ClienteNaoEncontradoException("Usuário BASIC não tem permissão para criar cartão para outro usuário.");
        }

        Cliente clienteAlvo = clienteDao.findById(dto.getIdCliente().longValue())
                .orElseThrow(() -> new ContaNaoEncontradaException("Cliente não encontrado"));

        Conta contaDoUser = contaDao.findById(dto.getIdConta())
                .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
        
        if(!contaDoUser.getCliente().getId().equals(clienteAlvo.getId())) {
            throw new ContaNaoEncontradaException("A conta informada não pertence ao cliente.");
        }
        
        if(!clienteAlvo.isClienteAtivo()) {
            throw new ClienteNaoEncontradoException("O cliente está desativado.");
        }
        
        String numCartao = gerarNumeros.gerarNumeroGeral();
        Cartao cartao = (dto.getTipoCartao() == TipoCartao.CREDITO) ? 
                new CartaoCredito() : new CartaoDebito();

        String sufixo = (dto.getTipoCartao() == TipoCartao.CREDITO) ? "-CC" : "-CD";
        
        cartao.setTipoCartao(dto.getTipoCartao());
        cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
        cartao.setNumeroCartao(numCartao + sufixo);
        cartao.setStatus(true);
        cartao.setConta(conta);
        cartao.setTipoConta(contaDoUser.getTipoConta());
        cartao.setCategoriaConta(contaDoUser.getCategoriaConta());

        if(cartao instanceof CartaoCredito cc) {
            cc.setLimiteCreditoPreAprovado(contaDoUser.getCategoriaConta() == CategoriaConta.PREMIUM ? 
                new BigDecimal("10000.00") : new BigDecimal("5000.00"));
        } else if(cartao instanceof CartaoDebito cd) {
            cd.setLimiteDiarioTransacao(new BigDecimal("2000.00"));
        }

        return cartaoDao.saveWithRelations(cartao);
    }
	
	
	@Transactional
	public List<Cartao> getCartoes(Authentication authentication) {
	    
		String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
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
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    String username = authentication.getName(); // Pegando o username do logado

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode acessar qualquer cartão por ID
	        return cartaoDao.findById(id)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode acessar o cartão dele mesmo
	        return cartaoDao.findByIdAndUsername(id, username)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado ou você não tem permissão para acessá-lo."));
	    } else {
	        throw new AccessDeniedException("Você não tem permissão para acessar esse cartão.");
	    }
	}


	@Transactional
	public Cartao updateSenha(Long cartaoId, CartaoUpdateDTO dto, Authentication authentication) throws AccessDeniedException {
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    String username = authentication.getName();

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode atualizar qualquer cartão
	        Cartao cartao = cartaoDao.findById(cartaoId)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado"));

	        // Atualiza a senha
	        cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
	        return cartaoDao.save(cartao);

	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode atualizar o cartão dele
	        Cartao cartao = cartaoDao.findByIdAndUsername(cartaoId, username)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado ou você não tem permissão para acessá-lo"));

	        if (cartao.getConta().getCliente().getId() != dto.getIdCliente()) {
	            throw new ClienteNaoEncontradoException("Você não tem permissão para alterar esse cartão.");
	        }

	        if (!cartao.isStatus()) {
	            throw new PermissaoNegadaException("Não é possível atualizar a senha de um cartão desativado.");
	        }

	        cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
	        return cartaoDao.save(cartao);
	        
	    } else {
	    	logger.error("Você não tem permissão para atualizar a senha desse cartão"+role);
	        throw new AccessDeniedException("Você não tem permissão para atualizar a senha desse cartão.");
	    }
	}

	
	@Transactional
	public boolean delete(Long idCartao, Authentication authentication) {
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    String username = authentication.getName();

	    // Usar o novo método para verificar a conta associada ao cartão
	    Conta contaAssociada = verificarContaAssociadaAoCartao(idCartao);

	    // Verificação de permissões e saldo da conta (continua igual)
	    if ("ROLE_ADMIN".equals(role)) {
	        logger.info("Conta carregada: {}", contaAssociada);

	        if (contaAssociada.getSaldoConta().compareTo(BigDecimal.ZERO) > 0) {
	            throw new IllegalArgumentException("A conta associada ao cartão possui saldo positivo. Faça o saque antes de remover o cartão.");
	        } else if (contaAssociada.getSaldoConta().compareTo(BigDecimal.ZERO) < 0) {
	            throw new IllegalArgumentException("A conta associada ao cartão está com saldo negativo. Regularize antes de remover o cartão.");
	        }

	        if (!contaAssociada.getStatus()) {
	            throw new IllegalArgumentException("A conta associada ao cartão já está desativada.");
	        }
	    } 
	    else if ("ROLE_BASIC".equals(role)) {
	        // Verificar se o usuário logado é o proprietário da conta
	        if (!contaAssociada.getCliente().getUser().getUsername().equals(username)) {
	            throw new IllegalArgumentException("Você não tem permissão para excluir este cartão");
	        }

	        if (contaAssociada.getSaldoConta().compareTo(BigDecimal.ZERO) > 0) {
	            throw new IllegalArgumentException("A conta associada ao cartão possui saldo positivo. Faça o saque antes de remover o cartão.");
	        } else if (contaAssociada.getSaldoConta().compareTo(BigDecimal.ZERO) < 0) {
	            throw new IllegalArgumentException("A conta associada ao cartão está com saldo negativo. Regularize antes de remover o cartão.");
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

	    cartaoDao.deleteCartao(idCartao);
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
	        throw new IllegalArgumentException("Não é possível excluir este cartão, pois é o único cartão associado à conta.");
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

        //Cartão que vai fazer o pagamento
        Cartao cartaoOrigem = cartaoDao.findById(dto.idCartao())
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado"));
       
        //Validando a senha do cartão
        if (!passwordEncoder.matches(dto.senha(), cartaoOrigem.getSenha())) {
            throw new IllegalArgumentException("Senha do cartão incorreta");
        }
       
        //Validando o status do cartão
        if (!cartaoOrigem.isStatus()) {
            throw new PermissaoNegadaException("Cartão está desativado");
        }

        //Conta que vai fazer o pagamento
        Conta contaOrigem = contaDao.findById(cartaoOrigem.getConta().getId())
                .orElseThrow(() -> new IllegalArgumentException("Conta associada ao cartão não encontrada"));
        
        // Conta que vai receber o valor
        Conta contaDestino = contaDao.findById(idContaReceber)
                .orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));

        //Validando a conta que vai receber o valor
        if (!contaOrigem.getStatus() || !contaDestino.getStatus()) {
            throw new PermissaoNegadaException("Uma das contas está desativada");
        }

        //Cliente que vai enviar o valor
        Cliente clienteOrigem = clienteDao.findById(contaOrigem.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente da conta de origem não encontrado"));
        
        // Usuario que está logado - aqui preciso disso porque se for ADMIN preciso autenticar se ele está enviando da conta dele ou da conta de outra pessoa
        User usuarioLogado = userDao.findByUsername(authentication.getName())
                .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));
        
        boolean isAdmin = authentication.getAuthorities().stream()        		
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        //se for user basic e ele estiver tentando enviar de outro usuario para outra pessoa, vai barrar
        if (!isAdmin && !clienteOrigem.getId().equals(usuarioLogado.getId().longValue())) {
            throw new AcessoNegadoException("Você só pode pagar com cartão vinculado à sua própria conta");
        }

        //transferencia - debito ou credito
        //se o cartão de origem for credito(não vai retirar valor do saldo), vai passar como credito e precisa entrar nas duas listas: transferenciasCredito (pq ele não ta tirando do dinheiro dele no
        //momento, está pagando no credito e transferenciasEnviadas porque foi um tipo de transferencia enviada
        
        //se for debito, precisa descontar do valor da conta e somente entrar em transferenciasEnviadas
        Transferencia transferencia = new Transferencia(
                contaOrigem,
                dto.valor(),
                contaDestino,
                (cartaoOrigem instanceof CartaoCredito) ? TipoTransferencia.CARTAO_CREDITO : TipoTransferencia.CARTAO_DEBITO,
                cartaoOrigem.getTipoCartao()
        );
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
        
     // Mantenha todo o código anterior igual até o bloco do cartão de crédito

        if (cartaoOrigem instanceof CartaoCredito cartaoCredito) {
            if (dto.valor().compareTo(cartaoCredito.getLimiteCreditoPreAprovado()) > 0) {
                throw new TransferenciaNaoRealizadaException("Limite de crédito insuficiente");
            }

            BigDecimal novoLimite = cartaoCredito.getLimiteCreditoPreAprovado().subtract(dto.valor());
            BigDecimal novoTotalGasto = cartaoCredito.getTotalGastoMesCredito() != null ?
                    cartaoCredito.getTotalGastoMesCredito().add(dto.valor()) : dto.valor();
            cartaoCredito.setLimiteCreditoPreAprovado(novoLimite);
            cartaoCredito.setTotalGastoMesCredito(novoTotalGasto);
            
            Fatura fatura = faturaDao.findByCartaoId(cartaoCredito.getId())
                    .orElseGet(() -> {
                        Fatura novaFatura = new Fatura();
                        novaFatura.setCartao(cartaoCredito);
                        novaFatura.setDataVencimento(LocalDateTime.now().plusDays(10));
                        novaFatura.setValorTotal(BigDecimal.ZERO);
                        return novaFatura;
                    });

            fatura.setValorTotal(fatura.getValorTotal().add(dto.valor()));
            transferencia.setFatura(fatura);
            
            // Salva a fatura primeiro
            Long faturaId = faturaDao.save(fatura);
            fatura.setId(faturaId);
            
            // Salva a transferência
            Long transferenciaId = transferenciaDao.save(transferencia);
            transferencia.setId(transferenciaId);
            
            // Faz as associações
            transferenciaDao.associarTransferenciaAFatura(faturaId, transferenciaId);
            cartaoDao.associarFaturaAoCartao(cartaoCredito.getId(), faturaId);
            
            // Atualiza o cartão
            cartaoCredito.setFaturaId(faturaId);
            cartaoDao.update(cartaoCredito);
            
            // Adiciona às listas (apenas uma vez)
            contaOrigem.getTransferencias().add(transferencia);
            fatura.getTransferenciasCredito().add(transferencia);
            
        } else if (cartaoOrigem instanceof CartaoDebito cartaoDebito) {
            // Mantenha o código do débito exatamente como está
            if (dto.valor().compareTo(cartaoDebito.getLimiteDiarioTransacao()) > 0) {
                throw new TransferenciaNaoRealizadaException("Limite diário excedido");
            }
            if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
                throw new TransferenciaNaoRealizadaException("Saldo insuficiente");
            }

            contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
            cartaoDebito.setLimiteDiarioTransacao(cartaoDebito.getLimiteDiarioTransacao().subtract(dto.valor()));
            BigDecimal novoTotalGastoMes = cartaoDebito.getTotalGastoMes() != null ?
                    cartaoDebito.getTotalGastoMes().add(dto.valor()) : dto.valor();
            cartaoDebito.setTotalGastoMes(novoTotalGastoMes);
            
            // Para débito, adiciona apenas na lista geral
            contaOrigem.getTransferencias().add(transferencia);
        }

        // Atualiza o saldo da conta destino (comum para crédito e débito)
        contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
        TaxaManutencao taxaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());
        contaDestino.setCategoriaConta(taxaDestino.getCategoria());
        contaDestino.setTaxas(List.of(taxaDestino));

        // Salva a transferência apenas uma vez (já foi salva dentro do bloco do crédito)
        if (!(cartaoOrigem instanceof CartaoCredito)) {
            Long transferenciaId = transferenciaDao.save(transferencia);
            transferencia.setId(transferenciaId);
        }

        // Atualiza as contas
        contaDao.update(contaOrigem);
        contaDao.update(contaDestino);

        return true;

//        //se o cartão de origem for credito(não vai retirar valor do saldo), vai passar como credito e precisa entrar nas duas listas: transferenciasCredito (pq ele não ta tirando do dinheiro dele no
//        //momento, está pagando no credito e transferenciasEnviadas porque foi um tipo de transferencia enviada
//        //se for debito, precisa descontar do valor da conta e somente entrar em transferenciasEnviadas
//        if (cartaoOrigem instanceof CartaoCredito cartaoCredito) {
//            if (dto.valor().compareTo(cartaoCredito.getLimiteCreditoPreAprovado()) > 0) {
//                throw new TransferenciaNaoRealizadaException("Limite de crédito insuficiente");
//            }
//
//            BigDecimal novoLimite = cartaoCredito.getLimiteCreditoPreAprovado().subtract(dto.valor());
//            BigDecimal novoTotalGasto = cartaoCredito.getTotalGastoMesCredito() != null ?
//                    cartaoCredito.getTotalGastoMesCredito().add(dto.valor()) : dto.valor();
//            cartaoCredito.setLimiteCreditoPreAprovado(novoLimite);
//            cartaoCredito.setTotalGastoMesCredito(novoTotalGasto);
//            
//            Fatura fatura = faturaDao.findByCartaoId(cartaoCredito.getId())
//                    .orElseGet(() -> {
//                        Fatura novaFatura = new Fatura();
//                        novaFatura.setCartao(cartaoCredito);
//                        novaFatura.setDataVencimento(LocalDateTime.now().plusDays(10));
//                        novaFatura.setValorTotal(BigDecimal.ZERO);
//                        return novaFatura;
//                    });
//
//            BigDecimal atual = fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO;
//            fatura.setValorTotal(atual.add(dto.valor())); //precisei tratar antes com zero porque mesmo iniciando na entidade ainda estava dando null (também colquei no banco para não aceitar valor null"
//            //fatura.setValorTotal(fatura.getValorTotal().add(dto.valor()));
//            transferencia.setFatura(fatura);
//            fatura.adicionarTransfCredito(transferencia);
//                        
//            //teste inserindo na lista de transferencias e na lista de credito
//            contaOrigem.getTransferencias().add(transferencia);
//            fatura.getTransferenciasCredito().add(transferencia);
//            
//            fatura.setCartao(cartaoOrigem); // atribuindo o cartão a fatura
//            Long faturaId = faturaDao.save(fatura); //salvei a fatura e retornei o id
//            fatura.setId(faturaId);         
//            
//            //ligando a transferencia a fatura antes de salvar
//            transferencia.setFaturaId(faturaId);
//            
//            Long transferenciaId = transferenciaDao.save(transferencia); //salvo a transferencia e pego o id
//            transferencia.setId(transferenciaId);
//            transferenciaDao.associarTransferenciaAFatura(faturaId, transferenciaId); //associo a transferencia a fatura na tabela de ligação
//            cartaoDao.associarFaturaAoCartao(cartaoCredito.getId(), faturaId); //associei a fatura ao cartão
//            
//            cartaoCredito.setFaturaId(faturaId); //atualizando o objeto em memoria
//           
//            
//        } else if (cartaoOrigem instanceof CartaoDebito cartaoDebito) {
//            if (dto.valor().compareTo(cartaoDebito.getLimiteDiarioTransacao()) > 0) {
//                throw new TransferenciaNaoRealizadaException("Limite diário excedido");
//            }
//            if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
//                throw new TransferenciaNaoRealizadaException("Saldo insuficiente");
//            }
//
//            contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
//            cartaoDebito.setLimiteDiarioTransacao(cartaoDebito.getLimiteDiarioTransacao().subtract(dto.valor()));
//            BigDecimal novoTotalGastoMes = cartaoDebito.getTotalGastoMes() != null ?
//                    cartaoDebito.getTotalGastoMes().add(dto.valor()) : dto.valor();
//            cartaoDebito.setTotalGastoMes(novoTotalGastoMes);
//        }
//
//        contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
//        TaxaManutencao taxaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());
//        contaDestino.setCategoriaConta(taxaDestino.getCategoria());
//        contaDestino.setTaxas(List.of(taxaDestino));
//        
//        //teste somente na lista de transferencia
//        contaOrigem.getTransferencias().add(transferencia);
//       
//
//        Long transferenciaId = transferenciaDao.save(transferencia);
//        transferencia.setId(transferenciaId);
//        contaDao.update(contaOrigem);
//        contaDao.update(contaDestino);
//        cartaoDao.update(cartaoOrigem);
//
//        return true;
    }

//	@Transactional
//    public boolean pagCartao(Long idContaReceber, UserCartaoPagCartaoDTO dto, Authentication authentication) {
//       
//		// Validando inputs
//        if (dto.idCartao() == null) {
//            throw new IllegalArgumentException("ID do cartão é obrigatório");
//        }
//        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
//            throw new IllegalArgumentException("O valor deve ser maior que zero");
//        }
//        if (dto.senha() == null || dto.senha().isEmpty()) {
//            throw new IllegalArgumentException("Senha do cartão é obrigatória");
//        }
//
//        //Cartão que vai fazer o pagamento
//        Cartao cartaoOrigem = cartaoDao.findById(dto.idCartao())
//                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado"));
//       
//        //Validando a senha do cartão
//        if (!passwordEncoder.matches(dto.senha(), cartaoOrigem.getSenha())) {
//            throw new IllegalArgumentException("Senha do cartão incorreta");
//        }
//       
//        //Validando o status do cartão
//        if (!cartaoOrigem.isStatus()) {
//            throw new PermissaoNegadaException("Cartão está desativado");
//        }
//
//        //Conta que vai fazer o pagamento
//        Conta contaOrigem = contaDao.findById(cartaoOrigem.getConta().getId())
//                .orElseThrow(() -> new IllegalArgumentException("Conta associada ao cartão não encontrada"));
//        
//        // Conta que vai receber o valor
//        Conta contaDestino = contaDao.findById(idContaReceber)
//                .orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));
//
//        //Validando a conta que vai receber o valor
//        if (!contaOrigem.getStatus() || !contaDestino.getStatus()) {
//            throw new PermissaoNegadaException("Uma das contas está desativada");
//        }
//
//        //Cliente que vai enviar o valor
//        Cliente clienteOrigem = clienteDao.findById(contaOrigem.getCliente().getId())
//                .orElseThrow(() -> new IllegalArgumentException("Cliente da conta de origem não encontrado"));
//        
//        // Usuario que está logado - aqui preciso disso porque se for ADMIN preciso autenticar se ele está enviando da conta dele ou da conta de outra pessoa
//        User usuarioLogado = userDao.findByUsername(authentication.getName())
//                .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));
//        
//        boolean isAdmin = authentication.getAuthorities().stream()        		
//                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
//        
//        //se for user basic e ele estiver tentando enviar de outro usuario para outra pessoa, vai barrar
//        if (!isAdmin && !clienteOrigem.getId().equals(usuarioLogado.getId().longValue())) {
//            throw new AcessoNegadoException("Você só pode pagar com cartão vinculado à sua própria conta");
//        }
//
//        //transferencia - debito ou credito
//        //se o cartão de origem for credito(não vai retirar valor do saldo), vai passar como credito e precisa entrar nas duas listas: transferenciasCredito (pq ele não ta tirando do dinheiro dele no
//        //momento, está pagando no credito e transferenciasEnviadas porque foi um tipo de transferencia enviada
//        
//        //se for debito, precisa descontar do valor da conta e somente entrar em transferenciasEnviadas
//        Transferencia transferencia = new Transferencia(
//                contaOrigem,
//                dto.valor(),
//                contaDestino,
//                (cartaoOrigem instanceof CartaoCredito) ? TipoTransferencia.CARTAO_CREDITO : TipoTransferencia.CARTAO_DEBITO,
//                cartaoOrigem.getTipoCartao()
//        );
//        transferencia.setIdCartao(cartaoOrigem.getId());
//        transferencia.setIdClienteOrigem(clienteOrigem.getId());
//        transferencia.setIdClienteDestino(contaDestino.getCliente().getId());
//        transferencia.setIdContaOrigem(contaOrigem.getId());
//        transferencia.setIdContaDestino(contaDestino.getId());
//        transferencia.setData(LocalDateTime.now());
//        transferencia.setCodigoOperacao(gerarNumeros.gerarNumeroGeral());
//
//        if (contaOrigem.getTransferencias() == null) {
//            contaOrigem.setTransferencias(new ArrayList<>());
//        }
//
//        //se o cartão de origem for credito(não vai retirar valor do saldo), vai passar como credito e precisa entrar nas duas listas: transferenciasCredito (pq ele não ta tirando do dinheiro dele no
//        //momento, está pagando no credito e transferenciasEnviadas porque foi um tipo de transferencia enviada
//        //se for debito, precisa descontar do valor da conta e somente entrar em transferenciasEnviadas
//        if (cartaoOrigem instanceof CartaoCredito cartaoCredito) {
//            if (dto.valor().compareTo(cartaoCredito.getLimiteCreditoPreAprovado()) > 0) {
//                throw new TransferenciaNaoRealizadaException("Limite de crédito insuficiente");
//            }
//
//            BigDecimal novoLimite = cartaoCredito.getLimiteCreditoPreAprovado().subtract(dto.valor());
//            BigDecimal novoTotalGasto = cartaoCredito.getTotalGastoMesCredito() != null ?
//                    cartaoCredito.getTotalGastoMesCredito().add(dto.valor()) : dto.valor();
//            cartaoCredito.setLimiteCreditoPreAprovado(novoLimite);
//            cartaoCredito.setTotalGastoMesCredito(novoTotalGasto);
//            
//            Fatura fatura = faturaDao.findByCartaoId(cartaoCredito.getId())
//                    .orElseGet(() -> {
//                        Fatura novaFatura = new Fatura();
//                        novaFatura.setCartao(cartaoCredito);
//                        novaFatura.setDataVencimento(LocalDateTime.now().plusDays(10));
//                        novaFatura.setValorTotal(BigDecimal.ZERO);
//                        return novaFatura;
//                    });
//
//            BigDecimal atual = fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO;
//            fatura.setValorTotal(atual.add(dto.valor())); //precisei tratar antes com zero porque mesmo iniciando na entidade ainda estava dando null (também colquei no banco para não aceitar valor null"
//            //fatura.setValorTotal(fatura.getValorTotal().add(dto.valor()));
//            transferencia.setFatura(fatura);
//            fatura.adicionarTransfCredito(transferencia);
//                        
//            //teste inserindo na lista de transferencias e na lista de credito
//            contaOrigem.getTransferencias().add(transferencia);
//            fatura.getTransferenciasCredito().add(transferencia);
//            
//            fatura.setCartao(cartaoOrigem); // atribuindo o cartão a fatura
//            Long faturaId = faturaDao.save(fatura); //salvei a fatura e retornei o id
//            fatura.setId(faturaId);         
//            
//            //ligando a transferencia a fatura antes de salvar
//            transferencia.setFaturaId(faturaId);
//            
//            Long transferenciaId = transferenciaDao.save(transferencia); //salvo a transferencia e pego o id
//            transferencia.setId(transferenciaId);
//            transferenciaDao.associarTransferenciaAFatura(faturaId, transferenciaId); //associo a transferencia a fatura na tabela de ligação
//            cartaoDao.associarFaturaAoCartao(cartaoCredito.getId(), faturaId); //associei a fatura ao cartão
//            
//            cartaoCredito.setFaturaId(faturaId); //atualizando o objeto em memoria
//           
//            
//        } else if (cartaoOrigem instanceof CartaoDebito cartaoDebito) {
//            if (dto.valor().compareTo(cartaoDebito.getLimiteDiarioTransacao()) > 0) {
//                throw new TransferenciaNaoRealizadaException("Limite diário excedido");
//            }
//            if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
//                throw new TransferenciaNaoRealizadaException("Saldo insuficiente");
//            }
//
//            contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
//            cartaoDebito.setLimiteDiarioTransacao(cartaoDebito.getLimiteDiarioTransacao().subtract(dto.valor()));
//            BigDecimal novoTotalGastoMes = cartaoDebito.getTotalGastoMes() != null ?
//                    cartaoDebito.getTotalGastoMes().add(dto.valor()) : dto.valor();
//            cartaoDebito.setTotalGastoMes(novoTotalGastoMes);
//        }
//
//        contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
//        TaxaManutencao taxaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());
//        contaDestino.setCategoriaConta(taxaDestino.getCategoria());
//        contaDestino.setTaxas(List.of(taxaDestino));
//        
//        //teste somente na lista de transferencia
//        contaOrigem.getTransferencias().add(transferencia);
//       
//
//        Long transferenciaId = transferenciaDao.save(transferencia);
//        transferencia.setId(transferenciaId);
//        contaDao.update(contaOrigem);
//        contaDao.update(contaDestino);
//        cartaoDao.update(cartaoOrigem);
//
//        return true;
//    }

		
}
