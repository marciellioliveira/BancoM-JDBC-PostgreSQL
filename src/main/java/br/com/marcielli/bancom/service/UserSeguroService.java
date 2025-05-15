package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
import br.com.marcielli.bancom.dto.security.ApoliceResponseDTO;
import br.com.marcielli.bancom.dto.security.SeguroCreateDTO;
import br.com.marcielli.bancom.dto.security.SeguroUpdateDTO;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Seguro;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoSeguro;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;
import br.com.marcielli.bancom.exception.PermissaoNegadaException;
import br.com.marcielli.bancom.exception.SeguroNaoEncontradoException;
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

		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
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

		Cartao cartao = clienteAlvo.getContas().stream().filter(conta -> conta.getCartoes() != null)
				.flatMap(conta -> conta.getCartoes().stream()).filter(c -> c.getId().equals(dto.idCartao())).findFirst()
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

	@Transactional
	public List<Seguro> getSeguros(Authentication authentication) {
		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		if ("ROLE_ADMIN".equals(role)) {
			// Admin pode ver todos os seguros
			return seguroDao.findAll();
		} else if ("ROLE_BASIC".equals(role)) {
			String username = authentication.getName();
			return seguroDao.findByUsername(username);
		} else {
			throw new RuntimeException("Você não tem permissão para acessar a lista de seguros.");
		}
	}

	@Transactional
	public Seguro getSegurosById(Long id, Authentication authentication) throws AccessDeniedException {
		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		String username = authentication.getName();

		if ("ROLE_ADMIN".equals(role)) {
			// Admin pode acessar qualquer seguro
			return seguroDao.findById(id).orElseThrow(() -> new SeguroNaoEncontradoException("Seguro não encontrado."));
		} else if ("ROLE_BASIC".equals(role)) {

			return seguroDao.findByIdAndUsername(id, username).orElseThrow(() -> new SeguroNaoEncontradoException(
					"Seguro não encontrado ou você não tem permissão para acessá-lo."));
		} else {
			throw new AccessDeniedException("Você não tem permissão para acessar esse seguro.");
		}
	}

	@Transactional
	public ApoliceResponseDTO gerarApoliceEletronica(Long seguroId, Authentication authentication) {
		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		String username = authentication.getName();

		Seguro seguro;

		if ("ROLE_ADMIN".equals(role)) {
			seguro = seguroDao.findById(seguroId).orElse(null);
		} else if ("ROLE_BASIC".equals(role)) {
			seguro = seguroDao.findByIdAndUsername(seguroId, username).orElse(null);
		} else {
			return null;
		}

		if (seguro == null) {
			return null;
		}

		String numeroApolice = "AP-" + LocalDate.now().getYear() + "-"
				+ UUID.randomUUID().toString().substring(0, 8).toUpperCase();

		String condicoes = seguro.getTipo() == TipoSeguro.SEGURO_FRAUDE
				? "Acionável em até 48h após transação não reconhecida."
				: "Cobre até R$ 10.000 em extravio de bagagem (comunicar em até 24h).";

		return new ApoliceResponseDTO(numeroApolice, LocalDate.now(), seguro.getCartao().getNumeroCartao(),
				seguro.getCartao().getConta().getCliente().getNome(), seguro.getValorApolice(), condicoes,
				seguro.getAtivo());
	}
	
	

	@Transactional
	public Seguro update(Long seguroId, SeguroUpdateDTO dto, Authentication authentication) {

	    String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
	            .orElse("");

	    String username = authentication.getName();

	    Seguro seguro;
	    if ("ROLE_ADMIN".equals(role)) {
	        seguro = seguroDao.findById(seguroId)
	                .orElseThrow(() -> new ContaNaoEncontradaException("Seguro não encontrado"));
	    } else if ("ROLE_BASIC".equals(role)) {
	        seguro = seguroDao.findByIdAndUsername(seguroId, username)
	                .orElseThrow(() -> new ContaNaoEncontradaException("Seguro não encontrado ou acesso negado"));
	    } else {
	        throw new PermissaoNegadaException("Acesso negado");
	    }

	    if (!seguro.getCartao().isStatus()) {
	        throw new IllegalStateException("Não é possível atualizar um seguro de um cartão desativado.");
	    }

	    if (!seguro.getAtivo()) {
	        throw new IllegalStateException("Não é possível atualizar um seguro inativo.");
	    }

	    if ("ROLE_BASIC".equals(role)
	            && !seguro.getCartao().getConta().getCliente().getUser().getUsername().equals(username)) {
	        throw new PermissaoNegadaException("Este seguro não pertence ao usuário autenticado");
	    }

	    clienteDao.findById(dto.idUsuario())
	            .orElseThrow(() -> new ContaNaoEncontradaException("Usuário não encontrado"));

	    seguro.setTipo(dto.tipo());

	    if (seguro.getTipo() == null) {
	        throw new IllegalArgumentException("Tipo do seguro não pode ser nulo.");
	    }

	    if (dto.tipo() == TipoSeguro.SEGURO_VIAGEM
	            && seguro.getCartao().getCategoriaConta() == CategoriaConta.PREMIUM) {
	        seguro.setValorMensal(BigDecimal.ZERO);
	    } else if (seguro.getValorMensal().compareTo(BigDecimal.ZERO) == 0) {
	        seguro.setValorMensal(new BigDecimal("50.00"));
	    }

	    if (dto.tipo() == TipoSeguro.SEGURO_FRAUDE) {
	        seguro.setValorApolice(new BigDecimal("5000.00"));
	    } else {
	        seguro.setValorApolice(null); 
	    }

	    return seguroDao.update(seguro);
	}

	@Transactional
	public boolean delete(Long id, Authentication authentication) throws AccessDeniedException {

		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		String username = authentication.getName();

		Seguro seguro;

		if ("ROLE_ADMIN".equals(role)) {
			seguro = seguroDao.findById(id).orElse(null);
		} else if ("ROLE_BASIC".equals(role)) {
			seguro = seguroDao.findByIdAndUsername(id, username).orElse(null);
		} else {
			throw new AccessDeniedException("Usuário sem permissão para deletar seguro");
		}

		if (seguro == null) {
			throw new SeguroNaoEncontradoException("Seguro não encontrado ou sem permissão");
		}

		if (!seguro.getAtivo()) {
			throw new IllegalStateException("Seguro já está inativo");
		}

		seguro.setAtivo(false);
		seguroDao.desativarSeguro(seguro.getId());
		return true;
	}

}
