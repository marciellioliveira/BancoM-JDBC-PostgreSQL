package br.com.marcielli.BancoM.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaFactory;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.Taxas;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExibirSaldoErroException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.ContaNaoFoiPossivelAlterarNumeroException;
import br.com.marcielli.BancoM.exception.ContaNaoRealizouTransferenciaException;
import br.com.marcielli.BancoM.exception.ContaTipoContaNaoExisteException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.ContaRepositoy;

@Service
public class ContaService {

	@Autowired
	private ContaRepositoy contaRepository;
	
	@Autowired
	private ClienteRepository clienteRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta save(Conta contaParaCriar) {
		
		Conta contaCriada = null;
		
		if (contaParaCriar.getId() == null) {	
			throw new ClienteNaoEncontradoException("Para cadastrar uma conta, você precisa ser um cliente do banco.");
		} 

		//Buscar cliente por ID
		Optional<Cliente> clienteExiste = clienteRepository.findById(contaParaCriar.getId());
		
		if(clienteExiste.isPresent()) {		
			
			Cliente simClienteExiste = clienteExiste.get();
			contaParaCriar.setCliente(simClienteExiste);
			
			// Validar dados
			if (contaParaCriar.getSaldoConta() < 0) {
				throw new ContaTipoContaNaoExisteException("O saldo inicial da conta precisa ser positivo");
			}

			//gerarNumeroDaConta(contaParaCriar);
			
			
			//Padrão de Design Factory	
			
			contaCriada = ContaFactory.criarConta(contaParaCriar);
			
			if(contaCriada != null) {				
				contaRepository.save(contaCriada);
				simClienteExiste.getContas().add(contaCriada);
				clienteRepository.save(simClienteExiste);
			} 	
			
		} else {
			throw new ClienteNaoEncontradoException("Para cadastrar uma conta, você precisa ser um cliente do banco.");
		}
		
		return contaCriada;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta update(Long idContaParaAtualizar, Conta contaComDadosParaAtualizar) {
		
		Optional<Conta> contaH2ComDados = contaRepository.findById(contaComDadosParaAtualizar.getId());
		Optional<Conta> contaH2ParaAtualizar = contaRepository.findById(idContaParaAtualizar);
		
		System.err.println("teste: "+contaH2ParaAtualizar);
		
		Conta contaAtualizada = null;
		
		if (contaH2ComDados.isPresent() && contaH2ParaAtualizar.isPresent()) {
			
			Conta contaDadosParaInserir = contaH2ComDados.get();
			Conta contaParaAtualizar = contaH2ParaAtualizar.get();
			
//			Taxas atualizarTaxas = new Taxas();
//			
//			Taxas taxasAtualizadas = new Taxas();
//			
//			
//			System.err.println("conta antiga? "+contaParaAtualizar);
//			
//			
//			contaAtualizada = atualizarTaxas.atualizarTaxas(taxasAtualizadas, contaDadosParaInserir, contaParaAtualizar);
//					//atualizarTaxas(taxasAtualizadas, contaDadosParaInserir, contaParaAtualizar);
//			
//			System.err.println("conta atualiza? "+contaAtualizada);
			
			
			
			
//			List<Taxas> novaTaxa = new ArrayList<Taxas>();
//			novaTaxa.add(atualizarTaxas);
//			
//			contaAtualizada.setTaxas(novaTaxa);
//			System.err.println("Atuazalida? "+atualizarTaxas);
//			
//			System.err.println("Conta atualizada:? "+contaAtualizada);
			
			
		
			return contaAtualizada;

//		Optional<Conta> contaH2ComDados = contaRepository.findById(contaComDadosParaAtualizar.getId());
//		
//		Optional<Conta> contaH2ParaAtualizar = contaRepository.findById(idContaParaAtualizar);
//		
//		
//		
//		Conta contaAtualizada = null;
//
//		if (contaH2ComDados.isPresent() && contaH2ParaAtualizar.isPresent()) {
//
//			Conta contaDadosParaInserir = contaH2ComDados.get();
//			Conta contaParaAtualizar = contaH2ParaAtualizar.get();
//			
//			CategoriaConta categoriaConta = null;
//			float taxaManutencaoMensalCC = 0;	
//			float taxaAcrescRendPP1 = 0;
//			float taxaMensalPP2 = 0;			
//			
//			String numeroDaContaAtual = contaParaAtualizar.getNumeroConta();
//			String numeroDaContaNovo = atualizarNumeroDaConta(numeroDaContaAtual);
//
//			if(contaDadosParaInserir.getSaldoConta() <= 1000) {
//				
//				//Conta Corrente
//				taxaManutencaoMensalCC = 12.00f;
//				
//				//Todas
//				categoriaConta = CategoriaConta.COMUM;
//				
//				//Conta Poupança
//				taxaAcrescRendPP1 = 0.005f;	
//				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);
//			}
//			
//			if(contaDadosParaInserir.getSaldoConta() > 1000 && contaDadosParaInserir.getSaldoConta() <= 5000) {
//				
//				//Conta Corrente
//				taxaManutencaoMensalCC = 8.00f;
//				
//				//Todas
//				categoriaConta = CategoriaConta.SUPER;
//				
//				//Conta Poupança
//				taxaAcrescRendPP1 = 0.007f;
//				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);
//			}
//			
//			if(contaDadosParaInserir.getSaldoConta() > 5000) {
//				
//				//Conta Corrente
//				taxaManutencaoMensalCC = 0f;	
//				
//				//Todas
//				categoriaConta = CategoriaConta.PREMIUM;
//				
//				//Conta Poupança
//				taxaAcrescRendPP1 = 0.009f;	
//				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);				
//			}	
//			
//			
//			contaParaAtualizar.setCliente(contaDadosParaInserir.getCliente());
//			contaParaAtualizar.setTipoConta(contaDadosParaInserir.getTipoConta());
//			contaParaAtualizar.setSaldoConta(contaDadosParaInserir.getSaldoConta());
//			contaParaAtualizar.setCategoriaConta(categoriaConta);
//			contaParaAtualizar.setNumeroConta(numeroDaContaNovo);
//			
//			
//			return contaAtualizada;

		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser atualizada porque não existe no banco.");
		}
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
	public String delete(Long contaId) {

		Optional<Conta> contaH2 = contaRepository.findById(contaId);

		if (contaH2.isPresent()) {
			contaRepository.deleteById(contaId);
			return "deletado";
		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser deletada porque não existe no banco.");
		}

	}

	// Transferência TED
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirTED(Long idPessoaReceber, Long idContaReceber, Transferencia dadosContaEnviar) {
		
		if(idPessoaReceber == null || idContaReceber == null || dadosContaEnviar.getIdClienteOrigem() == null || dadosContaEnviar.getIdContaOrigem() == null) {		
			throw new ContaNaoRealizouTransferenciaException("A transferência não foi realizada. Confirme os seus dados.");
		}		
				
		//@PathVariable
		Optional<Cliente> encontraRecebedorPorId = clienteRepository.findById(idPessoaReceber);		
		Optional<Conta> encontraContaRecebedorPorId = contaRepository.findById(idContaReceber);	
		
		//@RequestBody
		Optional<Cliente> encontraPagadorPorId = clienteRepository.findById(dadosContaEnviar.getIdClienteOrigem());
		Optional<Conta> encontraContaPagadorPorId = contaRepository.findById(dadosContaEnviar.getIdContaOrigem());
		
		float valorTransferencia = dadosContaEnviar.getValor();		
		
		if(valorTransferencia <= 0) {
			throw new ContaNaoRealizouTransferenciaException("A transferência não foi realizada. Valor precisa ser maior que 0. Confirme os seus dados.");
		}
		
		if(encontraRecebedorPorId.isPresent() && encontraContaRecebedorPorId.isPresent() && encontraPagadorPorId.isPresent() && encontraContaPagadorPorId.isPresent()) {
			
		Cliente clienteReceber = encontraRecebedorPorId.get();
		Conta contaReceber = encontraContaRecebedorPorId.get();
		
		Cliente clientePagador = encontraPagadorPorId.get();
		Conta contaPagador = encontraContaPagadorPorId.get();
			
			if(clienteReceber.getId() != null && contaReceber != null) {			
			
			System.err.println("Conta Enviar: \nPagador id: "+clientePagador.getId()+"\nPagador conta ID: "+contaPagador.getId()+"\nPagador saldo total: "+contaPagador.getSaldoConta());
			
			System.err.println("\nValor da transferência: "+valorTransferencia+"\n");
			
			System.err.println("Conta Receber: \nReceber id: "+clienteReceber.getId()+"\nReceber conta ID: "+contaReceber.getId()+"\nReceber saldo total: "+contaReceber.getSaldoConta());
			
			
			Transferencia novaTransferencia = new Transferencia(clientePagador.getId(),clienteReceber.getId());
			
			List<Conta> contasTransferidas = novaTransferencia.transferirTed(contaPagador, valorTransferencia, contaReceber);
			
			if (contasTransferidas != null) {
				
				
				for(Conta contasT : contasTransferidas) {
					if(contasT.getId() == contaPagador.getId()) {
						
						if(contasT.getTipoConta() == TipoConta.CORRENTE) {
							
							ContaCorrente minhaContaCorrente = (ContaCorrente)contaPagador;
							minhaContaCorrente.getTransferencia().add(novaTransferencia);
							
							contaRepository.save(minhaContaCorrente);
							
						}
						
						if(contasT.getTipoConta() == TipoConta.POUPANCA) {
							
							ContaPoupanca minhaContaPoupanca = (ContaPoupanca)contaPagador;
							minhaContaPoupanca.getTransferencia().add(novaTransferencia);
							
							contaRepository.save(minhaContaPoupanca);
						}
						
					}
				}
				
//				for(Conta contasT : contasTransferidas) {
//					//Cliente Pagar
//					if(contasT.getId() == contaPagador.getId()) {
//					
//						if(contasT.getTipoConta() == TipoConta.CORRENTE) {
//							
//							ContaCorrente minhaContaCorrente = (ContaCorrente)contaPagador;
//							minhaContaCorrente.getTransferencia().add(novaTransferencia);
//							minhaContaCorrente.setSaldoConta(contasT.getSaldoConta());
//							minhaContaCorrente.setCategoriaConta(contasT.getCategoriaConta());
//							minhaContaCorrente.setTipoConta(contasT.getTipoConta());
//							minhaContaCorrente.setTaxas(null);
//							
//							System.err.println("Minha conta Corrente Pagador: "+minhaContaCorrente);
//							
//							contaRepository.save(minhaContaCorrente);
//							
//						}
//						
//						if(contasT.getTipoConta() == TipoConta.POUPANCA) {
//							
//							ContaPoupanca minhaContaPoupanca = (ContaPoupanca)contaPagador;
//							minhaContaPoupanca.getTransferencia().add(novaTransferencia);
//							minhaContaPoupanca.setSaldoConta(contasT.getSaldoConta());
//							minhaContaPoupanca.setCategoriaConta(contasT.getCategoriaConta());
//							minhaContaPoupanca.setTipoConta(contasT.getTipoConta());
//							minhaContaPoupanca.setTaxas(null);
//							
//							System.err.println("Minha conta poupanca Pagador: "+minhaContaPoupanca);
//							
//							contaRepository.save(minhaContaPoupanca);
//							
//						}
//						
//						contaRepository.save(contaPagador);
//					}
//					
//					
//					//Cliente Receber
//					if(contasT.getId() == contaReceber.getId()) {
//						
//						if(contasT.getTipoConta() == TipoConta.CORRENTE) {
//							
//							ContaCorrente minhaContaCorrente = (ContaCorrente)contaReceber;
//						
//							minhaContaCorrente.setSaldoConta(contasT.getSaldoConta());
//							minhaContaCorrente.setCategoriaConta(contasT.getCategoriaConta());
//							minhaContaCorrente.setTipoConta(contasT.getTipoConta());
//							minhaContaCorrente.setTaxas(null);
//							
//							System.err.println("Minha conta Corrente Recebedor: "+minhaContaCorrente);
//							
//							contaRepository.save(minhaContaCorrente);
//							
//						}
//						
//						if(contasT.getTipoConta() == TipoConta.POUPANCA) {
//							
//							ContaPoupanca minhaContaPoupanca = (ContaPoupanca)contaReceber;
//						
//							minhaContaPoupanca.setSaldoConta(contasT.getSaldoConta());
//							minhaContaPoupanca.setCategoriaConta(contasT.getCategoriaConta());
//							minhaContaPoupanca.setTipoConta(contasT.getTipoConta());
//							minhaContaPoupanca.setTaxas(null);
//							
//							System.err.println("Minha conta Poupcança Recebedor: "+minhaContaPoupanca);
//							
//							contaRepository.save(minhaContaPoupanca);
//							
//						}						
//					}
//					
//					
//				}
				
			}

				
			} else {
				throw new ContaNaoRealizouTransferenciaException("O cliente para o qual você está tentando transferir não tem essa conta. Confirme os seus dados.");
			}			
			
		} else {
			throw new ContaNaoRealizouTransferenciaException("A transferência não foi realizada. Confirme os seus dados.");
		}
		
		return true;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public float exibirSaldo(Long idConta) {

		Optional<Conta> contaVerSaldo = contaRepository.findById(idConta);

		float exibirSaldo = 0;

		if (contaVerSaldo.isPresent()) {

			Conta exibirSaldoConta = contaVerSaldo.get();

			Transferencia saldoAtual = new Transferencia();
			exibirSaldo = saldoAtual.exibirSaldo(exibirSaldoConta);

		} else {
			throw new ContaExibirSaldoErroException(
					"Não foi possível exibir os dados da conta no momento. Tente mais tarde.");
		}

		return exibirSaldo;
	}

	// Outros métodos
	public String gerarNumeroDaConta(Conta conta) {

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

	public String atualizarNumeroDaConta(String numeroConta) {

		String doisUltimosDigitos = null;
		String novoNumConta = null;

		if (numeroConta.length() > 2) {

			doisUltimosDigitos = numeroConta.substring(numeroConta.length() - 2);

			if (numeroConta.equalsIgnoreCase("CC")) {

				novoNumConta = numeroConta.replaceAll("CC", "PP");

			} else if (numeroConta.equalsIgnoreCase("PP")) {

				novoNumConta = numeroConta.replaceAll("PP", "CC");

			} else {
				throw new ContaNaoFoiPossivelAlterarNumeroException(
						"Não foi possível alterar o número da conta no momento.");
			}

		} else {
			doisUltimosDigitos = null;
		}

		return novoNumConta;
	}	

}
