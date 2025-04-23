package br.com.marcielli.BancoM.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.security.ContaCreateDTO;
import br.com.marcielli.BancoM.dto.security.ContaUpdateDTO;
import br.com.marcielli.BancoM.dto.security.UserCreateDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.entity.ValidacaoUsuarioAtivo.ValidacaoUsuarioUtil;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.ClienteEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.repository.ContaRepositoy;
import br.com.marcielli.BancoM.repository.UserRepository;

@Service
public class UserContaService {
	
	private final ContaRepositoy contaRepository;
	private final UserRepository userRepository;

	public UserContaService(ContaRepositoy contaRepository, UserRepository userRepository) {
		this.contaRepository = contaRepository;
		this.userRepository = userRepository;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta save(ContaCreateDTO dto, JwtAuthenticationToken token) {
		
		//Receber o usuário que está logado e criar a conta desse usuário.
		TaxaManutencao taxa = new TaxaManutencao(dto.saldoConta(), dto.tipoConta());
		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxa);
		
		String numeroConta =  gerarNumeroDaConta();
		String numeroPix = gerarPixAleatorio();
		String novoPix = numeroPix.concat("-PIX");
		
		Conta conta = null;
		
		try {
			Integer userId = Integer.parseInt(token.getName());
			
			User user = userRepository.findById(userId)
				    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + userId));
			
			ValidacaoUsuarioUtil.verificarUsuarioAtivo(user);			
			
			if (dto.tipoConta() == TipoConta.CORRENTE) {
				
				conta = new ContaCorrente(taxa.getTaxaManutencaoMensal());
				conta.setTaxas(novaTaxa);
				String numContaCorrente = numeroConta.concat("-CC");
				conta.setNumeroConta(numContaCorrente);
				
			} else if (dto.tipoConta() == TipoConta.POUPANCA) {
				
				conta = new ContaPoupanca(taxa.getTaxaAcrescRend(), taxa.getTaxaMensal());
				conta.setTaxas(novaTaxa);
				String numContaPoupanca = numeroConta.concat("-PP");
				conta.setNumeroConta(numContaPoupanca);
			}
			
			conta.setPixAleatorio(novoPix);
			conta.setCategoriaConta(taxa.getCategoria());
			conta.setCliente(user.getCliente());
			conta.setTipoConta(dto.tipoConta());
			conta.setSaldoConta(dto.saldoConta());
			conta.setStatus(true);
			
		} catch (NumberFormatException e) {			
			System.out.println("ID inválido no token: " + token.getName());
		}
		
		contaRepository.save(conta);
		
		return conta;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta getContasById(Long id) {
		return contaRepository.findById(id).orElse(null);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta update(Long id, ContaUpdateDTO dto) {
		
		Conta contaExistente = contaRepository.findById(id).orElse(null);
		
		if (contaExistente == null) {
			 return null;
		}
		
		String novoPix = dto.pixAleatorio().concat("-PIX");
		contaExistente.setPixAleatorio(novoPix);		
		
		contaRepository.save(contaExistente);
		
		return contaExistente;
	
	}
	
	
	@Transactional
	public boolean delete(Long id) {
		
		Conta contaExistente = contaRepository.findById(id).orElse(null);
		
		boolean isAdmin = contaExistente.getCliente().getUser().getRoles().stream()
			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
	
		if(isAdmin) {
			throw new ClienteNaoEncontradoException("Não é possível deletar a conta administradora do sistema.");
		}
		
		contaExistente.setStatus(false);
		
	    return true;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
