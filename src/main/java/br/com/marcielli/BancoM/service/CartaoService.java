package br.com.marcielli.BancoM.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.CartaoFactory;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.repository.CartaoRepository;
import br.com.marcielli.BancoM.repository.ClienteRepository;

@Service
public class CartaoService {
	
	@Autowired
	private CartaoRepository cartaoRepository;
	
	@Autowired
	private ClienteRepository clienteRepository;
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao saveCartao(Long idConta, Cartao dadosCartao) {

		Cartao cartaoCriado = null;
		Conta contaDoCartao = null;
		
		if (dadosCartao.getId() == null) {
			throw new CartaoNaoEncontradoException("Para cadastrar um cartão, você precisa ter uma conta no banco.");
		}

		// Buscar cliente por ID
		Optional<Cliente> buscarClientePorId = clienteRepository.findById(dadosCartao.getId());		
	
		if (buscarClientePorId.isPresent()) {

			Cliente simClienteExiste = buscarClientePorId.get();
			
			for(Conta contaExiste : simClienteExiste.getContas()) {
				if(contaExiste.getId().equals(idConta)) {
					contaDoCartao = contaExiste;
					break;
				}
			}
			
			cartaoCriado = CartaoFactory.criarCartao(contaDoCartao, dadosCartao);
			
			//System.err.println("Cartão criado: "+cartaoCriado);

			if (cartaoCriado != null) {
				
				contaDoCartao.getCartoes().add(cartaoCriado);	
				//cartaoRepository.save(cartaoCriado);
				cartaoRepository.save(cartaoCriado);
				
				//contaRepository.save(contaDoCartao);
				
			}

		} else {
			throw new CartaoNaoEncontradoException("Para cadastrar um cartão, você precisa ter uma conta no banco.");
		}

		return cartaoCriado;
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
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean deleteCartao(Long clienteId, Long contaId) {

		Optional<Cliente> clienteH2 = clienteRepository.findById(clienteId);
		Optional<Cartao> cartoaH2 = cartaoRepository.findById(contaId);

		if (clienteH2.isPresent() && cartoaH2.isPresent()) {
			
				Cliente clienteConta = clienteH2.get();
				Cartao contaCliente = cartoaH2.get();

				for(Conta clienteTemConta : clienteConta.getContas()) {
					if(clienteTemConta.getId() == contaCliente.getId()) {
						
						clienteConta.getContas().remove(clienteTemConta);						
						cartaoRepository.deleteById(contaCliente.getId());
						//contaCliente.setStatus(false);
						break;
						
					} 
				}
		} else {
			
			throw new ContaNaoEncontradaException("O cartão não pode ser deletado porque não existe no banco.");
			
		}

		return true;

	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Cartao> getContaById(Long id) {

		Optional<Cartao> cartaoH2 = cartaoRepository.findById(id);

		if (!cartaoH2.isPresent()) {
			throw new ContaNaoEncontradaException("Cartão não encontrado.");
		}

		return cartaoH2;
	}
	
	
	
	
	
	
	
	
	

}
