package br.com.marcielli.BancoM.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.CartaoCredito;
import br.com.marcielli.BancoM.entity.CartaoDebito;
import br.com.marcielli.BancoM.entity.CartaoFactory;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.Taxas;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.ContaNaoRealizouTransferenciaException;
import br.com.marcielli.BancoM.repository.CartaoRepository;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.ContaRepositoy;

@Service
public class CartaoService {
	
	@Autowired
	private CartaoRepository cartaoRepository;
	
	@Autowired
	private ClienteRepository clienteRepository;
	
	@Autowired
	private ContaRepositoy contaRepository;	
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao saveCartao(Cartao dadosCartao) {		

		Cartao cartaoCriado = null;
		Cliente clienteCartao = null;
		
		if (dadosCartao.getId() == null ) {
			throw new CartaoNaoEncontradoException("Para cadastrar um cartão, você precisa ter uma conta no banco.");
		}
		
		Optional<Conta> conta = contaRepository.findById(dadosCartao.getConta().getId());
		
		if (conta.isPresent()) {	
			
			Conta contaCartao = conta.get();
			
			clienteCartao = contaCartao.getCliente();
			 
			String numCartao = gerarNumeroDoCartao();		
			
			if (dadosCartao.getTipoCartao() == TipoCartao.CREDITO) {
				
				String numCartaoCredito = numCartao.concat("-CC");		
				
				cartaoCriado =  new CartaoCredito(numCartaoCredito, contaCartao.getTipoConta(), contaCartao.getCategoriaConta(), TipoCartao.CREDITO, true, dadosCartao.getSenha(), contaCartao);
			
			} else if (dadosCartao.getTipoCartao() == TipoCartao.DEBITO) {
				
				String numCartaoDebito = numCartao.concat("-CD");
				cartaoCriado = new CartaoDebito(numCartaoDebito, contaCartao.getTipoConta(), contaCartao.getCategoriaConta(), TipoCartao.DEBITO, true, dadosCartao.getSenha(), contaCartao);
			}
			
			if(cartaoCriado != null) {				
				
				contaCartao.getCartoes().add(cartaoCriado);				
				cartaoRepository.save(cartaoCriado);
			}
			
		} else {
			throw new CartaoNaoEncontradoException("Para cadastrar um cartão, você precisa ter uma conta no banco.");
		}

		return cartaoCriado;
	}
	
	private static String gerarNumeroDoCartao() {

		int[] sequencia = new int[8];
		Random random = new Random();
		String meuCartao = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			meuCartao += Integer.toString(sequencia[i]);
		}

