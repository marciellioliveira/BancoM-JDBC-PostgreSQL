package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marcielli.BancoM.dto.security.ContaCreateDTO;
import br.com.marcielli.BancoM.dto.security.ContaUpdateDTO;
import br.com.marcielli.BancoM.dto.security.ConversionResponseDTO;
import br.com.marcielli.BancoM.dto.security.UserContaDepositoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaPixDTO;
import br.com.marcielli.BancoM.dto.security.UserContaRendimentoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaResponseDTO;
import br.com.marcielli.BancoM.dto.security.UserContaSaqueDTO;
import br.com.marcielli.BancoM.dto.security.UserContaTaxaManutencaoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaTedDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.entity.ValidacaoUsuarioAtivo;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.enuns.TipoTransferencia;
import br.com.marcielli.BancoM.exception.ClienteEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExibirSaldoErroException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.PermissaoNegadaException;
import br.com.marcielli.BancoM.exception.TaxaDeCambioException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.ContaRepositoy;
import br.com.marcielli.BancoM.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserContaService {

	private final ContaRepositoy contaRepository;
	private final UserRepository userRepository;
	private final ExchangeRateService exchangeRateService;
	private final ClienteRepository clienteRepository;

	private static final Logger log = LoggerFactory.getLogger(UserContaService.class);

	public UserContaService(ContaRepositoy contaRepository, UserRepository userRepository,
			ExchangeRateService exchangeRateService, ClienteRepository clienteRepository) {
		this.contaRepository = contaRepository;
		this.userRepository = userRepository;
		this.exchangeRateService = exchangeRateService;
		this.clienteRepository = clienteRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta save(ContaCreateDTO dto, JwtAuthenticationToken token) {
	    User currentUser = ValidacaoUsuarioAtivo.validarUsuarioAdmin(userRepository, token);
	    ValidacaoUsuarioAtivo.verificarUsuarioAtivo(currentUser);

	    // Definindo para qual cliente a conta será criada
	    Cliente clienteAlvo;

	    if (ValidacaoUsuarioAtivo.isAdmin(currentUser)) {
	        if (dto.idUsuario() == null) {
	            throw new IllegalArgumentException("Admin deve informar o ID do cliente.");
	        }
	        clienteAlvo = clienteRepository.findById(dto.idUsuario())
	                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
	    } else {
	        Long currentUserIdAsLong = Long.valueOf(currentUser.getId());
	        if (dto.idUsuario() == null || !dto.idUsuario().equals(currentUserIdAsLong)) {
	            throw new ClienteEncontradoException("Usuário não tem permissão para criar conta para outro ID.");
	        }
	        clienteAlvo = currentUser.getCliente();
	    }

	    TaxaManutencao taxa = new TaxaManutencao(dto.saldoConta(), dto.tipoConta());
	    List<TaxaManutencao> novaTaxa = new ArrayList<>();
	    novaTaxa.add(taxa);

	    String numeroConta = gerarNumeroDaConta();
	    String numeroPix = gerarPixAleatorio();
	    String novoPix = numeroPix.concat("-PIX");

	    Conta conta = null;

	    try {
	        if (dto.tipoConta() == TipoConta.CORRENTE) {
	            ContaCorrente contaCorrente = new ContaCorrente(taxa.getTaxaManutencaoMensal());
	            contaCorrente.setTaxaManutencaoMensal(taxa.getTaxaManutencaoMensal()); // Garantindo que a taxa está sendo setada
	            String numContaCorrente = numeroConta.concat("-CC");
	            contaCorrente.setNumeroConta(numContaCorrente);
	            conta = contaCorrente;
	        } else if (dto.tipoConta() == TipoConta.POUPANCA) {
	            ContaPoupanca contaPoupanca = new ContaPoupanca(taxa.getTaxaAcrescRend(), taxa.getTaxaMensal());
	            contaPoupanca.setTaxaAcrescRend(taxa.getTaxaAcrescRend()); // Garantindo que as taxas estão sendo setadas
	            contaPoupanca.setTaxaMensal(taxa.getTaxaMensal());
	            String numContaPoupanca = numeroConta.concat("-PP");
	            contaPoupanca.setNumeroConta(numContaPoupanca);
	            conta = contaPoupanca;
	        }

	        conta.setTaxas(novaTaxa);
	        conta.setPixAleatorio(novoPix);
	        conta.setCategoriaConta(taxa.getCategoria());
	        conta.setCliente(clienteAlvo);
	        conta.setTipoConta(dto.tipoConta());
	        conta.setSaldoConta(dto.saldoConta());
	        conta.setStatus(true);
	        conta.setCategoriaConta(taxa.getCategoria()); 

	        contaRepository.save(conta);

	    } catch (NumberFormatException e) {
	        System.out.println("ID inválido no token: " + token.getName());
	    }

	    return conta;
	}


	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta getContasById(Long id) {
		return contaRepository.findById(id).orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta update(Long idConta, ContaUpdateDTO dto, JwtAuthenticationToken token) {

		User currentUser = ValidacaoUsuarioAtivo.validarUsuarioAdmin(userRepository, token);
		ValidacaoUsuarioAtivo.verificarUsuarioAtivo(currentUser);

		Conta contaExistente = contaRepository.findById(idConta)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada"));

		if (!contaExistente.getStatus()) { // A conta está ativa? Porque no banco eu prefiro não deletar e somente
											// desativar
			throw new ClienteNaoEncontradoException("Não é possível atualizar uma conta desativada");
		}

		boolean isDonoDaConta = contaExistente.getCliente().getUser().getId().equals(currentUser.getId());
		Long idDonoConta = contaExistente.getCliente().getId();

		if (ValidacaoUsuarioAtivo.isAdmin(currentUser)) {
			if (dto.idUsuario() != null && !dto.idUsuario().equals(idDonoConta)) {
				throw new ClienteEncontradoException("A conta não pode ser deltada");
			}

			if (ValidacaoUsuarioAtivo.isAdmin(contaExistente.getCliente().getUser()) && !isDonoDaConta) {
				throw new ClienteEncontradoException("Admin não pode editar contas de outros admins");
			}
		} else {
			if (dto.idUsuario() == null || !dto.idUsuario().equals(currentUser.getCliente().getId())) {
				throw new ClienteEncontradoException("Você só pode editar contas com seu próprio ID");
			}

			if (!isDonoDaConta) {
				throw new ClienteEncontradoException("A conta não pertence ao usuário logado");
			}
		}

		String novoPix = dto.pixAleatorio().concat("-PIX");
		contaExistente.setPixAleatorio(novoPix);
		
		//Aqui tive que forçar o flush imediat porque ele estava salvando corretamente no banco mas não estava imprimindo corretamente no json
		Conta contaAtualizada = contaRepository.saveAndFlush(contaExistente); 

		return contaRepository.save(contaAtualizada);
	}

	@Transactional
	public boolean delete(Long idConta, ContaUpdateDTO dto, JwtAuthenticationToken token) {

		User currentUser = ValidacaoUsuarioAtivo.validarUsuarioAdmin(userRepository, token);
		ValidacaoUsuarioAtivo.verificarUsuarioAtivo(currentUser);

		Conta contaExistente = contaRepository.findById(idConta)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada"));

		boolean isDonoDaConta = contaExistente.getCliente().getUser().getId().equals(currentUser.getId());

		if (ValidacaoUsuarioAtivo.isAdmin(currentUser)) {

			if (dto.idUsuario() != null && !dto.idUsuario().equals(contaExistente.getCliente().getId())) {
				throw new ClienteEncontradoException("ID do cliente no DTO não corresponde à conta informada");
			}

			if (ValidacaoUsuarioAtivo.isAdmin(contaExistente.getCliente().getUser()) && !isDonoDaConta) {
				throw new ClienteNaoEncontradoException("Admin não pode desativar contas de outros admins");
			}
		} else {

			if (dto.idUsuario() == null || !dto.idUsuario().equals(currentUser.getCliente().getId())) {
				throw new ClienteEncontradoException("Você só pode desativar contas com seu próprio ID");
			}

			if (!isDonoDaConta) {
				throw new ClienteEncontradoException("Você só pode desativar suas próprias contas");
			}
		}

		contaExistente.setStatus(false);
		contaRepository.save(contaExistente);
		return true;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirTED(Long idContaReceber, UserContaTedDTO dto, JwtAuthenticationToken token) {

		User currentUser = ValidacaoUsuarioAtivo.validarUsuarioAdmin(userRepository, token);
		ValidacaoUsuarioAtivo.verificarUsuarioAtivo(currentUser);

		Conta contaOrigem = contaRepository.findById(dto.idContaOrigem())
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta origem não existe."));

		Conta contaDestino = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta destino não existe."));

		boolean isDonoDaContaOrigem = contaOrigem.getCliente().getUser().getId().equals(currentUser.getId());

		if (ValidacaoUsuarioAtivo.isAdmin(currentUser)) {

			if (ValidacaoUsuarioAtivo.isAdmin(contaOrigem.getCliente().getUser()) && !isDonoDaContaOrigem) {
				throw new PermissaoNegadaException("Admin não pode transferir de contas de outros admins");
			}

			if (dto.idUsuario() != null && !dto.idUsuario().equals(contaOrigem.getCliente().getId())) {
				throw new PermissaoNegadaException("ID do cliente no DTO não corresponde à conta origem");
			}
		} else {
			if (!isDonoDaContaOrigem) {
				throw new PermissaoNegadaException("Você só pode transferir de suas próprias contas");
			}

			if (!dto.idUsuario().equals(currentUser.getCliente().getId())) {
				throw new PermissaoNegadaException("Você só pode realizar transferências com seu próprio ID");
			}
		}

		if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
			throw new ContaExibirSaldoErroException("Saldo insuficiente na conta origem.");
		}

		List<Conta> contasTransferidas = new ArrayList<Conta>();

		contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
		contasTransferidas.add(contaOrigem);

		TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaOrigem);

		contaOrigem.setTaxas(novaTaxa);
		contaOrigem.setCategoriaConta(taxaContaOrigem.getCategoria());

		if (contaOrigem instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
		}

		if (contaOrigem instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
		}

		Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino, TipoTransferencia.TED);
		contaOrigem.getTransferencia().add(transferindo);

		contaRepository.save(contaOrigem);

		contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
		contasTransferidas.add(contaDestino);

		TaxaManutencao taxaContaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());

		List<TaxaManutencao> novaTaxa2 = new ArrayList<TaxaManutencao>();
		novaTaxa2.add(taxaContaDestino);

		contaDestino.setTaxas(novaTaxa2);
		contaDestino.setCategoriaConta(taxaContaDestino.getCategoria());

		if (contaDestino instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaDestino.getTaxaManutencaoMensal());
		}

		if (contaDestino instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaDestino.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaDestino.getTaxaMensal());
		}

		contaRepository.save(contaDestino);

		return true;
	}

	// Transferências
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean transferirTED(Long idContaReceber, UserContaTedDTO dto) {
//
//		Conta contaOrigem = contaRepository.findById(dto.idContaOrigem())
//				.orElseThrow(() -> new ContaNaoEncontradaException("A conta origem não existe."));
//
//		Conta contaDestino = contaRepository.findById(idContaReceber)
//				.orElseThrow(() -> new ContaNaoEncontradaException("A conta destino não existe."));
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

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public BigDecimal exibirSaldo(Long contaId) {

		Conta contaSaldo = contaRepository.findById(contaId)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));

		return contaSaldo.getSaldoConta();

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String, BigDecimal> exibirSaldoConvertido(Long contaId) {
		Conta contaSaldo = contaRepository.findById(contaId)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));

		BigDecimal saldo = contaSaldo.getSaldoConta();

		Map<String, BigDecimal> saldosConvertidos = new LinkedHashMap<>();
		saldosConvertidos.put("Saldo em Real", saldo);

		try {
			ConversionResponseDTO saldoUSD = exchangeRateService.convertAmount(saldo, "BRL", "USD");
			saldosConvertidos.put("Dólar", saldoUSD.getValorConvertido());
		} catch (TaxaDeCambioException e) {
			saldosConvertidos.put("Dólar", BigDecimal.ZERO); // Valor zero em caso de erro
		}

		try {
			ConversionResponseDTO saldoEUR = exchangeRateService.convertAmount(saldo, "BRL", "EUR");
			saldosConvertidos.put("Euro", saldoEUR.getValorConvertido());
		} catch (TaxaDeCambioException e) {
			saldosConvertidos.put("Euro", BigDecimal.ZERO); // Valor zero em caso de erro
		}

		return saldosConvertidos;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirPIX(Long idContaReceber, UserContaPixDTO dto) {

		Conta contaOrigem = contaRepository.findById(dto.idContaOrigem())
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta origem não existe."));

		Conta contaDestino = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta destino não existe."));

		contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));

		TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaOrigem);

		contaOrigem.setTaxas(novaTaxa);
		contaOrigem.setCategoriaConta(taxaContaOrigem.getCategoria());

		Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino, TipoTransferencia.PIX);
		contaOrigem.getTransferencia().add(transferindo);

		if (contaOrigem instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
		}

		if (contaOrigem instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
		}

		contaRepository.save(contaOrigem);

		contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));

		TaxaManutencao taxaContaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());

		List<TaxaManutencao> novaTaxa2 = new ArrayList<TaxaManutencao>();
		novaTaxa2.add(taxaContaDestino);

		contaDestino.setTaxas(novaTaxa2);
		contaDestino.setCategoriaConta(taxaContaDestino.getCategoria());

		if (contaDestino instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaDestino.getTaxaManutencaoMensal());
		}

		if (contaDestino instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaDestino.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaDestino.getTaxaMensal());
		}

		contaRepository.save(contaDestino);

		return true;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirDEPOSITO(Long idContaReceber, UserContaDepositoDTO dto) {

		Conta conta = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));

		conta.setSaldoConta(conta.getSaldoConta().add(dto.valor()));

		TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaOrigem);

		conta.setTaxas(novaTaxa);
		conta.setCategoriaConta(taxaContaOrigem.getCategoria());

		Transferencia transferindo = new Transferencia(conta, dto.valor(), conta, TipoTransferencia.DEPOSITO);
		conta.getTransferencia().add(transferindo);

		if (conta instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
		}

		if (conta instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
		}

		contaRepository.save(conta);

		return true;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirSAQUE(Long idContaReceber, UserContaSaqueDTO dto) {

		Conta conta = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));

		if (conta.getSaldoConta().compareTo(dto.valor()) < 0 || conta.getSaldoConta().compareTo(BigDecimal.ZERO) == 0) {
			throw new ContaExibirSaldoErroException("Saldo insuficiente.");
		}

		conta.setSaldoConta(conta.getSaldoConta().subtract(dto.valor()));

		TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaOrigem);

		conta.setTaxas(novaTaxa);
		conta.setCategoriaConta(taxaContaOrigem.getCategoria());

		Transferencia transferindo = new Transferencia(conta, dto.valor(), conta, TipoTransferencia.SAQUE);
		conta.getTransferencia().add(transferindo);

		if (conta instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
		}

		if (conta instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
		}

		contaRepository.save(conta);

		return true;
	}

	// ROTAS MANUAIS - FUNCIONAM
