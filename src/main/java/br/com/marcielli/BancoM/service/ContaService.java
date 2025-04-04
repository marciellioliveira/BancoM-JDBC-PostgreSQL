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
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.ContaRepositoy;

@Service
public class ContaService {

	@Autowired
	private ContaRepositoy contaRepository;
	
//	@Autowired
//	private ClienteRepository clienteRepository;
	
	@Transactional
	public Conta save(Conta conta) {
		
		//Validar dados
		
		String numConta = gerarNumeroDaConta(conta);
		CategoriaConta categoriaConta = null;
		Cliente novoCliente = null;
		
//		if(conta.getCliente() != null) {
//			novoCliente = clienteRepository.save(conta.getCliente());
//		}
		
		
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
		
		return null;
		
	}
	
	public List<Conta> getAll(){
		
		return contaRepository.findAll();
	}
	
	public Optional<Conta> getClienteById(Conta conta){	
		
		return contaRepository.findById(conta.getId());
	}
	
	public String delete(Long contaId) {
		
		contaRepository.deleteById(contaId);
		
		return "DELETED";
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
