package br.com.marcielli.BancoM.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.ContaTipoContaNaoExisteException;
import br.com.marcielli.BancoM.repository.ContaRepositoy;

@Service
public class ContaService {

	@Autowired
	private ContaRepositoy contaRepository;
	
	@Transactional
	public Conta save(Conta conta) {
		
		//Validar dados
		if(conta.getSaldoConta() < 0) {
			throw new ContaTipoContaNaoExisteException("O saldo inicial da conta precisa ser positivo");
		}
		
		String numConta = gerarNumeroDaConta(conta);
		CategoriaConta categoriaConta = null;
		Cliente novoCliente = null;

		if(conta.getTipoConta().getDescricao().equalsIgnoreCase("CORRENTE")) {
			
			String numContaCorrente = numConta.concat("-CC");

			Conta contaCorrente = new ContaCorrente(conta.getSaldoConta());
			contaCorrente.setTipoConta(conta.getTipoConta());
			contaCorrente.setCliente(conta.getCliente());
			contaCorrente.setNumeroConta(numContaCorrente);
			contaCorrente.setCategoriaConta(conta.getSaldoConta());
			contaCorrente.setSaldoConta(conta.getSaldoConta());
			
			return contaRepository.save(contaCorrente);
			
		} else if(conta.getTipoConta().getDescricao().equalsIgnoreCase("POUPANCA")) {
			
			String numContaPoupanca = numConta.concat("-PP");
			
			Conta contaPoupanca = new ContaPoupanca(conta.getSaldoConta());
			
			contaPoupanca.setTipoConta(conta.getTipoConta());
			contaPoupanca.setCliente(conta.getCliente());
			contaPoupanca.setNumeroConta(numContaPoupanca);
			contaPoupanca.setCategoriaConta(conta.getSaldoConta());
			contaPoupanca.setSaldoConta(conta.getSaldoConta());
			
			return contaRepository.save(contaPoupanca);
			
		}
		
		return null;
	}
	
	public Conta update(Conta conta, Long id) {
		
		Optional<Conta> contaH2 = contaRepository.findById(id);
		
		Conta contaAtualizada = null;
		
		if(contaH2.isPresent()) {
			
			if(conta.getTipoConta().getDescricao().equalsIgnoreCase("corrente")) {
				
				if(contaH2.isPresent()) {
					contaAtualizada = contaH2.get();
					contaAtualizada.setTipoConta(conta.getTipoConta());
					contaAtualizada.setCliente(conta.getCliente());
					contaAtualizada.setSaldoConta(conta.getSaldoConta());
					contaAtualizada.setCategoriaConta(conta.getSaldoConta());
					
					return contaRepository.save(contaAtualizada);
				}
				
				
			} else if(conta.getTipoConta().getDescricao().equalsIgnoreCase("poupanca")) {
				
				if(contaH2.isPresent()) {
					contaAtualizada = contaH2.get();
					contaAtualizada.setTipoConta(conta.getTipoConta());
					contaAtualizada.setCliente(conta.getCliente());
					contaAtualizada.setSaldoConta(conta.getSaldoConta());
					contaAtualizada.setCategoriaConta(conta.getSaldoConta());

					return contaRepository.save(contaAtualizada);
				}
				
			}	
			
		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser atualizada porque não existe no banco.");
		}
		return contaAtualizada;		
	}
	
	public List<Conta> getAll(){
		
		List<Conta> contasH2 = contaRepository.findAll();
		
		if(contasH2.size() <= 0) {
			throw new ContaNaoEncontradaException("Não existemn contas cadastradas no banco.");
		}
		
		return contasH2;
		//return contaRepository.findAll();
	}
	
	public Optional<Conta> getClienteById(Conta conta){	
		
		Optional<Conta> contaH2 = contaRepository.findById(conta.getId());
		
		if(!contaH2.isPresent()) {
			throw new ContaNaoEncontradaException("Conta não encontrada.");
		}
		
		return contaH2;
		//return contaRepository.findById(conta.getId());
	}
	
	public String delete(Long contaId) {
		
		//Optional<Conta> contaH2 = contaRepository.deleteById(contaId);
		Optional<Conta> contaH2 = contaRepository.findById(contaId);
		
		if(contaH2.isPresent()) {
			contaRepository.deleteById(contaId);
			return "deletado";
		} else {
			throw new ContaNaoEncontradaException("A conta não pode ser deletada porque não existe no banco.");
		}
		
		//contaRepository.deleteById(contaId);
		
	//	return "DELETED";
	}
	
	
	
	//Outros métodos
	
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
	

	
	
	
}