//
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Conta manutencaoTaxaCC(Long idConta) {
//		Conta conta = contaRepository.findById(idConta)
//				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada."));
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
//
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Conta rendimentoTaxaCP(Long idConta) {
//		Conta conta = contaRepository.findById(idConta)
//				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada."));
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

	// Agendamento de Taxas de Conta Corrente utilizando o @Scheduled(cron = "0 0 2
	// 1 * ?") do Spring
	public List<ContaCorrente> buscarTodasContasCorrentesAtivas() {
		return contaRepository.findAll().stream().filter(conta -> conta instanceof ContaCorrente && conta.getStatus())
				.map(conta -> (ContaCorrente) conta).collect(Collectors.toList());

	}

	// Agendamento de Taxas de Conta Poupança utilizando o @Scheduled(cron = "0 0 23
	// * *") do Spring
	public List<ContaPoupanca> buscarTodasContasPoupancaAtivas() {
		return contaRepository.findAll().stream().filter(conta -> conta instanceof ContaPoupanca && conta.getStatus())
				.map(conta -> (ContaPoupanca) conta).collect(Collectors.toList());

	}

	public Conta rendimentoTaxaCP(Long idConta) {
		log.debug("Iniciando aplicação de rendimento para conta {}", idConta);

		ContaPoupanca conta = (ContaPoupanca) contaRepository.findById(idConta).orElseThrow(() -> {
			log.error("Conta poupança {} não encontrada", idConta);
			return new ClienteNaoEncontradoException("Conta não encontrada");
		});

		if (!conta.getStatus()) {
			log.warn("Tentativa de aplicar rendimento em conta poupança inativa - ID: {}", idConta);

		}

		BigDecimal rendimento = calcularRendimentoCP(conta);
		conta.creditar(rendimento);

		log.info("Rendimento de {} aplicado na conta poupança {}", rendimento, idConta);
		return contaRepository.save(conta);
	}

	private BigDecimal calcularRendimentoCP(ContaPoupanca conta) {

		log.trace("Calculando rendimento para conta {}", conta.getId());
		return conta.getSaldoConta().multiply(conta.getTaxaAcrescRend());
	}

	public Conta manutencaoTaxaCC(Long idConta) {
		log.debug("Iniciando cobrança de manutenção para conta {}", idConta);

		ContaCorrente conta = (ContaCorrente) contaRepository.findById(idConta).orElseThrow(() -> {
			log.error("Conta corrente {} não encontrada", idConta);
			return new ClienteNaoEncontradoException("Conta não encontrada");
		});

		if (!conta.getStatus()) {
			log.warn("Tentativa de cobrar taxa em conta corrente inativa - ID: {}", idConta);

		}

		BigDecimal taxa = calcularAcrescimoTaxaCC(conta);
		conta.debitar(taxa);

		log.info("Taxa de manutenção de {} debitada da conta {}", taxa, idConta);
		return contaRepository.save(conta);
	}

	private BigDecimal calcularAcrescimoTaxaCC(ContaCorrente conta) {
		// Lógica de cálculo da taxa
		log.trace("Calculando taxa para conta {}", conta.getId());
		return conta.getTaxaManutencaoMensal();
	}

	public ResponseEntity<?> processarOperacaoConta(Long idConta, Function<Long, Conta> operacao) {
		try {
			Conta contaAtualizada = operacao.apply(idConta);
			return ResponseEntity.ok(converterParaDTO(contaAtualizada));
		} catch (ClienteNaoEncontradoException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	public UserContaResponseDTO converterParaDTO(Conta conta) {
		UserContaResponseDTO response = new UserContaResponseDTO();
		response.setId(conta.getId());
		response.setTipoConta(conta.getTipoConta());
		response.setCategoriaConta(conta.getCategoriaConta());
		if (conta instanceof ContaCorrente contaCorrente) {
			response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
		}

		if (conta instanceof ContaPoupanca contaPoupanca) {
			response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
			response.setTaxaMensal(contaPoupanca.getTaxaMensal());
		}

		response.setSaldoConta(conta.getSaldoConta());
		response.setNumeroConta(conta.getNumeroConta());
		response.setPixAleatorio(conta.getPixAleatorio());
		response.setStatus(conta.getStatus());
		return response;
	}

	// Outros métodos
	public String gerarNumeroDaConta() {

		int[] sequencia = new int[8];
		Random random = new Random();
		String minhaConta = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			minhaConta += Integer.toString(sequencia[i]);
		}

		return minhaConta;
	}

	public String gerarPixAleatorio() {

		int[] sequencia = new int[8];
		Random random = new Random();
		String meuPix = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			meuPix += Integer.toString(sequencia[i]);
		}

		return meuPix;
	}

}
