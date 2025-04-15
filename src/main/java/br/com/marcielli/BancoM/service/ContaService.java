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

import br.com.marcielli.BancoM.dto.ContaCorrenteTaxaManutencaoDTO;
import br.com.marcielli.BancoM.dto.ContaCreateDepositoDTO;
import br.com.marcielli.BancoM.dto.ContaCreatePixDTO;
import br.com.marcielli.BancoM.dto.ContaCreateSaqueDTO;
import br.com.marcielli.BancoM.dto.ContaCreateTedDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.enuns.TipoTransferencia;
import br.com.marcielli.BancoM.exception.ContaExisteNoBancoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.ContaRepositoy;

@Service
public class ContaService {

	@Autowired
	private ContaRepositoy contaRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta save(Conta dto) {

		Cliente cliente = clienteRepository.findById(dto.getId())
				.orElseThrow(() -> new ContaExisteNoBancoException("A conta já existe no banco."));

		dto.setCliente(cliente);

		TaxaManutencao taxa = new TaxaManutencao(dto.getSaldoConta(), dto.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxa);

		Conta novaConta = null;

		String numeroConta = gerarNumeroDaConta();
		String numeroPix = gerarPixAleatorio();

		String novoPix = numeroPix.concat("-PIX");

		if (dto.getTipoConta() == TipoConta.CORRENTE) {
			novaConta = new ContaCorrente(taxa.getTaxaManutencaoMensal());
			novaConta.setTaxas(novaTaxa);

			String numContaCorrente = numeroConta.concat("-CC");
			novaConta.setNumeroConta(numContaCorrente);
		}

		if (dto.getTipoConta() == TipoConta.POUPANCA) {
			novaConta = new ContaPoupanca(taxa.getTaxaAcrescRend(), taxa.getTaxaMensal());
			novaConta.setTaxas(novaTaxa);

			String numContaPoupanca = numeroConta.concat("-PP");
			novaConta.setNumeroConta(numContaPoupanca);

		}

		novaConta.setCliente(cliente);
		novaConta.setSaldoConta(dto.getSaldoConta());
		novaConta.setCategoriaConta(taxa.getCategoria());
		novaConta.setTipoConta(dto.getTipoConta());
		novaConta.setPixAleatorio(novoPix);
		novaConta.setStatus(true);

		return contaRepository.save(novaConta);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta update(Long idContaParaAtualizar, Conta dto) {

		Conta conta = contaRepository.findById(idContaParaAtualizar).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta não pode ser atualizada porque não existe no banco."));

		TaxaManutencao taxa = new TaxaManutencao(conta.getSaldoConta(), dto.getTipoConta());

		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxa);

		Conta novaConta = null;

		String numeroConta = gerarNumeroDaConta();
		String numeroPix = gerarPixAleatorio();

		String novoPix = numeroPix.concat("-PIX");

		if (dto.getTipoConta() == TipoConta.CORRENTE) {

			novaConta = new ContaCorrente(taxa.getTaxaManutencaoMensal());
			novaConta.setTaxas(novaTaxa);
			conta.setTaxas(novaConta.getTaxas());

			String numContaCorrente = numeroConta.concat("-CC");
			novaConta.setNumeroConta(numContaCorrente);
		}

		if (dto.getTipoConta() == TipoConta.POUPANCA) {

			novaConta = new ContaPoupanca(taxa.getTaxaAcrescRend(), taxa.getTaxaMensal());
			novaConta.setTaxas(novaTaxa);
			conta.setTaxas(novaConta.getTaxas());

			String numContaPoupanca = numeroConta.concat("-PP");
			novaConta.setNumeroConta(numContaPoupanca);

		}

		novaConta.setSaldoConta(dto.getSaldoConta());
		novaConta.setCategoriaConta(taxa.getCategoria());
		novaConta.setTipoConta(dto.getTipoConta());
		novaConta.setPixAleatorio(novoPix);
		novaConta.setStatus(true);

		conta.setPixAleatorio(novaConta.getPixAleatorio());
		conta.setNumeroConta(novaConta.getNumeroConta());
		conta.setCategoriaConta(novaConta.getCategoriaConta());
		conta.setTipoConta(novaConta.getTipoConta());
		conta.setPixAleatorio(novaConta.getPixAleatorio());

		return contaRepository.save(conta);

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Conta> getAll() {

		List<Conta> contasH2 = contaRepository.findAll();

		if (contasH2.size() <= 0) {
			throw new ContaNaoEncontradaException("Não existem contas cadastradas no banco.");
		}

		return contasH2;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Conta> getContaById(Long id) {

		Optional<Conta> contaH2 = contaRepository.findById(id);

		if (!contaH2.isPresent()) {
			throw new ContaNaoEncontradaException("Conta não encontrada.");
		}

		return contaH2;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean deleteConta(Long contaId) {
		
		Conta conta = contaRepository.findById(contaId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		contaRepository.deleteById(conta.getId());
		
		return true;

	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirTED(Long idContaReceber, ContaCreateTedDTO dto) {
		
		Cliente clienteOrigem = clienteRepository.findById(dto.getIdClienteOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cliente origem não existe."));
		
		Conta contaOrigem = contaRepository.findById(dto.getIdContaOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta origem não existe."));
		
		Cliente clienteDestino = clienteRepository.findById(dto.getIdClienteDestino()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cliente destino não existe."));
		
		Conta contaDestino = contaRepository.findById(idContaReceber).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta destino não existe."));		
	
		List<Conta> contasTransferidas = new ArrayList<Conta>();	
		
		for(Conta contasDoClienteOrigem : clienteOrigem.getContas()) {
		
			if(contasDoClienteOrigem.getId() == contaOrigem.getId()) {
			
				contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.getValor()));
				contasTransferidas.add(contaOrigem);
				
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
							
				Transferencia transferindo = new Transferencia(contaOrigem, dto.getValor(), contaDestino, TipoTransferencia.TED);
				contaOrigem.getTransferencia().add(transferindo);
	
				contaRepository.save(contaOrigem);		
				break;
			
			}
		}		
		
		for(Conta contasDoClienteDestino : clienteDestino.getContas()) {
			
			if(contasDoClienteDestino.getId() == contaDestino.getId()) {
				
				contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.getValor()));
				contasTransferidas.add(contaDestino);
				
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
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirPIX(Long idContaReceber, ContaCreatePixDTO dto) {
		
		Cliente clienteOrigem = clienteRepository.findById(dto.getIdClienteOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cliente origem não existe."));
		
		Conta contaOrigem = contaRepository.findById(dto.getIdContaOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta origem não existe."));
		
		Conta contaDestino = contaRepository.findById(idContaReceber).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta destino não existe."));	
		
		Cliente clienteDestino = clienteRepository.findById(contaDestino.getCliente().getId()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cliente destino não existe."));
		
		
		for(Conta contasDoClienteOrigem : clienteOrigem.getContas()) {
		
			if(contasDoClienteOrigem.getId() == contaOrigem.getId()) {
			
				contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.getValor()));				
				
				TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(), contaOrigem.getTipoConta());

				List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
				novaTaxa.add(taxaContaOrigem);
				
				contaOrigem.setTaxas(novaTaxa);
				contaOrigem.setCategoriaConta(taxaContaOrigem.getCategoria());
				
				Transferencia transferindo = new Transferencia(contaOrigem, dto.getValor(), contaDestino, TipoTransferencia.PIX);
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
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirDEPOSITO(Long idContaReceber, ContaCreateDepositoDTO dto) {
		
		Cliente cliente = clienteRepository.findById(dto.getIdClienteOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cliente não existe."));
		
		Conta conta = contaRepository.findById(idContaReceber).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta não existe."));

		
		for(Conta contasDoClienteOrigem : cliente.getContas()) {
		
			if(contasDoClienteOrigem.getId() == conta.getId()) {
			
				conta.setSaldoConta(conta.getSaldoConta().add(dto.getValor()));				
				
				TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());

				List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
				novaTaxa.add(taxaContaOrigem);
				
				conta.setTaxas(novaTaxa);
				conta.setCategoriaConta(taxaContaOrigem.getCategoria());
				
				Transferencia transferindo = new Transferencia(conta, dto.getValor(), conta, TipoTransferencia.DEPOSITO);
				conta.getTransferencia().add(transferindo);
	
				contaRepository.save(conta);		
				break;
			
			}
		}		

		return true;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean transferirSAQUE(Long idContaReceber, ContaCreateSaqueDTO dto) {
		
		Cliente cliente = clienteRepository.findById(dto.getIdClienteOrigem()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cliente não existe."));
		
		Conta conta = contaRepository.findById(idContaReceber).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta não existe."));

		
		for(Conta contasDoClienteOrigem : cliente.getContas()) {
		
			if(contasDoClienteOrigem.getId() == conta.getId()) {
			
				conta.setSaldoConta(conta.getSaldoConta().subtract(dto.getValor()));				
				
				TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());

				List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
				novaTaxa.add(taxaContaOrigem);
				
				conta.setTaxas(novaTaxa);
				conta.setCategoriaConta(taxaContaOrigem.getCategoria());
				
				Transferencia transferindo = new Transferencia(conta, dto.getValor(), conta, TipoTransferencia.SAQUE);
				conta.getTransferencia().add(transferindo);
	
				contaRepository.save(conta);		
				break;
			
			}
		}		

		return true;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean manutencaoTaxaCC(Long idConta, ContaCorrenteTaxaManutencaoDTO dto) {
		
		Conta conta = contaRepository.findById(idConta).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta não existe."));
		
		TaxaManutencao taxaContaOrigem = new TaxaManutencao(conta.getSaldoConta(), conta.getTipoConta());
		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaOrigem);
		conta.setTaxas(novaTaxa);
		conta.setCategoriaConta(taxaContaOrigem.getCategoria());
		
		return true;
	}
	
	

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public BigDecimal exibirSaldo(Long clienteId) {
		
		Cliente clienteSaldo = clienteRepository.findById(clienteId).orElseThrow(
				() -> new ContaNaoEncontradaException("O cliente não existe."));
		
		BigDecimal saldo = new BigDecimal("0");
		
		for(Conta getContas : clienteSaldo.getContas()) {
			
			saldo = saldo.add(getContas.getSaldoConta());
			
		}
		
		return saldo;
	

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
