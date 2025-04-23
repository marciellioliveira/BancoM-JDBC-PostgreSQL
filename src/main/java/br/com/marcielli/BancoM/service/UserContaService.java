package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.security.ContaCreateDTO;
import br.com.marcielli.BancoM.dto.security.ContaUpdateDTO;
import br.com.marcielli.BancoM.dto.security.UserContaDepositoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaPixDTO;
import br.com.marcielli.BancoM.dto.security.UserContaRendimentoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaSaqueDTO;
import br.com.marcielli.BancoM.dto.security.UserContaTaxaManutencaoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaTedDTO;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.entity.ValidacaoUsuarioAtivo.ValidacaoUsuarioUtil;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.enuns.TipoTransferencia;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExibirSaldoErroException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
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
			contaRepository.save(conta);
			
		} catch (NumberFormatException e) {			
			System.out.println("ID inválido no token: " + token.getName());
		}
		
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
		contaExistente.setStatus(true);
		
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
	
	
	
	//Transferências
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirTED(Long idContaReceber, UserContaTedDTO dto) {

		Conta contaOrigem = contaRepository.findById(dto.idContaOrigem())
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta origem não existe."));

		Conta contaDestino = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta destino não existe."));

		// Verificar se o saldo da conta origem é suficiente para a transferência
		if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
			throw new ContaExibirSaldoErroException("Saldo insuficiente na conta origem.");
		}

		List<Conta> contasTransferidas = new ArrayList<Conta>();

		contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
		contasTransferidas.add(contaOrigem);

		TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
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

		Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino,
				TipoTransferencia.TED);
		contaOrigem.getTransferencia().add(transferindo);

		contaRepository.save(contaOrigem);

		contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
		contasTransferidas.add(contaDestino);

		TaxaManutencao taxaContaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());

		List<TaxaManutencao> novaTaxa2 = new ArrayList<TaxaManutencao>();
		novaTaxa2.add(taxaContaDestino);

		contaDestino.setTaxas(novaTaxa2);
		contaDestino.setCategoriaConta(taxaContaDestino.getCategoria());

		if (contaDestino instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaDestino.getTaxaManutencaoMensal());
		}

		if (contaDestino instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaDestino.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaDestino.getTaxaMensal());
		}

		contaRepository.save(contaDestino);

		return true;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public BigDecimal exibirSaldo(Long contaId) {
		
		Conta contaSaldo = contaRepository.findById(contaId)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));
		
		return contaSaldo.getSaldoConta();

	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirPIX(Long idContaReceber, UserContaPixDTO dto) {

		Conta contaOrigem = contaRepository.findById(dto.idContaOrigem())
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta origem não existe."));

		Conta contaDestino = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta destino não existe."));

		contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));

		TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaOrigem);

		contaOrigem.setTaxas(novaTaxa);
		contaOrigem.setCategoriaConta(taxaContaOrigem.getCategoria());

		Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino,
				TipoTransferencia.PIX);
		contaOrigem.getTransferencia().add(transferindo);

		if (contaOrigem instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
		}

		if (contaOrigem instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
		}

		contaRepository.save(contaOrigem);

		contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));

		TaxaManutencao taxaContaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());

		List<TaxaManutencao> novaTaxa2 = new ArrayList<TaxaManutencao>();
		novaTaxa2.add(taxaContaDestino);

		contaDestino.setTaxas(novaTaxa2);
		contaDestino.setCategoriaConta(taxaContaDestino.getCategoria());

		if (contaDestino instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaDestino.getTaxaManutencaoMensal());
		}

		if (contaDestino instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaDestino.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaDestino.getTaxaMensal());
		}

		contaRepository.save(contaDestino);

		return true;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirDEPOSITO(Long idContaReceber, UserContaDepositoDTO dto) {

		Conta conta = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));

		conta.setSaldoConta(conta.getSaldoConta().add(dto.valor()));

		TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaOrigem);

		conta.setTaxas(novaTaxa);
		conta.setCategoriaConta(taxaContaOrigem.getCategoria());

		Transferencia transferindo = new Transferencia(conta, dto.valor(), conta, TipoTransferencia.DEPOSITO);
		conta.getTransferencia().add(transferindo);

		if (conta instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
		}

		if (conta instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
		}

		contaRepository.save(conta);

		return true;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirSAQUE(Long idContaReceber, UserContaSaqueDTO dto) {

		Conta conta = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta não existe."));
		
		if(conta.getSaldoConta().compareTo(dto.valor()) < 0 || conta.getSaldoConta().compareTo(BigDecimal.ZERO) == 0) {
			throw new ContaExibirSaldoErroException("Saldo insuficiente.");
		}

		conta.setSaldoConta(conta.getSaldoConta().subtract(dto.valor()));

		TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaOrigem);

		conta.setTaxas(novaTaxa);
		conta.setCategoriaConta(taxaContaOrigem.getCategoria());

		Transferencia transferindo = new Transferencia(conta, dto.valor(), conta, TipoTransferencia.SAQUE);
		conta.getTransferencia().add(transferindo);

		if (conta instanceof ContaCorrente cc) {
			cc.setTaxaManutencaoMensal(taxaContaOrigem.getTaxaManutencaoMensal());
		}

		if (conta instanceof ContaPoupanca cp) {
			cp.setTaxaAcrescRend(taxaContaOrigem.getTaxaAcrescRend());
			cp.setTaxaMensal(taxaContaOrigem.getTaxaMensal());
		}

		contaRepository.save(conta);

		return true;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta manutencaoTaxaCC(Long idConta, UserContaTaxaManutencaoDTO dto) {

		Conta conta = contaRepository.findById(idConta)
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada."));
		
		 if (conta.getSaldoConta() == null) {
		        throw new ContaNaoEncontradaException("Saldo da conta está indefinido.");
		 }
		 
		 if (!(conta instanceof ContaCorrente)) {
				throw new ContaNaoEncontradaException("Taxa de manutenção só pode ser aplicada a contas correntes.");
			}
		 
		 if(conta instanceof ContaCorrente cc) {				
			BigDecimal taxa = cc.getTaxaManutencaoMensal();
			conta.setSaldoConta(conta.getSaldoConta().subtract(taxa));
			TaxaManutencao novaTaxa = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
			conta.setCategoriaConta(novaTaxa.getCategoria());
		}		
		 
		 contaRepository.save(conta);
		
		return conta;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta rendimentoTaxaCP(Long idConta, UserContaRendimentoDTO dto) {
		
	    Conta conta = contaRepository.findById(idConta)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada."));	    
	    
	    if (conta.getSaldoConta() == null) {
	        throw new ContaNaoEncontradaException("Saldo da conta está indefinido.");
	    }
	    
	    if (!(conta instanceof ContaPoupanca)) {
	        throw new ContaNaoEncontradaException("Rendimentos só podem ser aplicados a contas poupança.");
	    }
	    
	    if(conta instanceof ContaPoupanca cp) {
	    	
	    	BigDecimal saldoAtual = conta.getSaldoConta();
	    	BigDecimal rendimento = saldoAtual
		            .multiply(cp.getTaxaAcrescRend())
		            .subtract(cp.getTaxaMensal());
	
		    conta.setSaldoConta(saldoAtual.add(rendimento));
		   
		    TaxaManutencao novaTaxa = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
		    conta.setCategoriaConta(novaTaxa.getCategoria());
	    }	    

	    return contaRepository.save(conta);
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
