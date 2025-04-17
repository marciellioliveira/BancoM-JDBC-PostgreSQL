package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marcielli.BancoM.dto.CartaoConsultarFaturaDTO;
import br.com.marcielli.BancoM.dto.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.CartaoCreateTedDTO;
import br.com.marcielli.BancoM.dto.CartaoDeleteDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateLimiteDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateSenhaDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateStatusDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.CartaoCredito;
import br.com.marcielli.BancoM.entity.CartaoDebito;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.Fatura;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.enuns.TipoTransferencia;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExisteNoBancoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.TransferenciaNaoRealizadaException;
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
	
	
	private BigDecimal limiteCredito = new BigDecimal("600");
	
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
			novoCartao.setStatus(true);	
			novoCartao.setTipoConta(conta.getTipoConta());
			conta.setCategoriaConta(conta.getCategoriaConta());
			
			if(novoCartao instanceof CartaoCredito cartaoCredito) {
				//((CartaoCredito) novoCartao).setLimiteCreditoPreAprovado(new BigDecimal("600"));
				cartaoCredito.setLimiteCreditoPreAprovado(limiteCredito);
			
			}
			
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
			novoCartao.setStatus(true);
			novoCartao.setTipoConta(conta.getTipoConta());
			conta.setCategoriaConta(conta.getCategoriaConta());
			
			if(novoCartao instanceof CartaoDebito cartaoDebito) {
				cartaoDebito.setLimiteDiarioTransacao(new BigDecimal("600"));
			}
			
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
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean pagCartao(Long idContaReceber, CartaoCreateTedDTO dto) {
		
		Cliente clienteOrigem = clienteRepository.findById(dto.getIdClienteOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cliente origem não existe."));
		
		Conta contaOrigem = contaRepository.findById(dto.getIdContaOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta origem não existe."));
		
		Cartao cartaoOrigem = cartaoRepository.findById(dto.getIdCartaoOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cartão origem não existe."));
		
		Conta contaDestino = contaRepository.findById(idContaReceber).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta destino não existe."));	
		
		Optional<Cliente> clienteDestino = clienteRepository.findById(contaDestino.getCliente().getId());
		
		
		for(Conta contasDoClienteOrigem : clienteOrigem.getContas()) {
			
			if(contasDoClienteOrigem.getId() == contaOrigem.getId()) {	
				
				
				if(cartaoOrigem instanceof CartaoCredito cartaoC) {
					if(dto.getValor().compareTo(cartaoC.getLimiteCreditoPreAprovado()) > 0) {
						throw new TransferenciaNaoRealizadaException("Você já utilizou o seu limite de crédito pré aprovado para envio.");
					}		
					
					if(cartaoC.getLimiteCreditoPreAprovado().compareTo(BigDecimal.ZERO) <= 0) {
						throw new TransferenciaNaoRealizadaException("O cartão não tem limite de crédito.");
					}
					
					cartaoC.atualizarTotalGastoMes(dto.getValor());
					cartaoC.atualizarLimiteCreditoPreAprovado(dto.getValor());
					
					//Já tem uma fatura associada?			
					Fatura faturaExistente = cartaoOrigem.getFatura();
					
					if(faturaExistente == null) {
						
						faturaExistente = new Fatura();
					
						faturaExistente.setCartao(cartaoOrigem);										
						cartaoOrigem.setFatura(faturaExistente);
					}
					
								
					Transferencia transferindo = new Transferencia(contaOrigem, dto.getValor(), contaDestino, TipoTransferencia.TED, cartaoOrigem.getTipoCartao());
					contaOrigem.getTransferencia().add(transferindo);
					
					transferindo.setFatura(faturaExistente);
					faturaExistente.getTransferenciasCredito().add(transferindo);
						
						cartaoRepository.save(cartaoC);
				}
				
				if(cartaoOrigem instanceof CartaoDebito cartaoD) {
					contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.getValor()));
					
					if(dto.getValor().compareTo(cartaoD.getLimiteDiarioTransacao()) > 0) {
						throw new TransferenciaNaoRealizadaException("Você já utilizou o seu limite de crédito pré aprovado para envio.");
					}		
					
					if(cartaoD.getLimiteDiarioTransacao().compareTo(BigDecimal.ZERO) <= 0) {
						throw new TransferenciaNaoRealizadaException("O cartão não tem limite de transação.");
					}
					
					cartaoD.atualizarLimiteDiarioTransacao(dto.getValor());
					cartaoD.atualizarTotalGastoMes(dto.getValor());
				
					TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());

					List<TaxaManutencao> novaTaxa = new ArrayList<>();
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
					
					Transferencia transferindo = new Transferencia(contaOrigem, dto.getValor(), contaDestino, TipoTransferencia.TED, cartaoOrigem.getTipoCartao());
					contaOrigem.getTransferencia().add(transferindo);
					
					contaRepository.save(contaOrigem);
				}
				
				break;
			
			}
		}	
		
		if(clienteDestino.isPresent()) {
			
			Cliente cdestino = clienteDestino.get();
			
			for(Conta contasDoClienteDestino : cdestino.getContas()) {
				
				if(contasDoClienteDestino.getId() == contaDestino.getId()) {
					
					contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.getValor()));
					
					TaxaManutencao taxaContaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());

					List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
					novaTaxa.add(taxaContaDestino);
					
					contaDestino.setTaxas(novaTaxa);
					contaDestino.setCategoriaConta(taxaContaDestino.getCategoria());
					
					contaRepository.save(contaDestino);
					break;
				
				}			
			}
			
		}
		return true;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarLimiteCartaoCredito(Long cartaoId, CartaoUpdateLimiteDTO dto) {	
		
		Cliente cliente = clienteRepository.findById(dto.getIdCliente())
				.orElseThrow(() -> new ContaExisteNoBancoException("O cliente não existe no banco."));
		
		Conta conta = contaRepository.findById(dto.getIdConta())
				.orElseThrow(() -> new ContaExisteNoBancoException("A conta não existe no banco."));
		
		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		if(dto.getNovoLimite() == null) {
			throw new CartaoNaoEncontradoException("Você precisa digitar um valor para o novo limite do cartão");
		}
		
		BigDecimal novoLimite = dto.getNovoLimite();
		
		for(Conta temConta : cliente.getContas()) {
			
			if(temConta.getId() == conta.getId()) {
				for(Cartao temCartao : conta.getCartoes()) {
					if(temCartao.getId() == cartaoId) {
					
						if(temCartao instanceof CartaoCredito cartaoCredito) {
							cartaoCredito.alterarLimiteCreditoPreAprovado(novoLimite);
						//	cartaoCredito.getFatura().setLimiteCredito(novoLimite);
							return cartaoRepository.save(temCartao);
							
						}	
					}
				}
				
				
			}			
		}
		//return cartao;
		return null;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarStatusC(Long cartaoId, CartaoUpdateStatusDTO dto) {	
		
		Cliente cliente = clienteRepository.findById(dto.getIdCliente())
				.orElseThrow(() -> new ContaExisteNoBancoException("O cliente não existe no banco."));
		
		Conta conta = contaRepository.findById(dto.getIdConta())
				.orElseThrow(() -> new ContaExisteNoBancoException("A conta não existe no banco."));
		
		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		if(!dto.getNovoStatus().equalsIgnoreCase("true") && !dto.getNovoStatus().equalsIgnoreCase("false") ) {
			throw new CartaoNaoEncontradoException("Digite (True ou False) para o status.");
		}
		
		String statusNovo = dto.getNovoStatus();
		
		for(Conta temConta : cliente.getContas()) {
			
			if(temConta.getId() == conta.getId()) {
				for(Cartao temCartao : conta.getCartoes()) {
					if(temCartao.getId() == cartaoId) {
						
						if(statusNovo.equalsIgnoreCase("true")) {
							cartao.setStatus(true);							
						} 

						if(statusNovo.equalsIgnoreCase("false")){
							cartao.setStatus(false);
						}
						
						cartaoRepository.save(cartao);
						break;
						
					}
				}
				
				
			}			
		}
		return cartao;
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarSenhaC(Long cartaoId, CartaoUpdateSenhaDTO dto) {	
		
		Cliente cliente = clienteRepository.findById(dto.getIdCliente())
				.orElseThrow(() -> new ContaExisteNoBancoException("O cliente não existe no banco."));
		
		Conta conta = contaRepository.findById(dto.getIdConta())
				.orElseThrow(() -> new ContaExisteNoBancoException("A conta não existe no banco."));
		
		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		String novaSenha = dto.getNovaSenha();
		
		for(Conta temConta : cliente.getContas()) {
			
			if(temConta.getId() == conta.getId()) {
				for(Cartao temCartao : conta.getCartoes()) {
					if(temCartao.getId() == cartaoId) {
						
						cartao.setSenha(novaSenha);
						
						cartaoRepository.save(cartao);
						break;
						
					}
				}
			}			
		}
		
		return cartao; //colocar null aqui e o return ali dentro da função pra ver se retorna ja com os dados
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarLimiteCartaoDebito(Long cartaoId, CartaoUpdateLimiteDTO dto) {	
		
		Cliente cliente = clienteRepository.findById(dto.getIdCliente())
				.orElseThrow(() -> new ContaExisteNoBancoException("O cliente não existe no banco."));
		
		Conta conta = contaRepository.findById(dto.getIdConta())
				.orElseThrow(() -> new ContaExisteNoBancoException("A conta não existe no banco."));
		
		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		if(dto.getNovoLimite() == null) {
			throw new CartaoNaoEncontradoException("Você precisa digitar um valor para o novo limite do cartão");
		}
		
		BigDecimal novoLimite = dto.getNovoLimite();

		
		for(Conta temConta : cliente.getContas()) {
			
			if(temConta.getId() == conta.getId()) {
				
				
				for(Cartao temCartao : conta.getCartoes()) {
					if(temCartao.getId() == cartaoId) {
										
						if(temCartao instanceof CartaoDebito cartaoDebito) {
							cartaoDebito.alterarLimiteDiarioTransacao(novoLimite);
						
							cartaoRepository.save(cartao);
							break;
						}
					}
				}
				
				
			}			
		}
		return cartao;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Fatura> getFaturaCartaoDeCreditoService(Long cartaoId) {
		
		
		return cartaoRepository.findById(cartaoId)
		        .filter(c -> c instanceof CartaoCredito)
		        .map(c -> ((CartaoCredito) c).getFatura());


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

}