		return meuCartao;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Cartao> getAll() {

		List<Cartao> cartoesH2 = cartaoRepository.findAll();

		if (cartoesH2.size() <= 0) {
			throw new ContaNaoEncontradaException("Não existem cartões cadastrados no banco.");
		}

		return cartoesH2;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Cartao> getCartaoById(Long id) {

		Optional<Cartao> cartaoH2 = cartaoRepository.findById(id);

		if (!cartaoH2.isPresent()) {
			throw new ContaNaoEncontradaException("Cartão não encontrado.");
		}

		return cartaoH2;
	}
//	
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean deleteCartao(Long clienteId, Long contaId) {
//
//		Optional<Cliente> clienteH2 = clienteRepository.findById(clienteId);
//		Optional<Cartao> cartoaH2 = cartaoRepository.findById(contaId);
//
//		if (clienteH2.isPresent() && cartoaH2.isPresent()) {
//			
//				Cliente clienteConta = clienteH2.get();
//				Cartao contaCliente = cartoaH2.get();
//
//				for(Conta clienteTemConta : clienteConta.getContas()) {
//					if(clienteTemConta.getId() == contaCliente.getId()) {
//						
//						clienteConta.getContas().remove(clienteTemConta);						
//						cartaoRepository.deleteById(contaCliente.getId());
//						break;
//						
//					} 
//				}
//		} else {
//			
//			throw new ContaNaoEncontradaException("O cartão não pode ser deletado porque não existe no banco.");
//			
//		}
//
//		return true;
//
//	}

	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao update(Long cartaoAtualizar, Cartao dadosParaAtualizar) {

		Cartao cartaoA = null;
		Optional<Cartao> cartaoParaAtualizar = cartaoRepository.findById(cartaoAtualizar);
		
		Optional<Conta> contaCartao = contaRepository.findById(dadosParaAtualizar.getConta().getId());
	
		if (cartaoParaAtualizar.isPresent()) {
			
			cartaoA = cartaoParaAtualizar.get();			
			
			String numCartao = gerarNumeroDoCartao();		
			
			if (dadosParaAtualizar.getTipoCartao() == TipoCartao.CREDITO) {
				
				String numCartaoCredito = numCartao.concat("-CC");		
				
				cartaoA.setTipoCartao(TipoCartao.CREDITO);
				cartaoA.setNumeroCartao(numCartaoCredito);
				cartaoA.setSenha(cartaoA.getSenha());
			
			} else if (dadosParaAtualizar.getTipoCartao() == TipoCartao.DEBITO) {
				
				String numCartaoDebito = numCartao.concat("-CD");
				cartaoA.setTipoCartao(TipoCartao.DEBITO);
				cartaoA.setNumeroCartao(numCartaoDebito);
				cartaoA.setSenha(cartaoA.getSenha());			
			}
			
			if(cartaoA != null) {				
				
				Conta contaA = contaCartao.get();
				contaA.getCartoes().add(cartaoA);				
				cartaoRepository.save(cartaoA);
			}

		} else {
			throw new ContaNaoEncontradaException("O cartão não pode ser atualizado porque não existe no banco.");
		}
		
		return cartaoA;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean deleteCartao(Cartao cartaoDeletar, Long cartaoId) {

		Optional<Cliente> clienteH2 = clienteRepository.findById(cartaoDeletar.getId());
		Optional<Conta> contaH2 = contaRepository.findById(cartaoDeletar.getConta().getId());
		Optional<Cartao> cartaoH2 = cartaoRepository.findById(cartaoId);

		if (clienteH2.isPresent() && contaH2.isPresent() && cartaoH2.isPresent()) {
			
				Cliente clienteCartao = clienteH2.get();
				Conta contaCartao = contaH2.get();
				Cartao cartaoCliente = cartaoH2.get();
				
				for(Cartao cartaoClienteExiste : contaCartao.getCartoes()) {
					
					if(cartaoClienteExiste.getConta().getCliente().getId() == clienteCartao.getId() && cartaoClienteExiste.getConta().getId() == contaCartao.getId() && cartaoClienteExiste.getId() == cartaoCliente.getId()) {
						contaCartao.getCartoes().remove(cartaoClienteExiste);
						cartaoRepository.deleteById(cartaoId);
						break;
					}
				}
		
		} else {
			
			throw new ContaNaoEncontradaException("O cartão não pode ser deletado porque não existe no banco.");
			
		}

		return true;

	}
	
	
	
	//Pagamento Cartão
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean pagamentoCartao(Long idClienteReceber, Long idContaReceber, Transferencia dadosContaEnviar) {
		
		if(idClienteReceber == null || idContaReceber == null) {
			throw new ContaNaoRealizouTransferenciaException(
					"O pagamento não foi realizado. Confirme os seus dados.");
		}
				
		//Param
		Optional<Cliente> encontrarClienteRecebedorPorId = clienteRepository.findById(idClienteReceber);

		Conta contaReceber = null;
//		float valorDeposito = dadosContaEnviar.getValor();
//
//		if (valorDeposito <= 0) {
//			throw new ContaNaoRealizouTransferenciaException(
//					"O pagamento não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
//		}
		
		if(encontrarClienteRecebedorPorId.isPresent()) {
			
			Cliente clienteReceber = encontrarClienteRecebedorPorId.get();
			
			System.err.println("cliente "+clienteReceber.getContas());
			
//			for(Conta clienteExiste : clienteReceber.getContas()) {
//				if(clienteExiste.getId() == idContaReceber) {
//					contaReceber = clienteExiste;
//					break;
//				}
//			}
//			
//			System.err.println("Cliente receber: "+encontrarClienteRecebedorPorId);
//			System.err.println("Conta Receber: "+contaReceber.getId());
//			
//			
//			
//			
//			
//			if(contaReceber.isStatus() == false) {
//				throw new ContaNaoRealizouTransferenciaException(
//						"A conta foi desativada.  Confirme os seus dados e faça o depósito em uma conta ativa.");
//			}
			
//			boolean clienteTemConta = false;
//			for(Conta contas : clienteReceber.getContas()) {
//				if(contas.getId() == contaReceber.getId()) {
//					clienteTemConta = true;
//				}
//			}
//			
//			if(clienteTemConta) {
//				
//				Transferencia novoDeposito = new Transferencia();
//
//				List<Conta> contasDeposito = novoDeposito.depositar(valorDeposito, contaReceber);
//
//				if (contasDeposito != null) {
//					
//					for (Conta contasT : contasDeposito) {
//						
//						// Recebedor
//						if (contasT.getId() == contaReceber.getId()) {
//
//							if (contasT.getTipoConta() == TipoConta.CORRENTE) {
//
//								ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
//								minhaContaCorrente.getTransferencia().add(novoDeposito);
//								contaRepository.save(minhaContaCorrente);
//
//							}
//
//							if (contasT.getTipoConta() == TipoConta.POUPANCA) {
//
//								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
//								minhaContaPoupanca.getTransferencia().add(novoDeposito);
//								contaRepository.save(minhaContaPoupanca);
//							}
//
//						}
//
//					}
//					
//				}
//				
//			}
//			
			
			
			
		} else {
			throw new ContaNaoRealizouTransferenciaException(
					"O pagamento não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
		}

		
		return true;
		
	}
	
	
	
	
	
	

}
