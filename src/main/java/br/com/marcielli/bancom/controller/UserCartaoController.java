package br.com.marcielli.bancom.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.bancom.dto.CartaoCreateDTO;
import br.com.marcielli.bancom.dto.CartaoUpdateDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarLimiteCartaoCreditoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarLimiteCartaoDebitoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoPagCartaoDTO;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.Fatura;
import br.com.marcielli.bancom.service.UserCartaoService;

@RestController
public class UserCartaoController {

	private final UserCartaoService cartaoService;

	public UserCartaoController(UserCartaoService cartaoService) {
		this.cartaoService = cartaoService;
	}

	// ADMIN pode criar cartao pra ele e pra todos
	// BASIC só pode criar cartao pra ele mesmo
	@PostMapping("/cartoes")
	public ResponseEntity<Object> criarCartao(@RequestBody CartaoCreateDTO cartao, Authentication authentication) {

		Cartao cartaoSalvo = cartaoService.salvar(cartao, authentication);

		if (cartaoSalvo != null) {
			return ResponseEntity.status(HttpStatus.CREATED).body(cartaoSalvo);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Tente novamente mais tarde.");
		}
	}

	// ADMIN pode ver todos os cartões
	// BASIC pode ver apenas os cartões com o id dele
	@GetMapping("/cartoes")
	public ResponseEntity<List<Cartao>> listCartoes(Authentication authentication) {
		var cartoes = cartaoService.getCartoes(authentication);
		return ResponseEntity.status(HttpStatus.OK).body(cartoes);
	}

	// ADMIN pode ver todos os cartões por id, dele e de qualquer usuario
	// BASIC só pode ver o cartão com id dele
	@GetMapping("/cartoes/{id}")
	public ResponseEntity<Cartao> getCartoesById(@PathVariable Long id, Authentication authentication)
			throws AccessDeniedException {

		Cartao cartao = cartaoService.getCartoesById(id, authentication);

		return ResponseEntity.ok(cartao);
	}

	// ADMIN pode atualizar de qualquer cartão
	// BASIC pode atualizar somente do cartão com o id dele
	@PutMapping("/cartoes/{id}")
	public ResponseEntity<?> atualizarSenha(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto,
			Authentication authentication) throws AccessDeniedException {

		Cartao cartaoAtualizado = cartaoService.updateSenha(id, dto, authentication);

		if (cartaoAtualizado != null) {
			return ResponseEntity.ok("Senha do cartão atualizada com sucesso");
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	// Ativar cartão
	@PutMapping("/cartoes/{id}/ativar")
	public ResponseEntity<String> ativarCartao(@PathVariable("id") Long id, Authentication authentication) {
		boolean cartaoAtivado = cartaoService.ativarCartao(id, authentication);
		if (cartaoAtivado) {
			return ResponseEntity.status(HttpStatus.OK).body("Cartão ativado com sucesso!");
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cartão não encontrado!");
	}

	// ADMIN pode deletar dele e de outras pessoas
	// BASIC só pode deletar dele
	@DeleteMapping("/cartoes/{id}")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, Authentication authentication) {

		boolean deletado = cartaoService.delete(id, authentication);

		if (deletado) {
			return ResponseEntity.ok("Cartão desativado com sucesso");
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	// Pagamentos

	// ADMIN pode fazer pagamento apenas com o cartão dele
	// BASIC pode fazer pagamento apenas com o cartão dele
	@PostMapping("/cartoes/{idContaReceber}/pagamento")
	public ResponseEntity<String> pagamentoCartao(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserCartaoPagCartaoDTO dto, Authentication authentication) {

		boolean pagamentoRealizado = cartaoService.pagCartao(idContaReceber, dto, authentication);

		if (pagamentoRealizado) {
			return new ResponseEntity<>("Pagamento realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	// ADMIN pode alterar limite do cartão de credito dele e de todos
	// BASIC não pode alterar limite
	@PutMapping("/cartoes/{cartaoId}/limite")
	public ResponseEntity<String> alterarLimiteCartaoCredito(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarLimiteCartaoCreditoDTO dto, Authentication authentication) {

		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoCredito(cartaoId, dto, authentication);

		if (limiteAtualizado != null) {

			return ResponseEntity.status(HttpStatus.OK).body("Limite do cartão de crédito alterado com suceso");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
		}

	}

	// ADMIN pode alterar limite dele e de todos
	// BASIC não pode alterar limite
	@PutMapping("/cartoes/{cartaoId}/limite-diario")
	public ResponseEntity<?> alterarLimiteCartaoDebito(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarLimiteCartaoDebitoDTO dto, Authentication authentication) {

		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoDebito(cartaoId, dto, authentication);

		if (limiteAtualizado != null) {
			return ResponseEntity.status(HttpStatus.OK).body("Limite do cartão de débito alterado com suceso");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
		}
	}

	// ADMIN pode ver de todos e dele
	// BASIC só pode ver dele
	@GetMapping("/cartoes/{cartaoId}/fatura")
	public ResponseEntity<?> getFaturaCartaoDeCredito(@PathVariable("cartaoId") Long cartaoId,
			Authentication authentication) {

		Fatura fatura = cartaoService.verFaturaCartaoCredito(cartaoId, authentication);

		if (fatura != null) {
			return ResponseEntity.status(HttpStatus.OK).body(fatura);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
		}
	}

	// ADMIN pode pagar de todos e dele
	// BASIC só pode pagar a propria fatura
	@PostMapping("/cartoes/{idCartao}/fatura/pagamento")
	public ResponseEntity<String> pagamentoFaturaCartaoCredito(@PathVariable("idCartao") Long idCartao,
			Authentication authentication) {

		boolean pagamentoFaturaOk = cartaoService.pagFaturaCartaoC(idCartao, authentication);

		if (pagamentoFaturaOk) {
			return new ResponseEntity<>("Fatura paga.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

}
