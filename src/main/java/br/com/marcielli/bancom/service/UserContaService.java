package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
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
import br.com.marcielli.bancom.dto.security.UserContaPixDTO;
import br.com.marcielli.bancom.dto.security.UserContaTedDTO;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
import br.com.marcielli.bancom.exception.AcessoNegadoException;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoTemSaldoSuficienteException;
import br.com.marcielli.bancom.exception.ContaExibirSaldoErroException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;

@Service
public class UserContaService {

	private final ContaDao contaDao;
	private final ExchangeRateService exchangeRateService;
	private final ClienteDao clienteDao;
	private final UserDao userDao;
	private final TransferenciaDao transferenciaDao;
	
	private Random random = new Random();

	private static final Logger logger = LoggerFactory.getLogger(UserClienteService.class);	

	public UserContaService(ContaDao contaDao,
							ExchangeRateService exchangeRateService, ClienteDao clienteDao, UserDao userDao,TransferenciaDao transferenciaDao) {
		this.contaDao = contaDao;
		this.exchangeRateService = exchangeRateService;
		this.clienteDao = clienteDao;
		this.userDao = userDao;
		this.transferenciaDao = transferenciaDao;
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

		String numeroContaBase = gerarNumeroDaConta();
		String chavePix = gerarPixAleatorio().concat("-PIX");

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
	    //Busca a conta
	    Conta conta = contaDao.findById(idConta)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
	    
	    //Verifica se é admin
	    boolean isAdmin = authentication.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
	    
	    //Se não for admin, verifica se a conta pertence ao usuário logado
	    if (!isAdmin) {
	        // Obtém usuário logado
	        User usuarioLogado = userDao.findByUsername(authentication.getName())
	                .orElseThrow(() -> new AcessoNegadoException("Usuário não autenticado"));
	        
	        // Verifica se o cliente tem usuário vinculado
	        if (conta.getCliente() == null || conta.getCliente().getUser() == null) {
	            throw new AcessoNegadoException("Conta não vinculada a um usuário válido");
	        }
	        
	        // Verifica se o usuário logado é dono da conta
	        if (!conta.getCliente().getUser().getId().equals(usuarioLogado.getId())) {
	            throw new AcessoNegadoException("Você só pode alterar sua própria conta");
	        }
	    }

	    //Atualiza o PIX
	    String novoPix = dto.pixAleatorio().concat("-PIX");
	    contaDao.atualizarPixAleatorio(idConta, novoPix);
	    
	    //Retorna a conta atualizada
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

        // Busca conta destino por chave PIX ou ID
        Conta contaDestino = contaDao.findByChavePix(chaveOuIdDestino);

        if (!contaOrigem.getStatus() || !contaDestino.getStatus()) {
            throw new ContaExibirSaldoErroException("Contas devem estar ativas");
        }

        if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
            throw new ClienteNaoTemSaldoSuficienteException("Saldo insuficiente");
        }

        // Atualiza saldos
        contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
        contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
        
        // Atualiza taxas
        atualizarTaxas(contaOrigem);
        atualizarTaxas(contaDestino);
        
        contaDao.updateSaldo(contaOrigem);
        contaDao.updateSaldo(contaDestino);
        
        // Cria e salva a transferência
        Transferencia transferencia = new Transferencia(contaOrigem, dto.valor(), contaDestino, TipoTransferencia.PIX, chaveOuIdDestino);
        transferenciaDao.save(transferencia);

        return true;
    }
	
	private void atualizarTaxas(Conta conta) {
	    TaxaManutencao taxa = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
	    
	    if (conta instanceof ContaCorrente cc) {
	        cc.setTaxaManutencaoMensal(taxa.getTaxaManutencaoMensal());
	    } else if (conta instanceof ContaPoupanca cp) {
	        cp.setTaxaAcrescRend(taxa.getTaxaAcrescRend());
	        cp.setTaxaMensal(taxa.getTaxaMensal());
	    }
	    
	    conta.setCategoriaConta(taxa.getCategoria());
	}
	
