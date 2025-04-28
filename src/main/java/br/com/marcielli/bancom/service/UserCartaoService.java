package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marcielli.bancom.dto.security.CartaoCreateDTO;
import br.com.marcielli.bancom.dto.security.CartaoUpdateDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarLimiteCartaoCreditoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarLimiteCartaoDebitoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarSenhaCartaoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarStatusCartaoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoPagCartaoDTO;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.entity.Fatura;
import br.com.marcielli.bancom.entity.TaxaManutencao;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
import br.com.marcielli.bancom.exception.CartaoNaoEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ContaExibirSaldoErroException;
import br.com.marcielli.bancom.exception.ContaExisteNoBancoException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;
import br.com.marcielli.bancom.exception.PermissaoNegadaException;
import br.com.marcielli.bancom.exception.TransferenciaNaoRealizadaException;
import br.com.marcielli.bancom.repository.CartaoRepository;
import br.com.marcielli.bancom.repository.ClienteRepository;
import br.com.marcielli.bancom.repository.ContaRepository;

@Service
public class UserCartaoService {

	private final CartaoRepository cartaoRepository;
	private final ContaRepository contaRepository;
	private final ClienteRepository clienteRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserCartaoService(CartaoRepository cartaoRepository,
			ContaRepository contaRepository, BCryptPasswordEncoder passwordEncoder,
			ClienteRepository clienteRepository) {
		this.cartaoRepository = cartaoRepository;
		
		this.contaRepository = contaRepository;
		this.passwordEncoder = passwordEncoder;
		this.clienteRepository = clienteRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao save(CartaoCreateDTO dto) {

		Cliente clienteAlvo = clienteRepository.findById(dto.idUsuario())
				.orElseThrow(() -> new ContaNaoEncontradaException("Cliente não encontrado"));

		Conta contaDoUser = clienteAlvo.getContas().stream().filter(c -> c.getId().equals(dto.idConta())).findFirst()
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada ou não pertence ao usuário"));
		
		if(!clienteAlvo.getContas().contains(contaDoUser)) {
			throw new ContaNaoEncontradaException("A conta informada não pertence ao cliente.");
		}
		
		if(clienteAlvo.isClienteAtivo() == false) {
	    	throw new ClienteNaoEncontradoException("O cliente está desativado. Não é possível criar uma conta para ele nesse momento.");
	    }

		String numCartao = gerarNumCartao();
		Cartao cartao = null;
		List<Cartao> cartoes = new ArrayList<>();

		if (dto.tipoCartao() == TipoCartao.CREDITO) {
			String numeroCartao = numCartao.concat("-CC");
			cartao = new CartaoCredito();

			cartao.setTipoCartao(dto.tipoCartao());
			cartao.setSenha(passwordEncoder.encode(dto.senha()));
			cartao.setNumeroCartao(numeroCartao);
			cartao.setStatus(true);
			cartoes.add(cartao);

			cartao.setConta(contaDoUser);
			cartao.setTipoConta(contaDoUser.getTipoConta());
			cartao.setCategoriaConta(contaDoUser.getCategoriaConta());
			contaDoUser.setCartoes(cartoes);
		} else if (dto.tipoCartao() == TipoCartao.DEBITO) {
			String numeroCartao = numCartao.concat("-CD");
			cartao = new CartaoDebito();

			cartao.setTipoCartao(dto.tipoCartao());
			cartao.setSenha(passwordEncoder.encode(dto.senha()));
			cartao.setNumeroCartao(numeroCartao);
			cartao.setStatus(true);
			cartoes.add(cartao);

			cartao.setConta(contaDoUser);
			cartao.setTipoConta(contaDoUser.getTipoConta());
			cartao.setCategoriaConta(contaDoUser.getCategoriaConta());
			contaDoUser.setCartoes(cartoes);
		}

		return cartaoRepository.save(cartao);

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Cartao> getCartoes() {
		return cartaoRepository.findAll();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao getCartoesById(Long id) {
		return cartaoRepository.findById(id).orElseThrow(() -> new ContaNaoEncontradaException("Cartão não encontrado"));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao updateSenha(Long cartaoId, CartaoUpdateDTO dto) {

		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaNaoEncontradaException("Cartão não encontrado"));
		
		Long userId = cartao.getConta().getCliente().getUser().getId().longValue();
		
		if(userId != dto.idUsuario()) {
			throw new ClienteNaoEncontradoException("Você não tem permissão para alterar esse cartão.");
		}	

		if (!cartao.isStatus()) { // Só pode atualizar se o cartão estiver com status true
			throw new PermissaoNegadaException("Não é possível atualizar a senha de um cartão desativado.");
		}

//		if (!cartao.getConta().getCliente().getId().equals(dto.idUsuario())) {
//			throw new PermissaoNegadaException("Este cartão não pertence ao usuário informado");
//		}

		clienteRepository.findById(dto.idUsuario())
				.orElseThrow(() -> new ContaNaoEncontradaException("Usuário não encontrado"));

		cartao.setSenha(passwordEncoder.encode(dto.senha()));
		return cartaoRepository.save(cartao);
	}

	@Transactional
	public boolean delete(Long cartaoId, @RequestBody CartaoUpdateDTO dto) {

		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaNaoEncontradaException("Cartão não encontrado"));
				
		if(cartao.getConta().getSaldoConta().compareTo(BigDecimal.ZERO) > 0) {
			throw new ContaExibirSaldoErroException("A conta possui um saldo de R$ "+cartao.getConta().getSaldoConta()+". Faça o saque antes de remover a conta.");
		}	
		
		Long userId = cartao.getConta().getCliente().getUser().getId().longValue();
		
		if(userId != dto.idUsuario()) {
			throw new ClienteNaoEncontradoException("Você não tem permissão para deletar esse cartão.");
		}	
		
		if (!cartao.isStatus()) { // Só pode atualizar se o cartão estiver com status true
			throw new PermissaoNegadaException("Cartão já foi desativado anteriormente.");
		}

		cartao.setStatus(false);
		cartaoRepository.save(cartao);
		return true;
	}

	// Pagamentos

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean pagCartao(Long idContaReceber, UserCartaoPagCartaoDTO dto) {

		if (idContaReceber == null) {
			throw new IllegalArgumentException("ID do cartão não pode ser nulo");
		}

		Cartao cartaoOrigem = cartaoRepository.findById(dto.idCartao())
				.orElseThrow(() -> new ContaNaoEncontradaException("O cartão origem não existe."));

		Conta contaOrigem = contaRepository.findById(cartaoOrigem.getConta().getId())
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta destino não existe."));

		Conta contaDestino = contaRepository.findById(idContaReceber)
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta destino não existe."));
		
		if (cartaoOrigem.isStatus() == false) { 
			throw new PermissaoNegadaException("Não é possível realizar operações através de cartões desativados.");
		}
		
		if (contaOrigem.getStatus() == false) { 
			throw new PermissaoNegadaException("Não é possível realizar operações através de contas desativadss.");
		}
		
		if (contaDestino.getStatus() == false) { 
			throw new PermissaoNegadaException("Não é possível realizar operações através de contas desativadss.");
		}
		
		if(!contaOrigem.getCartoes().contains(cartaoOrigem)) {
			throw new ContaNaoEncontradaException("O cartão informado não pertence a conta origem.");
		}

		if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
			throw new TransferenciaNaoRealizadaException("Saldo insuficiente na conta para realizar a transação.");
		}

		if (cartaoOrigem instanceof CartaoCredito cartaoC) {

			if (dto.valor().compareTo(cartaoC.getLimiteCreditoPreAprovado()) > 0) {
				throw new TransferenciaNaoRealizadaException(
						"Você já utilizou o seu limite de crédito pré aprovado para envio.");
			}

			if (cartaoC.getLimiteCreditoPreAprovado().compareTo(BigDecimal.ZERO) <= 0) {
				throw new TransferenciaNaoRealizadaException("O cartão não tem limite de crédito.");
			}

			cartaoC.atualizarTotalGastoMes(dto.valor());
			cartaoC.atualizarLimiteCreditoPreAprovado(dto.valor());

			// Já tem uma fatura associada?
			Fatura faturaExistente = cartaoOrigem.getFatura();

			if (faturaExistente == null) {

				faturaExistente = new Fatura();

				faturaExistente.setCartao(cartaoOrigem);
				cartaoOrigem.setFatura(faturaExistente);
			}

			Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino,
					TipoTransferencia.TED, cartaoOrigem.getTipoCartao());
			contaOrigem.getTransferencia().add(transferindo);

			transferindo.setFatura(faturaExistente);
			faturaExistente.getTransferenciasCredito().add(transferindo);

			cartaoRepository.save(cartaoC);
		}

		if (cartaoOrigem instanceof CartaoDebito cartaoD) {

			if (dto.valor().compareTo(cartaoD.getLimiteDiarioTransacao()) > 0) {
				throw new TransferenciaNaoRealizadaException(
						"Você já utilizou o seu limite de crédito pré aprovado para envio.");
			}

			if (cartaoD.getLimiteDiarioTransacao().compareTo(BigDecimal.ZERO) <= 0) {
				throw new TransferenciaNaoRealizadaException("O cartão não tem limite de transação.");
			}

			contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));

