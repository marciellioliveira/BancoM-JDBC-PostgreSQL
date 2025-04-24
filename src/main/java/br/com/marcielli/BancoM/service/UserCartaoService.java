package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.security.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.security.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoAlterarLimiteCartaoCreditoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoAlterarLimiteCartaoDebitoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoAlterarSenhaCartaoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoAlterarStatusCartaoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoPagCartaoDTO;
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
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.entity.ValidacaoUsuarioAtivo;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoTransferencia;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExisteNoBancoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.TransferenciaNaoRealizadaException;
import br.com.marcielli.BancoM.repository.CartaoRepository;
import br.com.marcielli.BancoM.repository.ContaRepositoy;
import br.com.marcielli.BancoM.repository.UserRepository;

@Service
public class UserCartaoService {
	
	private final CartaoRepository cartaoRepository;
	private final ContaRepositoy contaRepository;
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	public UserCartaoService(CartaoRepository cartaoRepository, UserRepository userRepository, ContaRepositoy contaRepository, BCryptPasswordEncoder passwordEncoder) {
		this.cartaoRepository = cartaoRepository;
		this.userRepository = userRepository;
		this.contaRepository = contaRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao save(CartaoCreateDTO dto, JwtAuthenticationToken token) {
		
		//Receber o usuário que está logado e criar a conta desse usuário.
		Integer userId = null;
		
		Cartao cartao = null;
		List<Cartao> cartoes = new ArrayList<Cartao>();
		String numCartao = gerarNumCartao();
		
		try {
			userId = Integer.parseInt(token.getName());
			User user = userRepository.findById(userId)
				    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
			
			ValidacaoUsuarioAtivo.verificarUsuarioAtivo(user);	
			
			Cliente cliente = user.getCliente();
			if (cliente == null) {
			    throw new RuntimeException("Cliente não associado ao usuário");
			}
			
			Conta contaDoUser = cliente.getContas().stream()
				    .filter(c -> c.getId().equals(dto.idConta()))
				    .findFirst()
				    .orElseThrow(() -> new RuntimeException("Conta não pertence a este cliente"));
			
			if(dto.tipoCartao() == TipoCartao.CREDITO) {
				
				String numeroCartao = numCartao.concat("-CC");
				
				cartao = new CartaoCredito();
				
				cartao.setTipoCartao(dto.tipoCartao());
				
				//user.setPassword(passwordEncoder.encode(cliente.password()));
				//cartao.setSenha(dto.senha());
				cartao.setSenha(passwordEncoder.encode(dto.senha()));
				cartao.setNumeroCartao(numeroCartao);
				cartao.setStatus(true);	
				cartoes.add(cartao);
				
				cartao.setConta(contaDoUser);
				cartao.setTipoConta(contaDoUser.getTipoConta());
				cartao.setCategoriaConta(contaDoUser.getCategoriaConta());
				contaDoUser.setCartoes(cartoes);
				
			}
			
			if(dto.tipoCartao() == TipoCartao.DEBITO) {
				
				String numeroCartao = numCartao.concat("-CD");
				
				cartao = new CartaoDebito();	
				
				cartao.setTipoCartao(dto.tipoCartao());
				cartao.setSenha(dto.senha());
				cartao.setNumeroCartao(numeroCartao);
				cartao.setStatus(true);
				
				cartoes.add(cartao);
				cartao.setConta(contaDoUser);
				cartao.setTipoConta(contaDoUser.getTipoConta());
				cartao.setCategoriaConta(contaDoUser.getCategoriaConta());
				contaDoUser.setCartoes(cartoes);
			
			}
			
			
		} catch (NumberFormatException e) {			
			System.out.println("ID inválido no token: " + token.getName());
		}
		
		cartaoRepository.save(cartao);
		
		return cartao;
		
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao getCartoesById(Long id) {
		return cartaoRepository.findById(id).orElse(null);
	}
	
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao update(Long id, CartaoUpdateDTO dto) {
		
		Cartao cartaoExiste = cartaoRepository.findById(id).orElse(null);
		
		if (cartaoExiste == null) {
			 return null;
		}
		
		cartaoExiste.setSenha(dto.senha());
		
		cartaoRepository.save(cartaoExiste);
		return cartaoExiste;
	
	}
	
	
	@Transactional
	public boolean delete(Long id) {
		
		Cartao cartaoExistente = cartaoRepository.findById(id).orElse(null);
		
		boolean isAdmin = cartaoExistente.getConta().getCliente().getUser().getRoles().stream()
			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
	
		if(isAdmin) {
			throw new ClienteNaoEncontradoException("Não é possível deletar dados do administrador do sistema.");
		}
		
		cartaoExistente.setStatus(false);
		
	    return true;
	}
	
	
	
	//Pagamentos
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean pagCartao(Long idContaReceber, UserCartaoPagCartaoDTO dto) {
		
		Cartao cartaoOrigem = cartaoRepository.findById(dto.idCartao()).orElseThrow(
				() -> new ContaNaoEncontradaException("O cartão origem não existe."));
		
		Conta contaOrigem = contaRepository.findById(cartaoOrigem.getConta().getId()).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta destino não existe."));	
		
		Conta contaDestino = contaRepository.findById(idContaReceber).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta destino não existe."));	
		
		if (contaOrigem.getSaldoConta().compareTo(dto.valor()) < 0) {
		    throw new TransferenciaNaoRealizadaException("Saldo insuficiente na conta para realizar a transação.");
		}

		
		if(cartaoOrigem instanceof CartaoCredito cartaoC) {
			
			if(dto.valor().compareTo(cartaoC.getLimiteCreditoPreAprovado()) > 0) {
				throw new TransferenciaNaoRealizadaException("Você já utilizou o seu limite de crédito pré aprovado para envio.");
			}	
			
			if(cartaoC.getLimiteCreditoPreAprovado().compareTo(BigDecimal.ZERO) <= 0) {
				throw new TransferenciaNaoRealizadaException("O cartão não tem limite de crédito.");
			}
			
			cartaoC.atualizarTotalGastoMes(dto.valor());
			cartaoC.atualizarLimiteCreditoPreAprovado(dto.valor());
			
			//Já tem uma fatura associada?			
			Fatura faturaExistente = cartaoOrigem.getFatura();
			
			if(faturaExistente == null) {
				
				faturaExistente = new Fatura();
			
				faturaExistente.setCartao(cartaoOrigem);										
				cartaoOrigem.setFatura(faturaExistente);
			}
			
			Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino, TipoTransferencia.TED, cartaoOrigem.getTipoCartao());
			contaOrigem.getTransferencia().add(transferindo);
			
			transferindo.setFatura(faturaExistente);
			faturaExistente.getTransferenciasCredito().add(transferindo);
				
				cartaoRepository.save(cartaoC);
		}
		
		if(cartaoOrigem instanceof CartaoDebito cartaoD) {
			
			
			if(dto.valor().compareTo(cartaoD.getLimiteDiarioTransacao()) > 0) {
				throw new TransferenciaNaoRealizadaException("Você já utilizou o seu limite de crédito pré aprovado para envio.");
			}		
			
			if(cartaoD.getLimiteDiarioTransacao().compareTo(BigDecimal.ZERO) <= 0) {
				throw new TransferenciaNaoRealizadaException("O cartão não tem limite de transação.");
			}
			
			contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(dto.valor()));
			
			cartaoD.atualizarLimiteDiarioTransacao(dto.valor());
			cartaoD.atualizarTotalGastoMes(dto.valor());
		
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
			
			Transferencia transferindo = new Transferencia(contaOrigem, dto.valor(), contaDestino, TipoTransferencia.TED, cartaoOrigem.getTipoCartao());
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
		
		if(dto.novoLimite() == null) {
			throw new CartaoNaoEncontradoException("Você precisa digitar um valor para o novo limite do cartão");
		}
		
		if(cartao instanceof CartaoCredito cartaoCredito) {
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
		
		if(statusNovo.equalsIgnoreCase("true")) {
			cartao.setStatus(true);							
		} 

		if(statusNovo.equalsIgnoreCase("false")){
			cartao.setStatus(false);
		}
		
		return cartaoRepository.save(cartao);
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarSenhaC(Long cartaoId, UserCartaoAlterarSenhaCartaoDTO dto) {	
		
		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		String senhaNova = dto.novaSenha();
		
		cartao.setSenha(senhaNova);
		
		return cartaoRepository.save(cartao);
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cartao alterarLimiteCartaoDebito(Long cartaoId, UserCartaoAlterarLimiteCartaoDebitoDTO dto) {	
		
		Cartao cartao = cartaoRepository.findById(cartaoId)
				.orElseThrow(() -> new ContaExisteNoBancoException("O cartão não existe no banco."));
		
		BigDecimal novoLimite = dto.novoLimite();
		
		if(dto.novoLimite() == null) {
			throw new CartaoNaoEncontradoException("Você precisa digitar um valor para o novo limite do cartão");
		}
		
		if(cartao instanceof CartaoDebito cartaoDebito) {
			cartaoDebito.alterarLimiteDiarioTransacao(novoLimite);				
		}
		
		cartaoRepository.save(cartao);
		return cartao;
	}
	
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Fatura> getFaturaCartaoDeCreditoService(Long cartaoId) {

		return cartaoRepository.findById(cartaoId)
		        .filter(c -> c instanceof CartaoCredito)
		        .map(c -> {
		            Fatura fatura = ((CartaoCredito) c).getFatura();
		            if (fatura.isStatus()) {
		                throw new CartaoNaoEncontradoException("A fatura já foi paga.");
		            }
		            return fatura;
		        });
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean pagFaturaCartaoC(Long idCartao) {
		
		Cartao cartaoOrigem = cartaoRepository.findById(idCartao).orElseThrow(
				() -> new CartaoNaoEncontradoException("O cartão origem não existe."));
		
		Conta contaOrigem = contaRepository.findById(cartaoOrigem.getConta().getId()).orElseThrow(
				() -> new ContaNaoEncontradaException("A conta origem não existe."));
		
		if(cartaoOrigem instanceof CartaoCredito cc) {
			
			if(cc.getTotalGastoMesCredito().compareTo(contaOrigem.getSaldoConta()) > 0 ) {
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