//	private Conta buscarContaPorChavePix(String chave) {
//	    // Primeiro tenta buscar como id como era no projeto anterior, se não achar busca pela chave
//	    try {
//	        Long id = Long.parseLong(chave);
//	        return contaDao.findById(id)
//	                .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
//	    } catch (NumberFormatException e) {
//	        // Se não for número, busca como chave PIX
//	        ChavePix chavePix = chavePixDao.findByValorChave(chave)
//	                .orElseThrow(() -> new ChavePixNaoEncontradaException("Chave PIX não encontrada"));
//	        
//	        return chavePix.getConta();
//	    }
//	}

//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirPIX(Long idContaReceber, UserContaPixDTO dto) {
//
//		Conta contaOrigem = contaRepository.findById(dto.idContaOrigem())
//				.orElseThrow(() -> new ContaNaoEncontradaException("A conta origem não existe."));
//
//		Conta contaDestino = contaRepository.findById(idContaReceber)
//				.orElseThrow(() -> new ContaNaoEncontradaException("A conta destino não existe."));
//
//		if(contaOrigem.getStatus() == false || contaDestino.getStatus() == false) {
//			throw new ContaExibirSaldoErroException("Não é possível realizar operações de contas desativadas");
//		}
//
//		contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
//
//		TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());
//
//		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
//		novaTaxa.add(taxaContaOrigem);
//
//		contaOrigem.setTaxas(novaTaxa);
//		contaOrigem.setCategoriaConta(taxaContaOrigem.getCategoria());
//
//		Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino, TipoTransferencia.PIX);
//		contaOrigem.getTransferencia().add(transferindo);
//
//		if (contaOrigem instanceof ContaCorrente cc) {
//			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
//		}
//
//		if (contaOrigem instanceof ContaPoupanca cp) {
//			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
//			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
//		}
//
//		contaRepository.save(contaOrigem);
//
//		contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
//
//		TaxaManutencao taxaContaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());
//
//		List<TaxaManutencao> novaTaxa2 = new ArrayList<TaxaManutencao>();
//		novaTaxa2.add(taxaContaDestino);
//
//		contaDestino.setTaxas(novaTaxa2);
//		contaDestino.setCategoriaConta(taxaContaDestino.getCategoria());
//
//		if (contaDestino instanceof ContaCorrente cc) {
//			cc.setTaxaManutencaoMensal(taxaContaDestino.getTaxaManutencaoMensal());
//		}
//
//		if (contaDestino instanceof ContaPoupanca cp) {
//			cp.setTaxaAcrescRend(taxaContaDestino.getTaxaAcrescRend());
//			cp.setTaxaMensal(taxaContaDestino.getTaxaMensal());
//		}
//
//		contaRepository.save(contaDestino);
//
//		return true;
//	}

//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirDEPOSITO(Long idContaReceber, UserContaDepositoDTO dto) {
//
//		Conta conta = contaRepository.findById(idContaReceber)
//				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));
//
//		Cliente cliente = clienteRepository.findById(dto.idUsuario())
//				.orElseThrow(() -> new ContaNaoEncontradaException("O cliente não existe."));
//
//		if(conta.getStatus() == false) {
//			throw new ContaExibirSaldoErroException("Não é possível realizar operações de contas desativadas");
//		}
//
//		if(!cliente.getContas().contains(conta)) {
//			throw new ContaExibirSaldoErroException("A conta não é do cliente informado.");
//		}
//
//		conta.setSaldoConta(conta.getSaldoConta().add(dto.valor()));
//		System.err.println(conta);
//
//		TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
//
//		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
//		novaTaxa.add(taxaContaOrigem);
//
//		conta.setTaxas(novaTaxa);
//		conta.setCategoriaConta(taxaContaOrigem.getCategoria());
//
//		Transferencia transferindo = new Transferencia(conta, dto.valor(), conta, TipoTransferencia.DEPOSITO);
//		conta.getTransferencia().add(transferindo);
//
//		if (conta instanceof ContaCorrente cc) {
//			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
//		}
//
//		if (conta instanceof ContaPoupanca cp) {
//			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
//			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
//		}
//
//		contaRepository.save(conta);
//
//		return true;
//	}

