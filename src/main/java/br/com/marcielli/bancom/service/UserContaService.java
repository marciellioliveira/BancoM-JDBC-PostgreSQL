package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import br.com.marcielli.bancom.dao.ClienteDao;
import br.com.marcielli.bancom.entity.*;
import br.com.marcielli.bancom.dao.ContaDao;
import br.com.marcielli.bancom.dao.TransferenciaDao;
import br.com.marcielli.bancom.dao.UserDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dto.security.ContaCreateDTO;
import br.com.marcielli.bancom.dto.security.ContaUpdateDTO;
import br.com.marcielli.bancom.dto.security.UserContaDepositoDTO;
import br.com.marcielli.bancom.dto.security.UserContaPixDTO;
import br.com.marcielli.bancom.dto.security.UserContaSaqueDTO;
import br.com.marcielli.bancom.dto.security.UserContaTedDTO;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
import br.com.marcielli.bancom.exception.AcessoNegadoException;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoTemSaldoSuficienteException;
import br.com.marcielli.bancom.exception.ContaExibirSaldoErroException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;
import br.com.marcielli.bancom.utils.GerarNumeros;

@Service
public class UserContaService {

	private final ContaDao contaDao;
	private final ExchangeRateService exchangeRateService;
	private final ClienteDao clienteDao;
	private final UserDao userDao;
	private final TransferenciaDao transferenciaDao;
	private final GerarNumeros gerarNumero;
	
	private Random random = new Random();

	private static final Logger logger = LoggerFactory.getLogger(UserClienteService.class);	

