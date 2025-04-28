package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.security.ApoliceResponseDTO;
import br.com.marcielli.BancoM.dto.security.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.security.SeguroCreateDTO;
import br.com.marcielli.BancoM.dto.security.SeguroUpdateDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Seguro;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoSeguro;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.PermissaoNegadaException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.SeguroRepository;

@Service
public class UserSeguroService {

	private final SeguroRepository seguroRepository;
	private final ClienteRepository clienteRepository;

	public UserSeguroService(SeguroRepository seguroRepository,
			ClienteRepository clienteRepository) {
		this.seguroRepository = seguroRepository;
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
	
	
	public ApoliceResponseDTO gerarApoliceEletronica(Long seguroId) {
	    Seguro seguro = seguroRepository.findById(seguroId)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Seguro não encontrado"));

	    String numeroApolice = "AP-" + LocalDate.now().getYear() + "-" + 
	                          UUID.randomUUID().toString().substring(0, 8).toUpperCase();

	    String condicoes = seguro.getTipo() == TipoSeguro.SEGURO_FRAUDE ?
	            "Acionável em até 48h após transação não reconhecida." :
	            "Cobre até R$ 10.000 em extravio de bagagem (comunicar em até 24h).";

	    return new ApoliceResponseDTO(
	            numeroApolice,
	            LocalDate.now(),
	            seguro.getCartao().getNumeroCartao(),
	            seguro.getCartao().getConta().getCliente().getNome(),
	            seguro.getValorApolice(),
	            condicoes,
	            seguro.getAtivo()
	    );
	}
	

}
