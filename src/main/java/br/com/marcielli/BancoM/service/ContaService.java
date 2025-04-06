package br.com.marcielli.BancoM.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaFactory;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
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

	@Transactional
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

	@Transactional
	public Conta update(Conta contaComDadosParaAtualizar, Long idContaParaAtualizar) {

		Optional<Conta> contaH2ComDados = contaRepository.findById(contaComDadosParaAtualizar.getId());
		
		Optional<Conta> contaH2ParaAtualizar = contaRepository.findById(idContaParaAtualizar);
		
		
		
		Conta contaAtualizada = null;

		if (contaH2ComDados.isPresent() && contaH2ParaAtualizar.isPresent()) {

			Conta contaDadosParaInserir = contaH2ComDados.get();
			Conta contaParaAtualizar = contaH2ParaAtualizar.get();
			
			CategoriaConta categoriaConta = null;
			float taxaManutencaoMensalCC = 0;	
			float taxaAcrescRendPP1 = 0;
			float taxaMensalPP2 = 0;			
			
			String numeroDaContaAtual = contaParaAtualizar.getNumeroConta();
			String numeroDaContaNovo = atualizarNumeroDaConta(numeroDaContaAtual);

			if(contaDadosParaInserir.getSaldoConta() <= 1000) {
				
				//Conta Corrente
				taxaManutencaoMensalCC = 12.00f;
				
				//Todas
				categoriaConta = CategoriaConta.COMUM;
				
				//Conta Poupança
				taxaAcrescRendPP1 = 0.005f;	
				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);
			}
			
			if(contaDadosParaInserir.getSaldoConta() > 1000 && contaDadosParaInserir.getSaldoConta() <= 5000) {
				
				//Conta Corrente
				taxaManutencaoMensalCC = 8.00f;
				
				//Todas
				categoriaConta = CategoriaConta.SUPER;
				
				//Conta Poupança
				taxaAcrescRendPP1 = 0.007f;
				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);
			}
			
			if(contaDadosParaInserir.getSaldoConta() > 5000) {
				
				//Conta Corrente
				taxaManutencaoMensalCC = 0f;	
				
				//Todas
				categoriaConta = CategoriaConta.PREMIUM;
				
				//Conta Poupança
				taxaAcrescRendPP1 = 0.009f;	
				taxaMensalPP2 = (float) (Math.pow(1+taxaAcrescRendPP1, 1.0/12) - 1);				
			}	
			
			
			contaParaAtualizar.setCliente(contaDadosParaInserir.getCliente());
			contaParaAtualizar.setTipoConta(contaDadosParaInserir.getTipoConta());
			contaParaAtualizar.setSaldoConta(contaDadosParaInserir.getSaldoConta());
			contaParaAtualizar.setCategoriaConta(categoriaConta);
			contaParaAtualizar.setNumeroConta(numeroDaContaNovo);
			
			
			return contaAtualizada;

		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser atualizada porque não existe no banco.");
		}
	}

	public List<Conta> getAll() {

		List<Conta> contasH2 = contaRepository.findAll();

		if (contasH2.size() <= 0) {
			throw new ContaNaoEncontradaException("Não existem contas cadastradas no banco.");
		}

		return contasH2;
	}

	public Optional<Conta> getContaById(Long id) {

		Optional<Conta> contaH2 = contaRepository.findById(id);

		if (!contaH2.isPresent()) {
			throw new ContaNaoEncontradaException("Conta não encontrada.");
		}

		return contaH2;
	}

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
	@Transactional
	public boolean transferirTED(Long idReceber, Conta contaEnviar) {

		Optional<Conta> contaReceber = contaRepository.findById(idReceber);
		Optional<Conta> contaPagar = contaRepository.findById(contaEnviar.getId());

		float valorTransferencia = contaEnviar.getValorTransferencia();
		

		if (contaReceber.isPresent() && contaPagar.isPresent()) {

			Conta recebedor = contaReceber.get();
			Conta pagador = contaPagar.get();
			
			Transferencia novaTransferencia = new Transferencia();
			
			if (novaTransferencia.transferirTed(pagador, valorTransferencia, recebedor)) {
				recebedor.getTransferencia().add(novaTransferencia);
				pagador.getTransferencia().add(novaTransferencia);

				contaRepository.save(pagador);
				contaRepository.save(recebedor);

			} else {
				throw new ContaNaoRealizouTransferenciaException(
						"A transferência não foi realizada. Confirme os seus dados.");
			}

		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"A transferência não foi realizada. Confirme os seus dados.");
		}
		return true;
	}

	@Transactional
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
