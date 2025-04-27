package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marcielli.BancoM.dto.security.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.security.SeguroCreateDTO;
import br.com.marcielli.BancoM.dto.security.SeguroUpdateDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.Seguro;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.entity.ValidacaoUsuarioAtivo;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoSeguro;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.PermissaoNegadaException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.SeguroRepository;
import br.com.marcielli.BancoM.repository.UserRepository;

@Service
public class UserSeguroService {

	private final SeguroRepository seguroRepository;
	private final UserRepository userRepository;
	private final ClienteRepository clienteRepository;

	public UserSeguroService(SeguroRepository seguroRepository, UserRepository userRepository,
			ClienteRepository clienteRepository) {
		this.seguroRepository = seguroRepository;
		this.userRepository = userRepository;
		this.clienteRepository = clienteRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro save(SeguroCreateDTO dto) {

		Cliente clienteAlvo = clienteRepository.findById(dto.idUsuario())
				.orElseThrow(() -> new ContaNaoEncontradaException("Cliente não encontrado"));

		Cartao cartao = clienteAlvo.getContas().stream().flatMap(conta -> conta.getCartoes().stream())
				.filter(c -> c.getId().equals(dto.idCartao())).findFirst()
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

		return seguroRepository.save(seguro);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro getSegurosById(Long id) {
		return seguroRepository.findById(id).orElse(null);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Seguro> getSeguros() {		
		return seguroRepository.findAll();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro update(Long seguroId, SeguroUpdateDTO dto) {

		Seguro seguro = seguroRepository.findById(seguroId)
				.orElseThrow(() -> new ContaNaoEncontradaException("Seguro não encontrado"));

		if (!seguro.getCartao().isStatus()) {
			throw new IllegalStateException("Não é possível atualizar um seguro de um cartão desativado.");
		}

		if (!seguro.getAtivo()) {
			throw new IllegalStateException("Não é possível atualizar um seguro inativo.");
		}

		if (!seguro.getCartao().getConta().getCliente().getId().equals(dto.idUsuario())) {
			throw new PermissaoNegadaException("Este seguro não pertence ao usuário informado");
		}

		clienteRepository.findById(dto.idUsuario())
				.orElseThrow(() -> new ContaNaoEncontradaException("Usuário não encontrado"));

		seguro.setTipo(dto.tipo());

		if (dto.tipo() == TipoSeguro.SEGURO_VIAGEM
				&& seguro.getCartao().getCategoriaConta() == CategoriaConta.PREMIUM) {
			seguro.setValorMensal(BigDecimal.ZERO);
		} else if (seguro.getValorMensal().compareTo(BigDecimal.ZERO) == 0) {
			seguro.setValorMensal(new BigDecimal("50.00"));
		}

		if (dto.tipo() == TipoSeguro.SEGURO_FRAUDE) {
			seguro.setValorApolice(new BigDecimal("5000.00"));
		}
		System.err.println("\n----------------------\n" + seguro);
		return seguroRepository.save(seguro);
	}

	@Transactional
	public boolean delete(Long id, CartaoUpdateDTO dto) {

		Seguro seguro = seguroRepository.findById(id)
				.orElseThrow(() -> new ContaNaoEncontradaException("Seguro não encontrado"));

		seguro.setAtivo(false);
		seguroRepository.save(seguro);
		return true;
	}

}
