package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dao.CartaoDao;
import br.com.marcielli.bancom.dao.ClienteDao;
import br.com.marcielli.bancom.dao.ContaDao;
import br.com.marcielli.bancom.dao.FaturaDao;
import br.com.marcielli.bancom.dao.PagamentoFaturaDao;
import br.com.marcielli.bancom.dao.SeguroDao;
import br.com.marcielli.bancom.dao.TransferenciaDao;
import br.com.marcielli.bancom.dao.UserDao;
import br.com.marcielli.bancom.dto.security.SeguroCreateDTO;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Seguro;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoSeguro;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;
import br.com.marcielli.bancom.exception.PermissaoNegadaException;
import br.com.marcielli.bancom.utils.GerarNumeros;

@Service
public class UserSeguroService {
	
	private final UserDao userDao;
	private final ClienteDao clienteDao;
	private final SeguroDao seguroDao;

	private static final Logger logger = LoggerFactory.getLogger(UserSeguroService.class);

	public UserSeguroService(ClienteDao clienteDao, UserDao userDao, TransferenciaDao transferenciaDao,
			ContaDao contaDao, CartaoDao cartaoDao, GerarNumeros gerarNumeros, BCryptPasswordEncoder passwordEncoder,
			FaturaDao faturaDao, PagamentoFaturaDao pagFaturaDao, SeguroDao seguroDao) {
		this.clienteDao = clienteDao;	
		this.seguroDao = seguroDao;
		this.userDao = userDao;
	}
	
	
	
	@Transactional
	public Seguro save(SeguroCreateDTO dto, Authentication authentication) {
	
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("");

	    String username = authentication.getName();

	    User loggedInUser = userDao.findByUsername(username)
	        .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

	    if ("ROLE_BASIC".equals(role) && !dto.idUsuario().equals(loggedInUser.getId().longValue())) {
	        throw new PermissaoNegadaException("Usuário BASIC não tem permissão para criar seguro para outro cliente.");
	    }

	    Cliente clienteAlvo = clienteDao.findClienteWithDetails(dto.idUsuario());

	    if (clienteAlvo == null) {
	        throw new ClienteNaoEncontradoException("Cliente não encontrado.");
	    }

	    if (!clienteAlvo.isClienteAtivo()) {
	        throw new ClienteNaoEncontradoException("Usuário está desativado. Não é possível criar um seguro.");
	    }

	    Cartao cartao = clienteAlvo.getContas().stream()
	        .filter(conta -> conta.getCartoes() != null)
	        .flatMap(conta -> conta.getCartoes().stream())
	        .filter(c -> c.getId().equals(dto.idCartao()))
	        .findFirst()
	        .orElseThrow(() -> new PermissaoNegadaException("Cartão não pertence ao cliente informado."));

	    if (!cartao.isStatus()) {
	        throw new IllegalStateException("Não é possível criar seguro para um cartão desativado.");
	    }

	    Seguro seguro = new Seguro();
	    seguro.setTipo(dto.tipoSeguro());
	    seguro.setAtivo(true);
	    seguro.setCartao(cartao);

	    if (dto.tipoSeguro() == TipoSeguro.SEGURO_VIAGEM && cartao.getCategoriaConta() == CategoriaConta.PREMIUM) {
	        seguro.setValorMensal(BigDecimal.ZERO);
	    } else {
	        seguro.setValorMensal(new BigDecimal("50.00"));
	    }

	    if (dto.tipoSeguro() == TipoSeguro.SEGURO_FRAUDE) {
	        seguro.setValorApolice(new BigDecimal("5000.00"));
	    }

	    return seguroDao.save(seguro);
	}






}
