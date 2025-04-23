package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.security.CartaoCreateDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.CartaoCredito;
import br.com.marcielli.BancoM.entity.CartaoDebito;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.entity.ValidacaoUsuarioAtivo.ValidacaoUsuarioUtil;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.repository.CartaoRepository;
import br.com.marcielli.BancoM.repository.UserRepository;

@Service
public class UserCartaoService {
	
	private final CartaoRepository cartaoRepository;
	private final UserRepository userRepository;
	
	private BigDecimal limiteCredito = new BigDecimal("600");
	private BigDecimal limiteDiarioTransacao = new BigDecimal("600");
	
	public UserCartaoService(CartaoRepository cartaoRepository, UserRepository userRepository) {
		this.cartaoRepository = cartaoRepository;
		this.userRepository = userRepository;
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
			
			ValidacaoUsuarioUtil.verificarUsuarioAtivo(user);	
			
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
				cartao.setSenha(dto.senha());
				cartao.setNumeroCartao(numeroCartao);
				cartao.setStatus(true);	
				
				if(cartao instanceof CartaoCredito cartaoCredito) {
					cartaoCredito.setLimiteCreditoPreAprovado(limiteCredito);
				}
				
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
				
				if(cartao instanceof CartaoDebito cartaoDebito) {
					cartaoDebito.setLimiteDiarioTransacao(limiteDiarioTransacao);
				}
				
				cartoes.add(cartao);	
				
					cartao.setConta(contaDoUser);
					cartao.setTipoConta(contaDoUser.getTipoConta());
					cartao.setCategoriaConta(contaDoUser.getCategoriaConta());
					contaDoUser.setCartoes(cartoes);
			
			}
			
			
		} catch (NumberFormatException e) {			
			System.out.println("ID inválido no token: " + token.getName());
		}
		
		
		
		
		
//		var user = userRepository.findById(userId);		
//		
//		Optional<Conta> contaDoUser = user
//			    .map(User::getCliente)
//			    .map(Cliente::getContas)
//			    .flatMap(contas -> contas.stream()
//			        .filter(conta -> conta.getId().equals(dto.idConta()))
//			        .findFirst());
//		
//		if(dto.tipoCartao() == TipoCartao.CREDITO) {
//			
//			String numeroCartao = numCartao.concat("-CC");
//			
//			cartao = new CartaoCredito();
//			
//			cartao.setTipoCartao(dto.tipoCartao());
//			cartao.setSenha(dto.senha());
//			cartao.setNumeroCartao(numeroCartao);
//			cartao.setStatus(true);	
//			
//			if(cartao instanceof CartaoCredito cartaoCredito) {
//				cartaoCredito.setLimiteCreditoPreAprovado(limiteCredito);
//			}
//			
//			cartoes.add(cartao);	
//			
//			if(contaDoUser.isPresent()) {
//				Conta contaDoCartao = contaDoUser.get();
//				cartao.setConta(contaDoCartao);
//				cartao.setTipoConta(contaDoCartao.getTipoConta());
//				cartao.setCategoriaConta(contaDoCartao.getCategoriaConta());
//				contaDoCartao.setCartoes(cartoes);
//			} else {
//				throw new RuntimeException("Conta não está vinculada ao usuário.");
//			}
//		}
//		
//		if(dto.tipoCartao() == TipoCartao.DEBITO) {
//			
//			String numeroCartao = numCartao.concat("-CD");
//			
//			cartao = new CartaoDebito();	
//			
//			cartao.setTipoCartao(dto.tipoCartao());
//			cartao.setSenha(dto.senha());
//			cartao.setNumeroCartao(numeroCartao);
//			cartao.setStatus(true);
//			
//			if(cartao instanceof CartaoDebito cartaoDebito) {
//				cartaoDebito.setLimiteDiarioTransacao(limiteDiarioTransacao);
//			}
//			
//			cartoes.add(cartao);	
//			
//			if(contaDoUser.isPresent()) {
//				Conta contaDoCartao = contaDoUser.get();
//				cartao.setConta(contaDoCartao);
//				cartao.setTipoConta(contaDoCartao.getTipoConta());
//				cartao.setCategoriaConta(contaDoCartao.getCategoriaConta());
//				contaDoCartao.setCartoes(cartoes);
//			} else {
//				throw new RuntimeException("Conta não está vinculada ao usuário.");
//			}
//		}
//		
		cartaoRepository.save(cartao);
		
		return cartao;
		
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
