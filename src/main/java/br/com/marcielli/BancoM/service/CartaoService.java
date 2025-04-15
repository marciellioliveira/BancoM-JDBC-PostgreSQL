package br.com.marcielli.BancoM.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.CartaoDeleteDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.CartaoCredito;
import br.com.marcielli.BancoM.entity.CartaoDebito;
import br.com.marcielli.BancoM.entity.CartaoFactory;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExisteNoBancoException;
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
	public Cartao save(CartaoCreateDTO cartao) {		
		
		Cliente cliente = clienteRepository.findById(cartao.getIdCliente())
				.orElseThrow(() -> new ContaExisteNoBancoException("O cliente não existe no banco."));
		
		Conta conta = contaRepository.findById(cartao.getIdConta())
				.orElseThrow(() -> new ContaExisteNoBancoException("A conta não existe no banco."));
		
		Cartao novoCartao = null;
		List<Cartao> cartoes = new ArrayList<Cartao>();
		
		String numCartao = gerarNumeroDoCartao();
		
		if(cartao.getTipoCartao() == TipoCartao.CREDITO) {
			
			String numeroCartao = numCartao.concat("-CC");
			
			novoCartao = new CartaoCredito();
			
			novoCartao.setConta(conta);
			novoCartao.setTipoCartao(cartao.getTipoCartao());
			novoCartao.setSenha(cartao.getSenha());
			novoCartao.setNumeroCartao(numeroCartao);
			
			cartoes.add(novoCartao);	
			conta.setCartoes(cartoes);
			
		}
		
		if(cartao.getTipoCartao() == TipoCartao.DEBITO) {
			
			String numeroCartao = numCartao.concat("-CD");
			
			novoCartao = new CartaoDebito();		
			
			novoCartao.setConta(conta);
			novoCartao.setTipoCartao(cartao.getTipoCartao());
			novoCartao.setSenha(cartao.getSenha());
			novoCartao.setNumeroCartao(numeroCartao);
			
			cartoes.add(novoCartao);
			conta.setCartoes(cartoes);			
			
		}
		
		return cartaoRepository.save(novoCartao);

	}
		
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao update(Long cartaoId, CartaoUpdateDTO dto) {	
		
		Cliente cliente = clienteRepository.findById(dto.getIdCliente())
				.orElseThrow(() -> new ContaExisteNoBancoException("O cliente não existe no banco."));
		
		Conta conta = contaRepository.findById(dto.getIdConta())
				.orElseThrow(() -> new ContaExisteNoBancoException("A conta não existe no banco."));
		
		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		for(Conta temConta : cliente.getContas()) {
			
			if(temConta.getId() == conta.getId()) {
				for(Cartao temCartao : conta.getCartoes()) {
					if(temCartao.getId() == cartaoId) {
						cartao.setSenha(dto.getSenha());
					}
				}
				
				
			}			
		}
		return cartaoRepository.save(cartao);
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean deleteCartao(Long cartaoId, CartaoDeleteDTO dto) {	
	
		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		cartaoRepository.deleteById(cartao.getId());
		
		return true;

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// Outros métodos
	public String gerarNumeroDoCartao() {

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

	
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Cartao update(Long cartaoAtualizar, Cartao dadosParaAtualizar) {
//
//		Cartao cartaoA = null;
//		Optional<Cartao> cartaoParaAtualizar = cartaoRepository.findById(cartaoAtualizar);
//		
//		Optional<Conta> contaCartao = contaRepository.findById(dadosParaAtualizar.getConta().getId());
//	
//		if (cartaoParaAtualizar.isPresent()) {
//			
//			cartaoA = cartaoParaAtualizar.get();			
//			
//			String numCartao = gerarNumeroDoCartao();		
//			
//			if (dadosParaAtualizar.getTipoCartao() == TipoCartao.CREDITO) {
//				
//				String numCartaoCredito = numCartao.concat("-CC");		
//				
//				cartaoA.setTipoCartao(TipoCartao.CREDITO);
//				cartaoA.setNumeroCartao(numCartaoCredito);
//				cartaoA.setSenha(cartaoA.getSenha());
//			
//			} else if (dadosParaAtualizar.getTipoCartao() == TipoCartao.DEBITO) {
//				
//				String numCartaoDebito = numCartao.concat("-CD");
//				cartaoA.setTipoCartao(TipoCartao.DEBITO);
//				cartaoA.setNumeroCartao(numCartaoDebito);
//				cartaoA.setSenha(cartaoA.getSenha());			
//			}
//			
//			if(cartaoA != null) {				
//				
//				Conta contaA = contaCartao.get();
//				contaA.getCartoes().add(cartaoA);				
//				cartaoRepository.save(cartaoA);
//			}
//
//		} else {
//			throw new ContaNaoEncontradaException("O cartão não pode ser atualizado porque não existe no banco.");
//		}
//		
//		return cartaoA;
//	}
//	
//	
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean deleteCartao(Cartao cartaoDeletar, Long cartaoId) {
//
//		Optional<Cliente> clienteH2 = clienteRepository.findById(cartaoDeletar.getId());
//		Optional<Conta> contaH2 = contaRepository.findById(cartaoDeletar.getConta().getId());
//		Optional<Cartao> cartaoH2 = cartaoRepository.findById(cartaoId);
//
//		if (clienteH2.isPresent() && contaH2.isPresent() && cartaoH2.isPresent()) {
//			
//				Cliente clienteCartao = clienteH2.get();
//				Conta contaCartao = contaH2.get();
//				Cartao cartaoCliente = cartaoH2.get();
//				
//				for(Cartao cartaoClienteExiste : contaCartao.getCartoes()) {
//					
//					if(cartaoClienteExiste.getConta().getCliente().getId() == clienteCartao.getId() && cartaoClienteExiste.getConta().getId() == contaCartao.getId() && cartaoClienteExiste.getId() == cartaoCliente.getId()) {
//						contaCartao.getCartoes().remove(cartaoClienteExiste);
//						cartaoRepository.deleteById(cartaoId);
//						break;
//					}
//				}
//		
//		} else {
//			
//			throw new ContaNaoEncontradaException("O cartão não pode ser deletado porque não existe no banco.");			
//		}
//
//		return true;
//	}
//	
//	
//	// Pagamento Cartão	
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean pagarCartao(Long idPessoaReceber, Long idContaReceber, Transferencia dadosContaEnviar) {
//
//	if (idPessoaReceber == null || idContaReceber == null || dadosContaEnviar.getIdClienteOrigem() == null
//			|| dadosContaEnviar.getIdContaOrigem() == null) {
//		throw new ContaNaoRealizouTransferenciaException(
//				"O pagamento não foi realizado. Confirme os seus dados.");
//	}
//
//	// PathVariable
//	Optional<Cliente> encontraRecebedorPorId = clienteRepository.findById(idPessoaReceber);
//	Optional<Conta> encontraContaRecebedorPorId = contaRepository.findById(idContaReceber);
//
//	// RequestBody
//	Optional<Cliente> encontraPagadorPorId = clienteRepository.findById(dadosContaEnviar.getIdClienteOrigem());
//	Optional<Conta> encontraContaPagadorPorId = contaRepository.findById(dadosContaEnviar.getIdContaOrigem());
//
//	float valorTransferencia = dadosContaEnviar.getValor();
//	
//	//String numeroCartao = dadosContaEnviar.getNumeroCartao();
//
//	if (valorTransferencia <= 0) {
//		throw new ContaNaoRealizouTransferenciaException(
//				"O pagamento não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
//	}
//
//	if (encontraRecebedorPorId.isPresent() && encontraContaRecebedorPorId.isPresent()
//			&& encontraPagadorPorId.isPresent() && encontraContaPagadorPorId.isPresent()) {
//
//		Cliente clienteReceber = encontraRecebedorPorId.get();
//		Conta contaReceber = encontraContaRecebedorPorId.get();
//		
//		Cliente clientePagador = encontraPagadorPorId.get();
//		Conta contaPagador = encontraContaPagadorPorId.get();
//		
//		if(contaReceber.isStatus() == false || contaPagador.isStatus() == false ) {
//			throw new ContaNaoRealizouTransferenciaException("Esse cartão foi desativada e não pode receber ou enviar transferência. Tente utilizar uma conta válida.");
//		}
//		
//
//		if (clienteReceber.getId() != null && contaReceber != null) {
//
//			// Conta pagador -> Request Body (idContaOrigem) -> Conta Recebedor ->
//			// PathVariable (id)
//			
//			String numCartao = dadosContaEnviar.getNumeroCartao();
//			
//			Transferencia novaTransferencia = new Transferencia(contaPagador.getId(), contaReceber.getId());
//
//			List<Conta> contasTransferidas = novaTransferencia.pagarCartao(contaPagador, numCartao, valorTransferencia,
//					contaReceber);
//
//			if (contasTransferidas != null) {
//
//				for (Conta contasT : contasTransferidas) {
//
//					// Pagador
//					if (contasT.getId() == contaPagador.getId()) {
//
//						if (contasT.getTipoConta() == TipoConta.CORRENTE) {
//
//							ContaCorrente minhaContaCorrente = (ContaCorrente) contaPagador;
//							minhaContaCorrente.getTransferencia().add(novaTransferencia);
//							contaRepository.save(minhaContaCorrente);
//
//						}
//
//						if (contasT.getTipoConta() == TipoConta.POUPANCA) {
//
//							ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaPagador;
//							minhaContaPoupanca.getTransferencia().add(novaTransferencia);
//							contaRepository.save(minhaContaPoupanca);
//						}
//
//					}
//
//					// Recebedor
//					if (contasT.getId() == contaReceber.getId()) {
//
//						if (contasT.getTipoConta() == TipoConta.CORRENTE) {
//
//							ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
//							contaRepository.save(minhaContaCorrente);
//
//						}
//
//						if (contasT.getTipoConta() == TipoConta.POUPANCA) {
//
//							ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
//							contaRepository.save(minhaContaPoupanca);
//						}
//
//					}
//
//				}
//
//			}
//
//		} else {
//			throw new ContaNaoRealizouTransferenciaException(
//					"O cliente para o qual você está tentando transferir não tem essa conta. Confirme os seus dados.");
//		}
//
//	} else {
//		throw new ContaNaoRealizouTransferenciaException(
//				"O pagamento não foi realizado. Confirme os seus dados.");
//	}
//
//	return true;
//}
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public boolean pagarCartao(Long idClienteReceber, Long idContaReceber, Transferencia dadosContaEnviar) {
//		
//		if(idClienteReceber == null || idContaReceber == null) {
//			throw new ContaNaoRealizouTransferenciaException(
//					"O pagamento não foi realizado. Confirme os seus dados.");
//		}
//				
//		//Param
//		Optional<Cliente> encontrarClienteRecebedorPorId = clienteRepository.findById(idClienteReceber);
//		Optional<Conta> encontraContaRecebedorPorId = contaRepository.findById(idContaReceber);
//		
//		float valorDeposito = dadosContaEnviar.getValor();
//
//		if (valorDeposito <= 0) {
//			throw new ContaNaoRealizouTransferenciaException(
//					"O pagamento não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
//		}
//		
//		if( encontraContaRecebedorPorId.isPresent() && encontrarClienteRecebedorPorId.isPresent()) {
//			
//			Conta contaReceber = encontraContaRecebedorPorId.get();
//			Cliente clienteReceber = encontrarClienteRecebedorPorId.get();
//			
//			String numeroCartao = dadosContaEnviar.getNumeroCartao();
//			
//			if(contaReceber.isStatus() == false) {
//				throw new ContaNaoRealizouTransferenciaException(
//						"O cartão foi desativado.  Confirme os seus dados e faça o pagamento em uma conta ativa.");
//			}
//			
//			boolean clienteTemConta = false;
//			for(Conta contas : clienteReceber.getContas()) {
//				if(contas.getId() == contaReceber.getId()) {
//					clienteTemConta = true;
//				}
//			}
//			
//			if(clienteTemConta) {
//				
//				Transferencia novoPagamento = new Transferencia();
//
//				List<Conta> contasPagamento = novoPagamento.pagarCartao(valorDeposito, contaReceber);
//
//				if (contasPagamento != null) {
//					
//					for (Conta contasT : contasPagamento) {
//						
//						// Recebedor
//						if (contasT.getId() == contaReceber.getId()) {
//
//							if (contasT.getTipoConta() == TipoConta.CORRENTE) {
//
//								ContaCorrente minhaContaCorrente = (ContaCorrente) contaReceber;
//								minhaContaCorrente.getTransferencia().add(novoPagamento);
//								contaRepository.save(minhaContaCorrente);
//							}
//
//							if (contasT.getTipoConta() == TipoConta.POUPANCA) {
//
//								ContaPoupanca minhaContaPoupanca = (ContaPoupanca) contaReceber;
//								minhaContaPoupanca.getTransferencia().add(novoPagamento);
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
//		} else {
//			throw new ContaNaoRealizouTransferenciaException(
//					"O pagamento não foi realizado. Valor precisa ser maior que 0. Confirme os seus dados.");
//		}
//		
//		return true;
//		
//	}
}
