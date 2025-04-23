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
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.enuns.TipoConta;
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
		Integer userId = null;
		TaxaManutencao taxa = new TaxaManutencao(dto.saldoConta(), dto.tipoConta());
		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxa);
		
		String numeroConta =  gerarNumeroDaConta();
		String numeroPix = gerarPixAleatorio();
		String novoPix = numeroPix.concat("-PIX");
		
		Conta conta = null;
		
		try {
			userId = Integer.parseInt(token.getName());
		} catch (NumberFormatException e) {			
			System.out.println("ID inválido no token: " + token.getName());
		}
		
		var user = userRepository.findById(userId);
		
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
		conta.setCliente(user.get().getCliente());
		conta.setTipoConta(dto.tipoConta());
		conta.setSaldoConta(dto.saldoConta());
		conta.setStatus(true);
		
		contaRepository.save(conta);
		
		return conta;
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