	public UserContaService(ContaDao contaDao,
							ExchangeRateService exchangeRateService, ClienteDao clienteDao, UserDao userDao,TransferenciaDao transferenciaDao, GerarNumeros gerarNumero) {
		this.contaDao = contaDao;
		this.exchangeRateService = exchangeRateService;
		this.clienteDao = clienteDao;
		this.userDao = userDao;
		this.transferenciaDao = transferenciaDao;
		this.gerarNumero = gerarNumero;
	}
	
	
	//ADMIN pode criar conta pra ele e pra todos
	//BASIC só pode criar conta pra ele mesmo
	@Transactional
	public Conta save(ContaCreateDTO dto, Authentication authentication) {
		
		//Pega a role do usuário logado
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");
	    
	    String username = authentication.getName();
	    
	    //Busca o usuário logado pelo username
	    User loggedInUser = userDao.findByUsername(username)
	            .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));
	    
	    // Se for BASIC e está tentando criar conta para outro usuário, bloqueia
	    if ("ROLE_BASIC".equals(role) && !dto.idUsuario().equals(loggedInUser.getId().longValue())) {
	        throw new ClienteNaoEncontradoException("Usuário BASIC não tem permissão para criar conta para outro usuário.");
	    }		
		
	    Cliente cliente = clienteDao.findById(dto.idUsuario())
				.orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
		
		if (!cliente.isClienteAtivo()) {
			throw new ClienteNaoEncontradoException("O cliente está desativado. Não é possível criar uma conta.");
		}

		TaxaManutencao taxa = new TaxaManutencao(dto.saldoConta(), dto.tipoConta());
		List<TaxaManutencao> taxas = new ArrayList<>();
		taxas.add(taxa);

		String numeroContaBase = gerarNumero.gerarNumeroGeral();
		String chavePix = gerarNumero.gerarNumeroGeral().concat("-PIX");

		Conta conta;

		if (dto.tipoConta() == TipoConta.CORRENTE) {
			ContaCorrente cc = new ContaCorrente(taxa.getTaxaManutencaoMensal());
			cc.setNumeroConta(numeroContaBase.concat("-CC"));
			cc.setTaxaManutencaoMensal(taxa.getTaxaManutencaoMensal());
			conta = cc;

		} else if (dto.tipoConta() == TipoConta.POUPANCA) {
			ContaPoupanca pp = new ContaPoupanca(taxa.getTaxaAcrescRend(), taxa.getTaxaMensal());
			pp.setNumeroConta(numeroContaBase.concat("-PP"));
			pp.setTaxaAcrescRend(taxa.getTaxaAcrescRend());
			pp.setTaxaMensal(taxa.getTaxaMensal());
			conta = pp;

		} else {
			throw new IllegalArgumentException("Tipo de conta inválido.");
		}

		conta.setCliente(cliente);
		conta.setPixAleatorio(chavePix);
		conta.setCategoriaConta(taxa.getCategoria());
		conta.setTipoConta(dto.tipoConta());
		conta.setSaldoConta(dto.saldoConta());
		conta.setStatus(true);
		conta.setTaxas(taxas);
		
		 // Salva a conta no banco 
	    Conta contaSalva = contaDao.save(conta);
	    
	 // Atualiza a lista em memória do cliente
	    if (cliente.getContas() == null) {
	        cliente.setContas(new ArrayList<>());
	    }
	    cliente.getContas().add(contaSalva);
	    
	    //preciso salvar na tabela cliente

	    return contaSalva;
	}
	
	@Transactional
	public List<Conta> getContas(Authentication authentication) {
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode ver todas as contas
	        return contaDao.findAll();
	    } else if ("ROLE_BASIC".equals(role)) {
	        String username = authentication.getName();
	        return contaDao.findByUsername(username); // Retorna todas as contas desse usuário
	    } else {
	        throw new RuntimeException("Você não tem permissão para acessar a lista de contas.");
	    }
	}


	@Transactional
	public Conta getContasById(Long id, Authentication authentication) {
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    String username = authentication.getName(); // Pegando o username do logado

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode acessar qualquer conta por ID
	        return contaDao.findById(id)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada."));
	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode acessar a conta dele mesmo
	        return contaDao.findByIdAndUsername(id, username)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada ou você não tem permissão para acessá-la."));
	    } else {
	        throw new RuntimeException("Você não tem permissão para acessar essa conta.");
	    }
	}
	
	@Transactional
	public boolean delete(Long idConta, Authentication authentication) {

	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    String username = authentication.getName();

	    Conta contaExistente;

	    if ("ROLE_ADMIN".equals(role)) {
	        contaExistente = contaDao.findById(idConta)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada."));

	        // Verificar se é a própria conta do admin
	        Long contaUserId = contaExistente.getCliente().getUser().getId().longValue();
	        User loggedInUser = userDao.findByUsername(username)
	            .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

	        if (contaUserId.equals(loggedInUser.getId())) {
	            throw new ContaExibirSaldoErroException("Administradores não podem deletar a própria conta. Apenas o superior pode realizar essa ação.");
	        }

	    } else if ("ROLE_BASIC".equals(role)) {
	        contaExistente = contaDao.findByIdAndUsername(idConta, username)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada ou você não tem permissão para deletá-la."));
	    } else {
	        throw new ClienteEncontradoException("Role não autorizada para deletar contas.");
	    }

	    // REGRAS DE SALDO
	    if (contaExistente.getSaldoConta().compareTo(BigDecimal.ZERO) > 0) {
	        throw new ContaExibirSaldoErroException("A conta possui um saldo de R$ " + contaExistente.getSaldoConta() + ". Faça o saque antes de remover a conta.");
	    } else if (contaExistente.getSaldoConta().compareTo(BigDecimal.ZERO) < 0) {
	        throw new ContaExibirSaldoErroException("A conta está com saldo negativo. Regularize antes de remover a conta.");
	    }

	    if (!contaExistente.getStatus()) {
	        throw new ContaExibirSaldoErroException("A conta já está desativada anteriormente.");
	    }

	    contaExistente.setStatus(false);
	    contaDao.save(contaExistente);
	    return true;
	}

	@Transactional
	public Conta update(Long idConta, ContaUpdateDTO dto, Authentication authentication) {
	   
	    Conta conta = contaDao.findById(idConta)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
	    
	    //Verifica se é admin
	    boolean isAdmin = authentication.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
	    
	    //Se não for admin, verifica se a conta pertence ao usuário logado
	    if (!isAdmin) {
	        User usuarioLogado = userDao.findByUsername(authentication.getName())
	                .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));
	        
	        if (conta.getCliente() == null || conta.getCliente().getUser() == null) {
	            throw new AcessoNegadoException("Conta não vinculada a um usuário válido");
	        }
	        
	        if (!conta.getCliente().getUser().getId().equals(usuarioLogado.getId())) {
	            throw new AcessoNegadoException("Você só pode alterar sua própria conta");
	        }
	    }

	    String novoPix = dto.pixAleatorio().concat("-PIX");
	    contaDao.atualizarPixAleatorio(idConta, novoPix);
	    
	    return contaDao.findById(idConta)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada após atualização"));
	}
	


	// Transferências

	@Transactional
    public boolean transferirTED(Long idContaReceber, UserContaTedDTO dto, Authentication authentication) {
       
        // Validar DTO
        if (dto.idContaOrigem() == null) {
            throw new IllegalArgumentException("ID da conta origem é obrigatório");
        }
        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser maior que zero");
        }

        // Buscar contas
        Conta contaOrigem = contaDao.findById(dto.idContaOrigem())
                .orElseThrow(() -> new ContaNaoEncontradaException("Conta origem não encontrada"));
        
        Conta contaDestino = contaDao.findById(idContaReceber)
                .orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));

        // Validar status das contas
        if (!contaOrigem.getStatus() || !contaDestino.getStatus()) {
            throw new ContaExibirSaldoErroException("Contas devem estar ativas para transferência");
        }

        // Validar saldo
        if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
            throw new ContaExibirSaldoErroException("Saldo insuficiente para transferência");
        }

        // Validar cliente associado
        if (contaOrigem.getCliente() == null) {
            System.out.println("Falha: Conta origem não vinculada a um cliente");
            throw new AcessoNegadoException("Conta origem não vinculada a um cliente");
        }
        if (contaDestino.getCliente() == null) {
            System.out.println("Falha: Conta destino não vinculada a um cliente");
            throw new AcessoNegadoException("Conta destino não vinculada a um cliente");
        }

        // Buscar usuário logado
        User usuarioLogado = userDao.findByUsername(authentication.getName())
                .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));

        // Verificar permissões
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ADMIN"));

        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("isAdmin: " + isAdmin);

        if (isAdmin) {
            // ADMIN: Validar que a conta origem pertence ao idUsuario do DTO
            if (dto.idUsuario() == null) {
                throw new IllegalArgumentException("ID do usuário é obrigatório para transferências de admin");
            }
            if (!contaOrigem.getCliente().getId().equals(dto.idUsuario())) {
                System.out.println("Falha: Conta origem Cliente ID " + 
                                   contaOrigem.getCliente().getId() + 
                                   " != idUsuario do DTO " + dto.idUsuario());
                throw new AcessoNegadoException("Conta origem não pertence ao cliente especificado");
            }
        } else {
            // BASIC: Validar que a conta origem pertence ao usuário logado
            if (!contaOrigem.getCliente().getId().equals(usuarioLogado.getId().longValue())) {
                System.out.println("Falha: Conta origem Cliente ID " + 
                                   contaOrigem.getCliente().getId() + 
                                   " != Usuário logado ID " + usuarioLogado.getId());
                throw new AcessoNegadoException("Você só pode transferir da sua própria conta");
            }
        }

        // Processar transferência (atualizar saldos)
        contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
        contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));

        // Aplicar taxas na conta origem
        TaxaManutencao taxaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());
        contaOrigem.setCategoriaConta(taxaOrigem.getCategoria());
        if (contaOrigem instanceof ContaCorrente cc) {
            cc.setTaxaManutencaoMensal(taxaOrigem.getTaxaManutencaoMensal());
        } else if (contaOrigem instanceof ContaPoupanca cp) {
            cp.setTaxaAcrescRend(taxaOrigem.getTaxaAcrescRend());
            cp.setTaxaMensal(taxaOrigem.getTaxaMensal());
        }

        // Aplicar taxas na conta destino
        TaxaManutencao taxaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());
        contaDestino.setCategoriaConta(taxaDestino.getCategoria());
        if (contaDestino instanceof ContaCorrente cc) {
            cc.setTaxaManutencaoMensal(taxaDestino.getTaxaManutencaoMensal());
        } else if (contaDestino instanceof ContaPoupanca cp) {
            cp.setTaxaAcrescRend(taxaDestino.getTaxaAcrescRend());
            cp.setTaxaMensal(taxaDestino.getTaxaMensal());
        }

        // Registrar transferência
        Transferencia transferencia = new Transferencia(contaOrigem, dto.valor(), contaDestino, TipoTransferencia.TED);
        transferenciaDao.save(transferencia);

        contaDao.updateSaldo(contaOrigem);
        contaDao.updateSaldo(contaDestino);

        System.out.println("Transferência TED concluída com sucesso");
        return true;
    }
	
	public Map<String, BigDecimal> exibirSaldoConvertido(Long contaId, Authentication authentication) {
	    //Verificar autenticação primeiro
	    User usuarioLogado = userDao.findByUsername(authentication.getName())
	            .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));

	    //Buscar a conta
	    Conta conta = contaDao.findById(contaId)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));

	    //Verificar permissões
	    boolean isAdmin = authentication.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

	    //Validação de acesso
	    if (!isAdmin) {
	        if (conta.getCliente() == null || !conta.getCliente().getId().equals(usuarioLogado.getId())) {
	            throw new AcessoNegadoException("Você só pode visualizar o saldo da sua própria conta");
	        }
	    }

	    //Validar status da conta
	    if (!conta.getStatus()) {
	        throw new ContaExibirSaldoErroException("Conta inativa");
	    }

	    //Processar conversões
	    BigDecimal saldoBRL = conta.getSaldoConta();
	    
	    Map<String, BigDecimal> resultado = new LinkedHashMap<>();
	    resultado.put("BRL", saldoBRL);
	    resultado.put("USD", exchangeRateService.converterMoeda(saldoBRL, "BRL", "USD"));
	    resultado.put("EUR", exchangeRateService.converterMoeda(saldoBRL, "BRL", "EUR"));

	    return resultado;
	}
	
	
	@Transactional
    public boolean transferirPIX(String chaveOuIdDestino, UserContaPixDTO dto, Authentication authentication) {
       
		// Busca conta origem
		Conta contaOrigem = contaDao.findById(dto.idContaOrigem())
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta origem não encontrada"));

        // Busca conta destino por chave pix ou id (no projeto anterior era somente id)
		Conta contaDestino = contaDao.findByChavePix(chaveOuIdDestino);

		if (!contaOrigem.getStatus() || !contaDestino.getStatus()) {
	        throw new ContaExibirSaldoErroException("Contas devem estar ativas");
	    }

	    if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
	        throw new ClienteNaoTemSaldoSuficienteException("Saldo insuficiente");
	    }
	    
	    // Buscar usuário logado
	    User usuarioLogado = userDao.findByUsername(authentication.getName())
	            .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));
	    
	    // Verificar permissões
	    boolean isAdmin = authentication.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ADMIN"));
	    
	    if (isAdmin) {
	        // ADMIN: Validar que a conta origem pertence ao idUsuario do DTO
	        if (dto.idUsuario() == null) {
	            throw new IllegalArgumentException("ID do usuário é obrigatório para transferências de admin");
	        }
	        if (!contaOrigem.getCliente().getId().equals(dto.idUsuario())) {
	            throw new AcessoNegadoException("Conta origem não pertence ao cliente especificado");
	        }
	    } else {
	        // BASIC: Validar que a conta origem pertence ao usuário logado
	        if (!contaOrigem.getCliente().getId().equals(usuarioLogado.getId().longValue())) {
	            throw new AcessoNegadoException("Você só pode transferir da sua própria conta");
	        }
	    }

        // Atualiza saldos
	    contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
	    contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
        
        // Atualiza taxas
	    atualizarTaxas(contaOrigem);
	    atualizarTaxas(contaDestino);
	    
	    // Cria a transferência somentepara origem
	    Transferencia transferencia = new Transferencia();
	    transferencia.setIdClienteOrigem(contaOrigem.getCliente().getId());
	    transferencia.setIdClienteDestino(contaDestino.getCliente().getId());
	    transferencia.setIdContaOrigem(contaOrigem.getId());
	    transferencia.setIdContaDestino(contaDestino.getId());
	    transferencia.setTipoTransferencia(TipoTransferencia.PIX);
	    transferencia.setValor(dto.valor());
	    transferencia.setData(LocalDateTime.now());
	    transferencia.setCodigoOperacao(gerarNumero.gerarNumeroGeral());
	    transferencia.setTipoCartao(TipoCartao.SEM_CARTAO);
	    
	    transferencia.setConta(contaOrigem);
	    
	    contaOrigem.getTransferencias().add(transferencia);
        
	    transferenciaDao.save(transferencia);
	    contaDao.updateSaldo(contaOrigem);
	    contaDao.updateSaldo(contaDestino);

        return true;
    }
	
	private void atualizarTaxas(Conta conta) {
	    TaxaManutencao taxa = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
	    
	    List<TaxaManutencao> novaTaxa = new ArrayList<>();
	    novaTaxa.add(taxa);
	    conta.setTaxas(novaTaxa);
	    conta.setCategoriaConta(taxa.getCategoria());

	    if (conta instanceof ContaCorrente cc) {
	        cc.setTaxaManutencaoMensal(taxa.getTaxaManutencaoMensal());
	    } else if (conta instanceof ContaPoupanca cp) {
	        cp.setTaxaAcrescRend(taxa.getTaxaAcrescRend());
	        cp.setTaxaMensal(taxa.getTaxaMensal());
	    }
	}
	
	
	@Transactional
	public boolean transferirDEPOSITO(Long idContaReceber, UserContaDepositoDTO dto, Authentication authentication) {

		 // Busca conta destino 
	    Conta contaDestino = contaDao.findById(idContaReceber)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));

	    if (!contaDestino.getStatus()) {
	        throw new ContaExibirSaldoErroException("Conta deve estar ativa");
	    }

	    // Busca usuário logado
	    User usuarioLogado = userDao.findByUsername(authentication.getName())
	            .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));

	    // Verificar permissões
	    boolean isAdmin = authentication.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ADMIN"));
	    
	    if (isAdmin) {
	        // ADMIN: Verifica se a conta destino pertence ao idUsuario do DTO (Admin pode depositar em qualquer conta)
	        if (dto.idUsuario() == null) {
	            throw new IllegalArgumentException("ID do usuário é obrigatório para depósitos de admin");
	        }
	        if (!contaDestino.getCliente().getId().equals(dto.idUsuario())) {
	            throw new AcessoNegadoException("Conta destino não pertence ao cliente especificado");
	        }
	    } else {
	        // BASIC: Verifica se a conta destino é a própria conta do usuário logado
	        if (!contaDestino.getCliente().getId().equals(usuarioLogado.getId().longValue())) {
	            throw new AcessoNegadoException("Você só pode depositar na sua própria conta");
	        }
	    }

	    // Atualiza saldo
	    contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));

	    // Atualiza taxas
	    atualizarTaxas(contaDestino);

	    // Criação da transferência
	    Transferencia transferencia = new Transferencia();
	    transferencia.setIdClienteOrigem(contaDestino.getCliente().getId());
	    transferencia.setIdClienteDestino(contaDestino.getCliente().getId());
	    transferencia.setIdContaOrigem(contaDestino.getId());
	    transferencia.setIdContaDestino(contaDestino.getId());
	    transferencia.setTipoTransferencia(TipoTransferencia.DEPOSITO);
	    transferencia.setValor(dto.valor());
	    transferencia.setData(LocalDateTime.now());
	    transferencia.setCodigoOperacao(gerarNumero.gerarNumeroGeral());
	    transferencia.setTipoCartao(TipoCartao.SEM_CARTAO);

	    transferencia.setConta(contaDestino);

	    contaDestino.getTransferencias().add(transferencia);

	    transferenciaDao.save(transferencia);
	    contaDao.updateSaldo(contaDestino);

	    return true;
	}
	
	@Transactional
	public boolean transferirSAQUE(Long idContaReceber, UserContaSaqueDTO dto, Authentication authentication) {

		// Busca conta destino
	    Conta conta = contaDao.findById(idContaReceber)
	            .orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));

	    if (!conta.getStatus()) {
	        throw new ContaExibirSaldoErroException("Não é possível realizar operações de contas desativadas");
	    }

	    if (conta.getSaldoConta().compareTo(dto.valor()) < 0 || conta.getSaldoConta().compareTo(BigDecimal.ZERO) == 0) {
	        throw new ContaExibirSaldoErroException("Saldo insuficiente.");
	    }

	    // Busca usuário logado
	    User usuarioLogado = userDao.findByUsername(authentication.getName())
	            .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));

	    // Verificar permissões
	    boolean isAdmin = authentication.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ADMIN"));

	    if (isAdmin) {
	        // ADMIN: Verifica se a conta de saque pertence ao idUsuario do DTO (Admin pode sacar de qualquer conta)
	        if (dto.idUsuario() == null) {
	            throw new IllegalArgumentException("ID do usuário é obrigatório para saques de admin");
	        }
	        if (!conta.getCliente().getId().equals(dto.idUsuario())) {
	            throw new AcessoNegadoException("Conta destino não pertence ao cliente especificado");
	        }
	    } else {
	        // BASIC: Verifica se a conta de saque é a própria conta do usuário logado
	        if (!conta.getCliente().getId().equals(usuarioLogado.getId().longValue())) {
	            throw new AcessoNegadoException("Você só pode sacar da sua própria conta");
	        }
	    }

	    // Atualiza saldo 
	    conta.setSaldoConta(conta.getSaldoConta().subtract(dto.valor()));

	    // Atualiza taxas
	    TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());

	    List<TaxaManutencao> novaTaxa = new ArrayList<>();
	    novaTaxa.add(taxaContaOrigem);

	    conta.setTaxas(novaTaxa);
	    conta.setCategoriaConta(taxaContaOrigem.getCategoria());

	    Transferencia transferencia = new Transferencia();
	    transferencia.setIdClienteOrigem(conta.getCliente().getId());  
	    transferencia.setIdClienteDestino(conta.getCliente().getId()); 
	    transferencia.setIdContaOrigem(conta.getId());  
	    transferencia.setIdContaDestino(conta.getId());  
	    transferencia.setTipoTransferencia(TipoTransferencia.SAQUE);  
	    transferencia.setValor(dto.valor());
	    transferencia.setData(LocalDateTime.now());
	    transferencia.setCodigoOperacao(gerarNumero.gerarNumeroGeral()); 
	    transferencia.setTipoCartao(TipoCartao.SEM_CARTAO); 

	    transferencia.setConta(conta);
	    conta.getTransferencias().add(transferencia);  

	    transferenciaDao.save(transferencia);
	    contaDao.updateSaldo(conta);

	    return true;
	}

	// ROTAS MANUAIS
	@Transactional
	public boolean manutencaoTaxaCC(Long idConta, Authentication authentication) {
	    
	    if (!authentication.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
	                             auth.getAuthority().equals("ADMIN"))) {
	        throw new AcessoNegadoException("Apenas administradores podem executar esta operação");
	    }

	    ContaCorrente cc = contaDao.findContaCorrenteById(idConta)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta corrente não encontrada"));

	    if (!cc.getStatus()) {
	        throw new ContaExibirSaldoErroException("Não é possível operar com contas desativadas");
	    }
	    if (cc.getSaldoConta() == null || cc.getTaxaManutencaoMensal() == null) {
	        throw new ContaExibirSaldoErroException("Saldo ou taxa não configurados corretamente");
	    }
	    if (cc.getSaldoConta().compareTo(cc.getTaxaManutencaoMensal()) < 0) {
	        throw new ContaExibirSaldoErroException("Saldo insuficiente para cobrança da taxa");
	    }

	    BigDecimal novoSaldo = cc.getSaldoConta().subtract(cc.getTaxaManutencaoMensal());
	    cc.setSaldoConta(novoSaldo);
	    cc.setCategoriaConta(new TaxaManutencao(novoSaldo, cc.getTipoConta()).getCategoria());
	    contaDao.updateContaCorrente(cc);
	    
	    return true;
	}
	
	@Transactional
	public boolean rendimentoTaxaCP(Long idConta, Authentication authentication) {
	   
	    if (!authentication.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
	                             auth.getAuthority().equals("ADMIN"))) {
	        throw new AcessoNegadoException("Apenas administradores podem executar esta operação");
	    }

	    ContaPoupanca cp = contaDao.findContaPoupancaById(idConta)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta poupança não encontrada."));

	    if(!cp.getStatus()) {
	        throw new ContaExibirSaldoErroException("Não é possível realizar operações de contas desativadas");
	    }

	    if (cp.getSaldoConta() == null) {
	        throw new ContaNaoEncontradaException("Saldo da conta está indefinido.");
	    }

	    if (cp.getTaxaAcrescRend() == null) {
	        throw new ContaExibirSaldoErroException("Taxa de rendimento não configurada para esta conta");
	    }

	    BigDecimal rendimento = cp.getSaldoConta().multiply(cp.getTaxaAcrescRend());
	    BigDecimal novoSaldo = cp.getSaldoConta().add(rendimento);
	    
	    cp.setSaldoConta(novoSaldo);
	    
	    TaxaManutencao novaTaxa = new TaxaManutencao(novoSaldo, cp.getTipoConta());
	    cp.setCategoriaConta(novaTaxa.getCategoria());
	    
	    contaDao.updateSaldo(cp);
	    
	    return true;
	}
	

	// Outros métodos
//	public String gerarNumeroDaConta() {
//		int[] sequencia = new int[8];
//		StringBuilder minhaConta = new StringBuilder();
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//			minhaConta.append(sequencia[i]);
//		}
//
//		return minhaConta.toString();
//	}
//
//	public String gerarPixAleatorio() {
//		int[] sequencia = new int[8];
//		StringBuilder meuPix = new StringBuilder();
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//			meuPix.append(sequencia[i]);
//		}
//
//		return meuPix.toString();
//	}
//
//	public String gerarCodigoTransferencia() {
//		int[] sequencia = new int[8];
//		StringBuilder codTransferencia = new StringBuilder();
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//			codTransferencia.append(sequencia[i]);
//		}
//
//		return codTransferencia.toString();
//	}

}
