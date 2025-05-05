package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dao.CartaoDao;
import br.com.marcielli.bancom.dao.ClienteDao;
import br.com.marcielli.bancom.dao.ContaDao;
import br.com.marcielli.bancom.dao.TransferenciaDao;
import br.com.marcielli.bancom.dao.UserDao;
import br.com.marcielli.bancom.dto.CartaoCreateDTO;
import br.com.marcielli.bancom.dto.CartaoUpdateDTO;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.exception.CartaoNaoEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;
import br.com.marcielli.bancom.exception.PermissaoNegadaException;
import br.com.marcielli.bancom.utils.GerarNumeros;

@Service
public class UserCartaoService {
	
	private final ClienteDao clienteDao;
	private final UserDao userDao;
	private final TransferenciaDao transferenciaDao;
	private final ContaDao contaDao;
	private final CartaoDao cartaoDao;
	private final GerarNumeros gerarNumeros;
	private final BCryptPasswordEncoder passwordEncoder;
	
	
	public UserCartaoService(ClienteDao clienteDao, UserDao userDao, TransferenciaDao transferenciaDao, ContaDao contaDao, CartaoDao cartaoDao, GerarNumeros gerarNumeros, BCryptPasswordEncoder passwordEncoder) {
		this.clienteDao = clienteDao;
		this.userDao = userDao;
		this.transferenciaDao = transferenciaDao;
		this.contaDao = contaDao;
		this.cartaoDao = cartaoDao;
		this.gerarNumeros = gerarNumeros;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
    public Cartao salvar(CartaoCreateDTO dto, Authentication authentication) {
   
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        
        User loggedInUser = userDao.findByUsername(authentication.getName())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

        // Se for BASIC e está tentando criar cartão para outro usuário, bloqueia
        if ("ROLE_BASIC".equals(role) && !dto.getIdCliente().equals(loggedInUser.getId().longValue())) {
            throw new ClienteNaoEncontradoException("Usuário BASIC não tem permissão para criar cartão para outro usuário.");
        }

        Cliente clienteAlvo = clienteDao.findById(dto.getIdCliente().longValue())
                .orElseThrow(() -> new ContaNaoEncontradaException("Cliente não encontrado"));

        Conta contaDoUser = contaDao.findById(dto.getIdConta())
                .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
        
        if(!contaDoUser.getCliente().getId().equals(clienteAlvo.getId())) {
            throw new ContaNaoEncontradaException("A conta informada não pertence ao cliente.");
        }
        
        if(!clienteAlvo.isClienteAtivo()) {
            throw new ClienteNaoEncontradoException("O cliente está desativado.");
        }
        
        String numCartao = gerarNumeros.gerarNumeroGeral();
        Cartao cartao = (dto.getTipoCartao() == TipoCartao.CREDITO) ? 
                new CartaoCredito() : new CartaoDebito();

        String sufixo = (dto.getTipoCartao() == TipoCartao.CREDITO) ? "-CC" : "-CD";
        
        cartao.setTipoCartao(dto.getTipoCartao());
        cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
        cartao.setNumeroCartao(numCartao + sufixo);
        cartao.setStatus(true);
        cartao.setConta(contaDoUser);
        cartao.setTipoConta(contaDoUser.getTipoConta());
        cartao.setCategoriaConta(contaDoUser.getCategoriaConta());

        if(cartao instanceof CartaoCredito cc) {
            cc.setLimiteCreditoPreAprovado(contaDoUser.getCategoriaConta() == CategoriaConta.PREMIUM ? 
                new BigDecimal("10000.00") : new BigDecimal("5000.00"));
        } else if(cartao instanceof CartaoDebito cd) {
            cd.setLimiteDiarioTransacao(new BigDecimal("2000.00"));
        }

        return cartaoDao.saveWithRelations(cartao);
    }
	
	
	@Transactional
	public List<Cartao> getCartoes(Authentication authentication) {
	    
		String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode ver todos os cartões
	        return cartaoDao.findAll();
	    } else if ("ROLE_BASIC".equals(role)) {
	        String username = authentication.getName();
	        return cartaoDao.findByUsername(username); // Retorna todos os cartões desse usuário
	    } else {
	        throw new RuntimeException("Você não tem permissão para acessar a lista de cartões.");
	    }
	}

	@Transactional
	public Cartao getCartoesById(Long id, Authentication authentication) throws AccessDeniedException {
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    String username = authentication.getName(); // Pegando o username do logado

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode acessar qualquer cartão por ID
	        return cartaoDao.findById(id)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode acessar o cartão dele mesmo
	        return cartaoDao.findByIdAndUsername(id, username)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado ou você não tem permissão para acessá-lo."));
	    } else {
	        throw new AccessDeniedException("Você não tem permissão para acessar esse cartão.");
	    }
	}


	@Transactional
	public Cartao updateSenha(Long cartaoId, CartaoUpdateDTO dto, Authentication authentication) throws AccessDeniedException {
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    String username = authentication.getName();

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode atualizar qualquer cartão
	        Cartao cartao = cartaoDao.findById(cartaoId)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado"));

	        // Atualiza a senha
	        cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
	        return cartaoDao.save(cartao);

	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode atualizar o cartão dele
	        Cartao cartao = cartaoDao.findByIdAndUsername(cartaoId, username)
	            .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado ou você não tem permissão para acessá-lo"));

	        if (cartao.getConta().getCliente().getId() != dto.getIdCliente()) {
	            throw new ClienteNaoEncontradoException("Você não tem permissão para alterar esse cartão.");
	        }

	        if (!cartao.isStatus()) {
	            throw new PermissaoNegadaException("Não é possível atualizar a senha de um cartão desativado.");
	        }

	        cartao.setSenha(passwordEncoder.encode(dto.getSenha()));
	        return cartaoDao.save(cartao);
	        
	    } else {
	        throw new AccessDeniedException("Você não tem permissão para atualizar a senha desse cartão.");
	    }
	}

	
	

	
	
	
	
	
	
}
