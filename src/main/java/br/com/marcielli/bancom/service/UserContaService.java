package br.com.marcielli.bancom.service;

import java.util.*;

import br.com.marcielli.bancom.dao.ClienteDao;
import br.com.marcielli.bancom.entity.*;
import br.com.marcielli.bancom.dao.ContaDao;
import br.com.marcielli.bancom.dao.UserDao;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dto.security.ContaCreateDTO;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;

@Service
public class UserContaService {

	private final ContaDao contaDao;
	private final ExchangeRateService exchangeRateService;
	private final ClienteDao clienteDao;
	private final UserDao userDao;
	
	private Random random = new Random();

	//private static final Logger log = LoggerFactory.getLogger(UserContaService.class);

	public UserContaService(ContaDao contaDao,
							ExchangeRateService exchangeRateService, ClienteDao clienteDao, UserDao userDao) {
		this.contaDao = contaDao;
		this.exchangeRateService = exchangeRateService;
		this.clienteDao = clienteDao;
		this.userDao = userDao;
	}

	//ADMIN pode criar conta pra ele e pra todos
	//BASIC só pode criar conta pra ele mesmo
	@Transactional(propagation = Propagation.REQUIRES_NEW)
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

		return contaDao.save(conta);
	}




//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public List<Conta> getContas() {
//		return contaRepository.findAll();
//	}


//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Conta getContasById(Long id) {
//		return contaRepository.findById(id).orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
//	}

//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Conta update(Long idConta, ContaUpdateDTO dto) { //, JwtAuthenticationToken token
//
//		Conta contaExistente = contaRepository.findById(idConta)
//				.orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada"));
//
//		Long userId = contaExistente.getCliente().getUser().getId().longValue();
//
//		if(userId != dto.idUsuario()) {
//			throw new ClienteNaoEncontradoException("Você não tem permissão para alterar essa conta.");
//		}
//
//		if (!contaExistente.getStatus()) { // A conta está ativa? Porque no banco eu prefiro não deletar e somente
//											// desativar
//			throw new ClienteNaoEncontradoException("Não é possível atualizar uma conta desativada");
//		}
//
//		String novoPix = dto.pixAleatorio().concat("-PIX");
//		contaExistente.setPixAleatorio(novoPix);
//
//		//Aqui tive que forçar o flush imediat porque ele estava salvando corretamente no banco mas não estava imprimindo corretamente no json
//		Conta contaAtualizada = contaRepository.saveAndFlush(contaExistente);
//
//		return contaRepository.save(contaAtualizada);
//	}

//	@Transactional
//	public boolean delete(Long idConta, ContaUpdateDTO dto) {
//
//		Conta contaExistente = contaRepository.findById(idConta)
//				.orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada"));
//
//		if(contaExistente.getSaldoConta().compareTo(BigDecimal.ZERO) > 0) {
//			throw new ContaExibirSaldoErroException("A conta possui um saldo de R$ "+contaExistente.getSaldoConta()+". Faça o saque antes de remover a conta.");
//		}
//
//		Long userId = contaExistente.getCliente().getUser().getId().longValue();
//
//		if(userId != dto.idUsuario()) {
//			throw new ClienteNaoEncontradoException("Você não tem permissão para deletar essa conta.");
//		}
//
//		if(contaExistente.getStatus() == false) {
//			throw new ContaExibirSaldoErroException("A conta já está desativada anteriomente.");
//		}
//
//		contaExistente.setStatus(false);
//		contaRepository.save(contaExistente);
//		return true;
//	}


//	// Transferências
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirTED(Long idContaReceber, UserContaTedDTO dto) {
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
//		// Verificar se o saldo da conta origem é suficiente para a transferência
//		if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
//			throw new ContaExibirSaldoErroException("Saldo insuficiente na conta origem.");
//		}
//
//		List<Conta> contasTransferidas = new ArrayList<Conta>();
//
//		contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
//		contasTransferidas.add(contaOrigem);
//
//		TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());
//
//		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
//		novaTaxa.add(taxaContaOrigem);
//
//		contaOrigem.setTaxas(novaTaxa);
//		contaOrigem.setCategoriaConta(taxaContaOrigem.getCategoria());
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
//		Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino, TipoTransferencia.TED);
//		contaOrigem.getTransferencia().add(transferindo);
//
//		contaRepository.save(contaOrigem);
//
//		contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
//		contasTransferidas.add(contaDestino);
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
//	public Map<String, BigDecimal> exibirSaldoConvertido(Long contaId) {
//		Conta contaSaldo = contaRepository.findById(contaId)
//				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));
//
//		if(contaSaldo.getStatus() == false) {
//			throw new ContaExibirSaldoErroException("Não é possível realizar operações de contas desativadas");
//		}
//
//		BigDecimal saldo = contaSaldo.getSaldoConta();
//
//		Map<String, BigDecimal> saldosConvertidos = new LinkedHashMap<>();
//		saldosConvertidos.put("Saldo em Real", saldo);
//
//		try {
//			ConversionResponseDTO saldoUSD = exchangeRateService.convertAmount(saldo, "BRL", "USD");
//			saldosConvertidos.put("Dólar", saldoUSD.getValorConvertido());
//		} catch (TaxaDeCambioException e) {
//			saldosConvertidos.put("Dólar", BigDecimal.ZERO); // Valor zero em caso de erro
//		}
//
//		try {
//			ConversionResponseDTO saldoEUR = exchangeRateService.convertAmount(saldo, "BRL", "EUR");
//			saldosConvertidos.put("Euro", saldoEUR.getValorConvertido());
//		} catch (TaxaDeCambioException e) {
//			saldosConvertidos.put("Euro", BigDecimal.ZERO); // Valor zero em caso de erro
//		}
//
//		return saldosConvertidos;
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