//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirSAQUE(Long idContaReceber, UserContaSaqueDTO dto) {
//
//		Conta conta = contaRepository.findById(idContaReceber)
//				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));
//
//		Cliente cliente = clienteRepository.findById(dto.idUsuario())
//				.orElseThrow(() -> new ContaNaoEncontradaException("O cliente não existe."));
//
//		if(conta.getStatus() == false) {
//			throw new ContaExibirSaldoErroException("Não é possível realizar operações de contas desativadas");
//		}
//
//		if(!cliente.getContas().contains(conta)) {
//			throw new ContaExibirSaldoErroException("A conta não é do cliente informado.");
//		}
//
//		if (conta.getSaldoConta().compareTo(dto.valor()) < 0 || conta.getSaldoConta().compareTo(BigDecimal.ZERO) == 0) {
//			throw new ContaExibirSaldoErroException("Saldo insuficiente.");
//		}
//
//
//		conta.setSaldoConta(conta.getSaldoConta().subtract(dto.valor()));
//
//		TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
//
//		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
//		novaTaxa.add(taxaContaOrigem);
//
//		conta.setTaxas(novaTaxa);
//		conta.setCategoriaConta(taxaContaOrigem.getCategoria());
//
//		Transferencia transferindo = new Transferencia(conta, dto.valor(), conta, TipoTransferencia.SAQUE);
//		conta.getTransferencia().add(transferindo);
//
//		if (conta instanceof ContaCorrente cc) {
//			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
//		}
//
//		if (conta instanceof ContaPoupanca cp) {
//			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
//			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
//		}
//
//		contaRepository.save(conta);
//
//		return true;
//	}

	// ROTAS MANUAIS
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Conta manutencaoTaxaCC(Long idConta) {
//		Conta conta = contaRepository.findById(idConta)
//				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada."));
//
//		if(conta.getStatus() == false) {
//			throw new ContaExibirSaldoErroException("Não é possível realizar operações de contas desativadas");
//		}
//
//		if (conta.getSaldoConta() == null) {
//			throw new ContaNaoEncontradaException("Saldo da conta está indefinido.");
//		}
//
//		if (!(conta instanceof ContaCorrente)) {
//			throw new ContaNaoEncontradaException("Taxa de manutenção só pode ser aplicada a contas correntes.");
//		}
//
//		ContaCorrente cc = (ContaCorrente) conta;
//		BigDecimal taxa = cc.getTaxaManutencaoMensal();
//
//		if (conta.getSaldoConta().compareTo(taxa) < 0) {
//			throw new ContaExibirSaldoErroException("Saldo insuficiente para cobrança da taxa de manutenção");
//		}
//
//		conta.setSaldoConta(conta.getSaldoConta().subtract(taxa));
//
//		TaxaManutencao novaTaxa = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
//		conta.setCategoriaConta(novaTaxa.getCategoria());
//		conta.getTaxas().add(novaTaxa);
//
//		return contaRepository.save(conta);
//	}

//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Conta rendimentoTaxaCP(Long idConta) {
//		Conta conta = contaRepository.findById(idConta)
//				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada."));
//
//		if(conta.getStatus() == false) {
//			throw new ContaExibirSaldoErroException("Não é possível realizar operações de contas desativadas");
//		}
//
//		if (conta.getSaldoConta() == null) {
//			throw new ContaNaoEncontradaException("Saldo da conta está indefinido.");
//		}
//
//		if (!(conta instanceof ContaPoupanca)) {
//			throw new ContaNaoEncontradaException("Rendimentos só podem ser aplicados a contas poupança.");
//		}
//
//		ContaPoupanca cp = (ContaPoupanca) conta;
//
//		BigDecimal rendimento = conta.getSaldoConta().multiply(cp.getTaxaAcrescRend());
//
//		conta.setSaldoConta(conta.getSaldoConta().add(rendimento));
//
//		TaxaManutencao novaTaxa = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
//		conta.setCategoriaConta(novaTaxa.getCategoria());
//		conta.getTaxas().add(novaTaxa);
//
//		return contaRepository.save(conta);
//	}




	// Outros métodos
	public String gerarNumeroDaConta() {
		int[] sequencia = new int[8];
		StringBuilder minhaConta = new StringBuilder();

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
			minhaConta.append(sequencia[i]);
		}

		return minhaConta.toString();
	}

	public String gerarPixAleatorio() {
		int[] sequencia = new int[8];
		StringBuilder meuPix = new StringBuilder();

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
			meuPix.append(sequencia[i]);
		}

		return meuPix.toString();
	}


}