			cartaoD.atualizarLimiteDiarioTransacao(dto.valor());
			cartaoD.atualizarTotalGastoMes(dto.valor());

			TaxaManutencao taxaContaOrigem = new TaxaManutencao(contaOrigem.getSaldoConta(),
					contaOrigem.getTipoConta());

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

			Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino,
					TipoTransferencia.TED, cartaoOrigem.getTipoCartao());
			contaOrigem.getTransferencia().add(transferindo);

			contaRepository.save(contaOrigem);
		}

		contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(dto.valor()));
		TaxaManutencao taxaContaDestino = new TaxaManutencao(contaDestino.getSaldoConta(), contaDestino.getTipoConta());
		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
		novaTaxa.add(taxaContaDestino);

		contaDestino.setTaxas(novaTaxa);
		contaDestino.setCategoriaConta(taxaContaDestino.getCategoria());

		contaRepository.save(contaDestino);

		return true;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarLimiteCartaoCredito(Long cartaoId, UserCartaoAlterarLimiteCartaoCreditoDTO dto) {

		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));

		BigDecimal novoLimite = dto.novoLimite();
		
		if(cartao.isStatus() == false) {
			throw new CartaoNaoEncontradoException("Não é possível alterar limite de cartão desativado.");
		}

		if (dto.novoLimite() == null) {
			throw new CartaoNaoEncontradoException("Você precisa digitar um valor para o novo limite do cartão");
		}

		if (cartao instanceof CartaoCredito cartaoCredito) {
			cartaoCredito.alterarLimiteCreditoPreAprovado(novoLimite);

		}
		cartaoRepository.save(cartao);
		return cartao;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarStatusC(Long cartaoId, UserCartaoAlterarStatusCartaoDTO dto) {

		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		String statusNovo = dto.novoStatus();

		if (statusNovo.equalsIgnoreCase("true")) {
			cartao.setStatus(true);
		}

		if (statusNovo.equalsIgnoreCase("false")) {
			cartao.setStatus(false);
		}

		return cartaoRepository.save(cartao);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarSenhaC(Long cartaoId, UserCartaoAlterarSenhaCartaoDTO dto) {

		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		if(cartao.isStatus() == false) {
			throw new CartaoNaoEncontradoException("Não é possível alterar senha de cartão desativado.");
		}

		String senhaNova = dto.novaSenha();

		cartao.setSenha(senhaNova);

		return cartaoRepository.save(cartao);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarLimiteCartaoDebito(Long cartaoId, UserCartaoAlterarLimiteCartaoDebitoDTO dto) {

		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		if(cartao.isStatus() == false) {
			throw new CartaoNaoEncontradoException("Não é possível alterar limite de cartão desativado.");
		}

		BigDecimal novoLimite = dto.novoLimite();

		if (dto.novoLimite() == null) {
			throw new CartaoNaoEncontradoException("Você precisa digitar um valor para o novo limite do cartão");
		}

		if (cartao instanceof CartaoDebito cartaoDebito) {
			cartaoDebito.alterarLimiteDiarioTransacao(novoLimite);
		}

		cartaoRepository.save(cartao);
		return cartao;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Fatura> getFaturaCartaoDeCreditoService(Long cartaoId) {

		return cartaoRepository.findById(cartaoId).filter(c -> c instanceof CartaoCredito).map(c -> {
			Fatura fatura = ((CartaoCredito) c).getFatura();
			if (fatura.isStatus()) {
				throw new CartaoNaoEncontradoException("A fatura já foi paga.");
			}
			return fatura;
		});
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean pagFaturaCartaoC(Long idCartao) {

		Cartao cartaoOrigem = cartaoRepository.findById(idCartao)
				.orElseThrow(() -> new CartaoNaoEncontradoException("O cartão origem não existe."));

		Conta contaOrigem = contaRepository.findById(cartaoOrigem.getConta().getId())
				.orElseThrow(() -> new ContaNaoEncontradaException("A conta origem não existe."));
		
		if(cartaoOrigem.isStatus() == false) {
			throw new CartaoNaoEncontradoException("Não é possível pagar fatura através de um cartão desativado.");
		}
		
		if(contaOrigem.getStatus() == false) {
			throw new CartaoNaoEncontradoException("Não é possível pagar fatura através de uma conta desativada.");
		}

		if (cartaoOrigem instanceof CartaoCredito cc) {

			if (cc.getTotalGastoMesCredito().compareTo(contaOrigem.getSaldoConta()) > 0) {
				throw new CartaoNaoEncontradoException("Você não tem saldo suficiente para realizar o pagamento.");
			}

			cc.getTotalGastoMesCredito();
			contaOrigem.pagarFatura(cc.getTotalGastoMesCredito());
			cc.getFatura().setStatus(true);

		}

		return true;

	}

	public String gerarNumCartao() {

		int[] sequencia = new int[8];
		Random random = new Random();
		String meucartao = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			meucartao += Integer.toString(sequencia[i]);
		}

		return meucartao;
	}

}
