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
import br.com.marcielli.BancoM.dto.CartaoCreateTedDTO;
import br.com.marcielli.BancoM.dto.CartaoDeleteDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.CartaoCredito;
import br.com.marcielli.BancoM.entity.CartaoDebito;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.enuns.TipoTransferencia;
import br.com.marcielli.BancoM.exception.ContaExisteNoBancoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
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
		
		Cliente clienteDestino = clienteRepository.getById(contaDestino.getCliente().getId());
		
		
		for(Conta contasDoClienteOrigem : clienteOrigem.getContas()) {
			
			if(contasDoClienteOrigem.getId() == contaOrigem.getId()) {
			
				contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.getValor()));
				
				TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());

				List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
				novaTaxa.add(taxaContaOrigem);
				
				contaOrigem.setTaxas(novaTaxa);
				contaOrigem.setCategoriaConta(taxaContaOrigem.getCategoria());
				
				if(contaOrigem.getTipoConta() == TipoConta.CORRENTE) {
					ContaCorrente cc = (ContaCorrente)contaOrigem;
					cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
				}
				
				if(contaOrigem.getTipoConta() == TipoConta.POUPANCA) {
					ContaPoupanca cp = (ContaPoupanca)contaOrigem;
					cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
					cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());					
				}
							
				Transferencia transferindo = new Transferencia(contaOrigem, dto.getValor(), contaDestino, TipoTransferencia.TED, cartaoOrigem.getTipoCartao());
				contaOrigem.getTransferencia().add(transferindo);
	
				contaRepository.save(contaOrigem);		
				break;
			
			}
		}	
		
		
		for(Conta contasDoClienteDestino : clienteDestino.getContas()) {
			
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

}
