package br.com.marcielli.BancoM.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.security.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.security.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoAlterarLimiteCartaoCreditoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoAlterarLimiteCartaoDebitoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoAlterarSenhaCartaoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoAlterarStatusCartaoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoPagCartaoDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoResponseDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.CartaoCredito;
import br.com.marcielli.BancoM.entity.CartaoDebito;
import br.com.marcielli.BancoM.entity.Fatura;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.PermissaoNegadaException;
import br.com.marcielli.BancoM.repository.UserRepository;
import br.com.marcielli.BancoM.service.UserCartaoService;

@RestController
public class UserCartaoController {

	private final UserCartaoService cartaoService;
	private final UserRepository userRepository;

	public UserCartaoController(UserCartaoService cartaoService, UserRepository userRepository) {
		this.cartaoService = cartaoService;
		this.userRepository = userRepository;
	}

	@PostMapping("/cartoes")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> createCartao(@RequestBody CartaoCreateDTO dto, JwtAuthenticationToken token) {

		Cartao cartaoAdicionado = cartaoService.save(dto, token);

		if (cartaoAdicionado != null) {
			UserCartaoResponseDTO response = new UserCartaoResponseDTO();

			response.setId(cartaoAdicionado.getId());
			response.setIdConta(cartaoAdicionado.getConta().getId());
			response.setTipoConta(cartaoAdicionado.getConta().getTipoConta());
			response.setCategoriaConta(cartaoAdicionado.getCategoriaConta());
			response.setTipoCartao(cartaoAdicionado.getTipoCartao());
			response.setNumeroCartao(cartaoAdicionado.getNumeroCartao());
			response.setStatus(cartaoAdicionado.isStatus());
			response.setSenha(cartaoAdicionado.getSenha());

			if (cartaoAdicionado instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			if (cartaoAdicionado instanceof CartaoDebito cd) {
				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
			}

			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("/cartoes")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<User>> listUsers() {
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}

	@GetMapping("/cartoes/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> getCartoesById(@PathVariable("id") Long id) {

		Cartao cartao = cartaoService.getCartoesById(id);

		if (cartao == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}

		boolean isAdmin = cartao.getConta().getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserCartaoResponseDTO response = new UserCartaoResponseDTO();

			response.setId(id);
			response.setTipoConta(cartao.getConta().getTipoConta());
			response.setCategoriaConta(cartao.getCategoriaConta());
			response.setTipoCartao(cartao.getTipoCartao());
			response.setNumeroCartao(cartao.getNumeroCartao());
			response.setStatus(cartao.isStatus());
			response.setSenha(cartao.getSenha());

			if (cartao instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			if (cartao instanceof CartaoDebito cd) {
				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
			}

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}
	}

	@PutMapping("/cartoes/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> atualizarSenha(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto,
			JwtAuthenticationToken token) {
		try {
			Cartao cartaoAtualizado = cartaoService.updateSenha(id, dto, token);

			return ResponseEntity.ok("Senha do cartão atualizada com sucesso");

		} catch (ContaNaoEncontradaException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (PermissaoNegadaException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/cartoes/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto,
			JwtAuthenticationToken token) {

		try {
			boolean deletado = cartaoService.delete(id, dto, token);
			return deletado ? ResponseEntity.ok("Cartão desativado com sucesso!")
					: ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cartão não encontrado.");
		} catch (ContaNaoEncontradaException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (PermissaoNegadaException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// Pagamentos
	@PostMapping("/cartoes/{idContaReceber}/pagamento")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> pagamentoCartao(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserCartaoPagCartaoDTO dto) {

		boolean pagamentoRealizado = cartaoService.pagCartao(idContaReceber, dto);

		if (pagamentoRealizado) {
			return new ResponseEntity<>("Pagamento realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@PutMapping("/cartoes/{cartaoId}/limite")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<?> alterarLimiteCartaoCredito(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarLimiteCartaoCreditoDTO dto) {

		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoCredito(cartaoId, dto);

		boolean isAdmin = limiteAtualizado.getConta().getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserCartaoResponseDTO response = new UserCartaoResponseDTO();

			response.setId(cartaoId);

			if (limiteAtualizado instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			response.setTipoCartao(limiteAtualizado.getTipoCartao());
			response.setNumeroCartao(limiteAtualizado.getNumeroCartao());
			response.setCategoriaConta(limiteAtualizado.getCategoriaConta());
			response.setStatus(limiteAtualizado.isStatus());
			response.setSenha(limiteAtualizado.getSenha());
			response.setTipoConta(limiteAtualizado.getTipoConta());

			if (limiteAtualizado instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			if (limiteAtualizado instanceof CartaoDebito cd) {
				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
			}

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
		}

	}

	@PutMapping("/cartoes/{cartaoId}/status")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<?> alterarStatusCartao(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarStatusCartaoDTO dto) {

		Cartao limiteAtualizado = cartaoService.alterarStatusC(cartaoId, dto);

		boolean isAdmin = limiteAtualizado.getConta().getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserCartaoResponseDTO response = new UserCartaoResponseDTO();

			response.setId(cartaoId);

			if (limiteAtualizado instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			response.setTipoCartao(limiteAtualizado.getTipoCartao());
			response.setNumeroCartao(limiteAtualizado.getNumeroCartao());
			response.setCategoriaConta(limiteAtualizado.getCategoriaConta());
			response.setStatus(limiteAtualizado.isStatus());
			response.setSenha(limiteAtualizado.getSenha());
			response.setTipoConta(limiteAtualizado.getTipoConta());

			if (limiteAtualizado instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			if (limiteAtualizado instanceof CartaoDebito cd) {
				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
			}

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
		}

	}

	@PutMapping("/cartoes/{cartaoId}/senha")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> alterarSenhaCartao(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarSenhaCartaoDTO dto) {

		Cartao limiteAtualizado = cartaoService.alterarSenhaC(cartaoId, dto);

		boolean isAdmin = limiteAtualizado.getConta().getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserCartaoResponseDTO response = new UserCartaoResponseDTO();

			response.setId(cartaoId);

			if (limiteAtualizado instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			response.setTipoCartao(limiteAtualizado.getTipoCartao());
			response.setNumeroCartao(limiteAtualizado.getNumeroCartao());
			response.setCategoriaConta(limiteAtualizado.getCategoriaConta());
			response.setStatus(limiteAtualizado.isStatus());
			response.setSenha(limiteAtualizado.getSenha());
			response.setTipoConta(limiteAtualizado.getTipoConta());

			if (limiteAtualizado instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			if (limiteAtualizado instanceof CartaoDebito cd) {
				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
			}

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
		}

	}

	@PutMapping("/cartoes/{cartaoId}/limite-diario")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<?> alterarLimiteCartaoDebito(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarLimiteCartaoDebitoDTO dto) {

		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoDebito(cartaoId, dto);

		boolean isAdmin = limiteAtualizado.getConta().getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserCartaoResponseDTO response = new UserCartaoResponseDTO();

			response.setId(cartaoId);

			if (limiteAtualizado instanceof CartaoCredito cc) {
				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
			}

			if (limiteAtualizado instanceof CartaoDebito cd) {
				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
			}

			response.setTipoCartao(limiteAtualizado.getTipoCartao());
			response.setNumeroCartao(limiteAtualizado.getNumeroCartao());
			response.setCategoriaConta(limiteAtualizado.getCategoriaConta());
			response.setStatus(limiteAtualizado.isStatus());
			response.setSenha(limiteAtualizado.getSenha());
			response.setTipoConta(limiteAtualizado.getTipoConta());

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
		}

	}

	@GetMapping("/cartoes/{cartaoId}/fatura")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> getFaturaCartaoDeCredito(@PathVariable("cartaoId") Long cartaoId) {

		Fatura fatura = cartaoService.getFaturaCartaoDeCreditoService(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Não existe fatura para esse cartão."));

		return ResponseEntity.status(HttpStatus.OK).body(fatura);
	}

	@PostMapping("/cartoes/{idCartao}/fatura/pagamento")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> pagamentoFaturaCartaoCredito(@PathVariable("idCartao") Long idCartao) {

		boolean pagamentoFaturaOk = cartaoService.pagFaturaCartaoC(idCartao);

		if (pagamentoFaturaOk) {
			return new ResponseEntity<>("Fatura paga.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

}
