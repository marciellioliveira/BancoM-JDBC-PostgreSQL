package br.com.marcielli.BancoM.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaFactory;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExisteNoBancoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.ContaNaoRealizouTransferenciaException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.ContaRepositoy;

@Service
public class ContaService {

	@Autowired
	private ContaRepositoy contaRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta save(Conta dto) {

		Cliente cliente = clienteRepository.findById(dto.getId())
				.orElseThrow(() -> new ContaExisteNoBancoException("A conta já existe no banco."));

		dto.setCliente(cliente);

		TaxaManutencao taxa = new TaxaManutencao(dto.getSaldoConta(), dto.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxa);

		Conta novaConta = null;

		String numeroConta = gerarNumeroDaConta();
		String numeroPix = gerarPixAleatorio();

		String novoPix = numeroPix.concat("-PIX");

		if (dto.getTipoConta() == TipoConta.CORRENTE) {
			novaConta = new ContaCorrente(taxa.getTaxaManutencaoMensal());
			novaConta.setTaxas(novaTaxa);

			String numContaCorrente = numeroConta.concat("-CC");
			novaConta.setNumeroConta(numContaCorrente);
		}

		if (dto.getTipoConta() == TipoConta.POUPANCA) {
			novaConta = new ContaPoupanca(taxa.getTaxaAcrescRend(), taxa.getTaxaMensal());
			novaConta.setTaxas(novaTaxa);

			String numContaPoupanca = numeroConta.concat("-PP");
			novaConta.setNumeroConta(numContaPoupanca);

		}

		novaConta.setCliente(cliente);
		novaConta.setSaldoConta(dto.getSaldoConta());
		novaConta.setCategoriaConta(taxa.getCategoria());
		novaConta.setTipoConta(dto.getTipoConta());
		novaConta.setPixAleatorio(novoPix);
		novaConta.setStatus(true);

		return contaRepository.save(novaConta);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta update(Long idContaParaAtualizar, Conta dto) {

		Conta conta = contaRepository.findById(idContaParaAtualizar).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta não pode ser atualizada porque não existe no banco."));

		TaxaManutencao taxa = new TaxaManutencao(conta.getSaldoConta(), dto.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxa);

		Conta novaConta = null;

		String numeroConta = gerarNumeroDaConta();
		String numeroPix = gerarPixAleatorio();

		String novoPix = numeroPix.concat("-PIX");

		if (dto.getTipoConta() == TipoConta.CORRENTE) {

			novaConta = new ContaCorrente(taxa.getTaxaManutencaoMensal());
			novaConta.setTaxas(novaTaxa);
			conta.setTaxas(novaConta.getTaxas());

			String numContaCorrente = numeroConta.concat("-CC");
			novaConta.setNumeroConta(numContaCorrente);
		}

		if (dto.getTipoConta() == TipoConta.POUPANCA) {

			novaConta = new ContaPoupanca(taxa.getTaxaAcrescRend(), taxa.getTaxaMensal());
			novaConta.setTaxas(novaTaxa);
			conta.setTaxas(novaConta.getTaxas());

			String numContaPoupanca = numeroConta.concat("-PP");
			novaConta.setNumeroConta(numContaPoupanca);

		}

		novaConta.setSaldoConta(dto.getSaldoConta());
		novaConta.setCategoriaConta(taxa.getCategoria());
		novaConta.setTipoConta(dto.getTipoConta());
		novaConta.setPixAleatorio(novoPix);
		novaConta.setStatus(true);

		conta.setPixAleatorio(novaConta.getPixAleatorio());
		conta.setNumeroConta(novaConta.getNumeroConta());
		conta.setCategoriaConta(novaConta.getCategoriaConta());
		conta.setTipoConta(novaConta.getTipoConta());
		conta.setPixAleatorio(novaConta.getPixAleatorio());

		return contaRepository.save(conta);

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Conta> getAll() {

		List<Conta> contasH2 = contaRepository.findAll();

		if (contasH2.size() <= 0) {
			throw new ContaNaoEncontradaException("Não existem contas cadastradas no banco.");
		}

		return contasH2;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Conta> getContaById(Long id) {

		Optional<Conta> contaH2 = contaRepository.findById(id);

		if (!contaH2.isPresent()) {
			throw new ContaNaoEncontradaException("Conta não encontrada.");
		}

		return contaH2;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean deleteConta(Long contaId) {

		Optional<Conta> contaH2 = contaRepository.findById(contaId);

		if (contaH2.isPresent()) {

			contaRepository.deleteById(contaId);
			return true;

		} else {

			throw new ContaNaoEncontradaException("A conta não pode ser deletada porque não existe no banco.");

		}

	}


	// Transferência TED
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirTED(Long idPessoaReceber, Long idContaReceber, Transferencia dadosContaEnviar) {

		if (idPessoaReceber == null || idContaReceber == null || dadosContaEnviar.getIdClienteOrigem() == null
				|| dadosContaEnviar.getIdContaOrigem() == null) {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Confirme os seus dados.");
		}

		// PathVariable
		Optional<Cliente> encontraRecebedorPorId = clienteRepository.findById(idPessoaReceber);
		Optional<Conta> encontraContaRecebedorPorId = contaRepository.findById(idContaReceber);

		// RequestBody
		Optional<Cliente> encontraPagadorPorId = clienteRepository.findById(dadosContaEnviar.getIdClienteOrigem());
		Optional<Conta> encontraContaPagadorPorId = contaRepository.findById(dadosContaEnviar.getIdContaOrigem());

		float valorTransferencia = dadosContaEnviar.getValor();

		if (valorTransferencia <= 0) {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		if (encontraRecebedorPorId.isPresent() && encontraContaRecebedorPorId.isPresent()
				&& encontraPagadorPorId.isPresent() && encontraContaPagadorPorId.isPresent()) {

			Cliente clienteReceber = encontraRecebedorPorId.get();
			Conta contaReceber = encontraContaRecebedorPorId.get();

			Cliente clientePagador = encontraPagadorPorId.get();
			Conta contaPagador = encontraContaPagadorPorId.get();

			if (contaReceber.isStatus() == false || contaPagador.isStatus() == false) {
				throw new ContaNaoRealizouTransferenciaException(
						"Essa conta foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
			}

			if (clienteReceber.getId() != null && contaReceber != null) {

				// Conta pagador -> Request Body (idContaOrigem) -> Conta Recebedor ->
				// PathVariable (id)
				Transferencia novaTransferencia = new Transferencia(contaPagador.getId(), contaReceber.getId());

				List<Conta> contasTransferidas = novaTransferencia.transferirTed(contaPagador, valorTransferencia,
						contaReceber);

				if (contasTransferidas != null) {

					for (Conta contasT : contasTransferidas) {

						// Pagador
						if (contasT.getId() == contaPagador.getId()) {

							if (contasT.getTipoConta() == TipoConta.CORRENTE) {

								ContaCorrente minhaContaCorrente = (ContaCorrente) contaPagador;
								minhaContaCorrente.getTransferencia().add(novaTransferencia);
								contaRepository.save(minhaContaCorrente);

							}

							if (contasT.getTipoConta() == TipoConta.POUPANCA) {

								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaPagador;
								minhaContaPoupanca.getTransferencia().add(novaTransferencia);
								contaRepository.save(minhaContaPoupanca);
							}

						}

						// Recebedor
						if (contasT.getId() == contaReceber.getId()) {

							if (contasT.getTipoConta() == TipoConta.CORRENTE) {

								ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
								contaRepository.save(minhaContaCorrente);

							}

							if (contasT.getTipoConta() == TipoConta.POUPANCA) {

								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
								contaRepository.save(minhaContaPoupanca);
							}

						}

					}

				}

			} else {
				throw new ContaNaoRealizouTransferenciaException(
						"O cliente para o qual você está tentando transferir não tem essa conta. Confirme os seus dados.");
			}

		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Confirme os seus dados.");
		}

		return true;
	}

	// Transferência PIX
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirPIX(String pixAleatorio, Transferencia dadosContaEnviar) {

		Conta contaReceber = null;

		// RequestBody
		Optional<Cliente> encontraPagadorPorId = clienteRepository.findById(dadosContaEnviar.getIdClienteOrigem());
		Optional<Conta> encontraContaPagadorPorId = contaRepository.findById(dadosContaEnviar.getIdContaOrigem());

		float valorTransferencia = dadosContaEnviar.getValor();

		if (valorTransferencia <= 0) {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		if (encontraContaPagadorPorId.isPresent() && encontraPagadorPorId.isPresent()) {

			Conta dadosParaEnviar = encontraContaPagadorPorId.get();

			Transferencia novaTransferencia = null;

			if (dadosParaEnviar.isStatus() == false) {
				throw new ContaNaoRealizouTransferenciaException(
						"Essa conta foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
			}

			boolean pixExiste = false;

			for (Conta getContaPix : getAll()) {

				if (getContaPix.getTipoConta() == TipoConta.CORRENTE) {

					ContaCorrente minhaContaCorrente = (ContaCorrente) getContaPix;

					if (pixAleatorio.equals(minhaContaCorrente.getPixAleatorio())) {

						if (minhaContaCorrente.isStatus() == false) {
							throw new ContaNaoRealizouTransferenciaException(
									"Essa conta foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
						}

						novaTransferencia = new Transferencia(dadosContaEnviar.getIdClienteOrigem(),
								minhaContaCorrente.getCliente().getId());
						contaReceber = minhaContaCorrente;

						pixExiste = true;

					}
				}

				if (getContaPix.getTipoConta() == TipoConta.POUPANCA) {

					ContaPoupanca minhaContaPoupanca = (ContaPoupanca) getContaPix;

					if (pixAleatorio.equals(minhaContaPoupanca.getPixAleatorio())) {

						if (minhaContaPoupanca.isStatus() == false) {
							throw new ContaNaoRealizouTransferenciaException(
									"Essa conta foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
						}

						novaTransferencia = new Transferencia(dadosContaEnviar.getIdClienteOrigem(),
								minhaContaPoupanca.getCliente().getId());
						contaReceber = minhaContaPoupanca;
					}

					pixExiste = true;

				}
			}

			if (pixExiste == false) {
				// Pix não existe
				throw new ContaNaoRealizouTransferenciaException(
						"O PIX " + pixAleatorio + " que você digitou é inválido. Tente transferir para um PIX válido.");

			}

			List<Conta> contasTransferidas = novaTransferencia.transferirPix(dadosParaEnviar, valorTransferencia,
					contaReceber);

			if (contasTransferidas != null) {

				for (Conta contasT : contasTransferidas) {

					// Pagador
					if (contasT.getId() == dadosParaEnviar.getId()) {

						if (contasT.getTipoConta() == TipoConta.CORRENTE) {

							ContaCorrente minhaContaCorrente = (ContaCorrente) dadosParaEnviar;
							minhaContaCorrente.getTransferencia().add(novaTransferencia);
							contaRepository.save(minhaContaCorrente);

						}

						if (contasT.getTipoConta() == TipoConta.POUPANCA) {

							ContaPoupanca minhaContaPoupanca = (ContaPoupanca) dadosParaEnviar;
							minhaContaPoupanca.getTransferencia().add(novaTransferencia);
							contaRepository.save(minhaContaPoupanca);
						}

					}

					// Recebedor
					if (contasT.getId() == contaReceber.getId()) {

						if (contasT.getTipoConta() == TipoConta.CORRENTE) {

							ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
							contaRepository.save(minhaContaCorrente);

						}

						if (contasT.getTipoConta() == TipoConta.POUPANCA) {

							ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
							contaRepository.save(minhaContaPoupanca);
						}

					}

				}

			}

		} else {
			throw new ContaNaoRealizouTransferenciaException("O PIX não foi realizado. Confirme os seus dados.");
		}

		return true;
	}

	// Transferência DEPOSITO
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirDEPOSITAR(Long idClienteReceber, Long idContaReceber, Transferencia dadosContaEnviar) {

		if (idClienteReceber == null || idContaReceber == null) {
			throw new ContaNaoRealizouTransferenciaException("O depósito não foi realizado. Confirme os seus dados.");
		}

		// Param
		Optional<Cliente> encontrarClienteRecebedorPorId = clienteRepository.findById(idClienteReceber);
		Optional<Conta> encontraContaRecebedorPorId = contaRepository.findById(idContaReceber);

		float valorDeposito = dadosContaEnviar.getValor();

		if (valorDeposito <= 0) {
			throw new ContaNaoRealizouTransferenciaException(
					"O depósito não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		if (encontraContaRecebedorPorId.isPresent() && encontrarClienteRecebedorPorId.isPresent()) {

			// Conta dadosParaEnviar = encontraContaPagadorPorId.get();
			Conta contaReceber = encontraContaRecebedorPorId.get();
			Cliente clienteReceber = encontrarClienteRecebedorPorId.get();

			if (contaReceber.isStatus() == false) {
				throw new ContaNaoRealizouTransferenciaException(
						"A conta foi desativada.  Confirme os seus dados e faça o depósito em uma conta ativa.");
			}

			boolean clienteTemConta = false;
			for (Conta contas : clienteReceber.getContas()) {
				if (contas.getId() == contaReceber.getId()) {
					clienteTemConta = true;
				}
			}

			if (clienteTemConta) {

				Transferencia novoDeposito = new Transferencia();

				List<Conta> contasDeposito = novoDeposito.depositar(valorDeposito, contaReceber);

				if (contasDeposito != null) {

					for (Conta contasT : contasDeposito) {

						// Recebedor
						if (contasT.getId() == contaReceber.getId()) {

							if (contasT.getTipoConta() == TipoConta.CORRENTE) {

								ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
								minhaContaCorrente.getTransferencia().add(novoDeposito);
								contaRepository.save(minhaContaCorrente);

							}

							if (contasT.getTipoConta() == TipoConta.POUPANCA) {

								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
								minhaContaPoupanca.getTransferencia().add(novoDeposito);
								contaRepository.save(minhaContaPoupanca);
							}

						}

					}

				}

			}

		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"O depósito não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		return true;

	}

	// Transferência sacar
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirSACAR(Long idClientePegar, Long idContaPegar, Transferencia dadosContaEnviar) {

		if (idClientePegar == null || idContaPegar == null) {
			throw new ContaNaoRealizouTransferenciaException("O saque não foi realizado. Confirme os seus dados.");
		}

		// Param
		Optional<Cliente> encontrarClienteSaquePorId = clienteRepository.findById(idClientePegar);
		Optional<Conta> encontraContaSaquePorId = contaRepository.findById(idContaPegar);

		float valorSaque = dadosContaEnviar.getValor();

		if (valorSaque <= 0) {
			throw new ContaNaoRealizouTransferenciaException(
					"O saque não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		if (encontraContaSaquePorId.isPresent() && encontrarClienteSaquePorId.isPresent()) {

			// Conta dadosParaEnviar = encontraContaPagadorPorId.get();
			Conta contaSacar = encontraContaSaquePorId.get();
			Cliente clienteSacar = encontrarClienteSaquePorId.get();

			if (contaSacar.isStatus() == false) {
				throw new ContaNaoRealizouTransferenciaException(
						"A conta foi desativada.  Confirme os seus dados e faça o saque através de uma conta ativa.");
			}

			boolean clienteTemConta = false;
			for (Conta contas : clienteSacar.getContas()) {
				if (contas.getId() == contaSacar.getId()) {
					clienteTemConta = true;
				}
			}

			if (clienteTemConta) {

				Transferencia novoSaque = new Transferencia();

				List<Conta> contasSaque = novoSaque.sacar(valorSaque, contaSacar);

				if (contasSaque != null) {

					for (Conta contasT : contasSaque) {

						// Retirou
						if (contasT.getId() == contaSacar.getId()) {

							if (contasT.getTipoConta() == TipoConta.CORRENTE) {

								ContaCorrente minhaContaCorrente = (ContaCorrente) contaSacar;
								minhaContaCorrente.getTransferencia().add(novoSaque);
								contaRepository.save(minhaContaCorrente);

							}

							if (contasT.getTipoConta() == TipoConta.POUPANCA) {

								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaSacar;
								minhaContaPoupanca.getTransferencia().add(novoSaque);
								contaRepository.save(minhaContaPoupanca);
							}

						}

					}

				}

			}

		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"O saque não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		return true;
	}

//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public float[] exibirSaldo(Long clienteId) {
//
//		Optional<Cliente> clienteVerSaldo = clienteRepository.findById(clienteId);
//
//		float[] saldoContas = { 0, 0, 0 };
//
//		if (clienteVerSaldo.isPresent()) {
//
//			Cliente cliente = clienteVerSaldo.get();
//			
//			for(Conta getContas : cliente.getContas()) {
//				
//				if(getContas.isStatus() == true && getContas.getTipoConta() == TipoConta.CORRENTE) {
//					saldoContas[0] += getContas.getSaldoConta();
//					
//					if(getContas.isStatus() == false) {
//						saldoContas[0] += 0;
//					}
//				}
//				
//				if(getContas.isStatus() == true && getContas.getTipoConta() == TipoConta.POUPANCA) {
//					saldoContas[1] += getContas.getSaldoConta();					
//					
//					if(getContas.isStatus() == false) {
//						saldoContas[1] += 0;
//					}
//				}
//				
//				
//				
//				
//				
//			}

	// }

//		saldoContas[2] = saldoContas[0] + saldoContas[1];
//
//		return saldoContas;
//
//	}

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
