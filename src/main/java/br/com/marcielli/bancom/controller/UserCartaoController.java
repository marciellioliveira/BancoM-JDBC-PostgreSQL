package br.com.marcielli.bancom.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marcielli.bancom.dto.CartaoCreateDTO;
import br.com.marcielli.bancom.dto.CartaoUpdateDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarLimiteCartaoCreditoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarLimiteCartaoDebitoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarSenhaCartaoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoAlterarStatusCartaoDTO;
import br.com.marcielli.bancom.dto.security.UserCartaoPagCartaoDTO;
import br.com.marcielli.bancom.entity.Cartao;

public class UserCartaoController {

	//private final UserCartaoService cartaoService;

//	public UserCartaoController(UserCartaoService cartaoService) {
//		this.cartaoService = cartaoService;
//	}

	//ADMIN pode criar cartao pra ele e pra todos
	//BASIC só pode criar cartao pra ele mesmo
	@PostMapping("/cartoes")
	public ResponseEntity<?> createCartao(@RequestBody CartaoCreateDTO dto) {
		return null;

//		Cartao cartaoAdicionado = cartaoService.save(dto);
//
//		if (cartaoAdicionado != null) {
//			UserCartaoResponseDTO response = new UserCartaoResponseDTO();
//
//			response.setId(cartaoAdicionado.getId());
//			response.setIdConta(cartaoAdicionado.getConta().getId());
//			response.setTipoConta(cartaoAdicionado.getConta().getTipoConta());
//			response.setCategoriaConta(cartaoAdicionado.getCategoriaConta());
//			response.setTipoCartao(cartaoAdicionado.getTipoCartao());
//			response.setNumeroCartao(cartaoAdicionado.getNumeroCartao());
//			response.setStatus(cartaoAdicionado.isStatus());
//			response.setSenha(cartaoAdicionado.getSenha());
//
//			if (cartaoAdicionado instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			if (cartaoAdicionado instanceof CartaoDebito cd) {
//				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
//			}
//
//			return ResponseEntity.status(HttpStatus.CREATED).body(response);
//		} else {
//			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
//		}
	}

	//ADMIN pode ver todos os cartões
	//BASIC pode ver apenas os cartões com o id dele
	@GetMapping("/cartoes")
	public ResponseEntity<List<Cartao>> listCartoes() {
		return null;
//		var cartoes = cartaoService.getCartoes();
//		return ResponseEntity.status(HttpStatus.OK).body(cartoes);
	}


	//ADMIN pode ver todos os cartões por id, dele e de qualquer usuario
	//BASIC só pode ver o cartão com id dele
	@GetMapping("/cartoes/{id}")
	public ResponseEntity<?> getCartoesById(@PathVariable("id") Long id) {
		return null;

//		Cartao cartao = cartaoService.getCartoesById(id);
//
//		if (cartao != null) {
//			UserCartaoResponseDTO response = new UserCartaoResponseDTO();
//
//			response.setId(id);
//			response.setTipoConta(cartao.getConta().getTipoConta());
//			response.setCategoriaConta(cartao.getCategoriaConta());
//			response.setTipoCartao(cartao.getTipoCartao());
//			response.setNumeroCartao(cartao.getNumeroCartao());
//			response.setStatus(cartao.isStatus());
//			response.setSenha(cartao.getSenha());
//
//			if (cartao instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			if (cartao instanceof CartaoDebito cd) {
//				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
//			}
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
//		}
	}

	//ADMIN pode atualizar de qualquer cartão
	//BASIC pode atualizar somente do cartão com o id dele
	@PutMapping("/cartoes/{id}")
	public ResponseEntity<?> atualizarSenha(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto) {
		return null;

//		Cartao cartaoAtualizado = cartaoService.updateSenha(id, dto);
//
//		if (cartaoAtualizado != null) {
//			return ResponseEntity.ok("Senha do cartão atualizada com sucesso");
//		} else {
//			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
//		}
	}

	//ADMIN pode deletar dele e de outras pessoas
	//BASIC só pode deletar dele
	@DeleteMapping("/cartoes/{id}")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto) {
		return null;

//		boolean deletado = cartaoService.delete(id, dto);
//
//		if (deletado) {
//			return ResponseEntity.ok("Senha do cartão atualizada com sucesso");
//		} else {
//			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
//		}

	}

	// Pagamentos
	
	//ADMIN pode fazer pagamento apenas com o cartão dele
	//BASIC pode fazer pagamento apenas com o cartão dele
	@PostMapping("/cartoes/{idContaReceber}/pagamento")
	public ResponseEntity<String> pagamentoCartao(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserCartaoPagCartaoDTO dto) {
				return null;

//		boolean pagamentoRealizado = cartaoService.pagCartao(idContaReceber, dto);
//
//		if (pagamentoRealizado) {
//			return new ResponseEntity<>("Pagamento realizado com sucesso.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
	}

	//ADMIN pode alterar limite do cartão de credito dele e de todos
	//BASIC não pode alterar limite
	@PutMapping("/cartoes/{cartaoId}/limite")
	public ResponseEntity<?> alterarLimiteCartaoCredito(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarLimiteCartaoCreditoDTO dto) {
				return null;

//		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoCredito(cartaoId, dto);
//
//		if (limiteAtualizado != null) {
//			UserCartaoResponseDTO response = new UserCartaoResponseDTO();
//
//			response.setId(cartaoId);
//
//			if (limiteAtualizado instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			response.setTipoCartao(limiteAtualizado.getTipoCartao());
//			response.setNumeroCartao(limiteAtualizado.getNumeroCartao());
//			response.setCategoriaConta(limiteAtualizado.getCategoriaConta());
//			response.setStatus(limiteAtualizado.isStatus());
//			response.setSenha(limiteAtualizado.getSenha());
//			response.setTipoConta(limiteAtualizado.getTipoConta());
//
//			if (limiteAtualizado instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			if (limiteAtualizado instanceof CartaoDebito cd) {
//				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
//			}
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
//		}

	}

	//ADMIN pode alterar staus do cartão dele e de todosd 
	//BASIC nao pode alterar status
	@PutMapping("/cartoes/{cartaoId}/status")
	public ResponseEntity<?> alterarStatusCartao(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarStatusCartaoDTO dto) {
				return null;

//		Cartao limiteAtualizado = cartaoService.alterarStatusC(cartaoId, dto);
//
//		if (limiteAtualizado != null) {
//			UserCartaoResponseDTO response = new UserCartaoResponseDTO();
//
//			response.setId(cartaoId);
//
//			if (limiteAtualizado instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			response.setTipoCartao(limiteAtualizado.getTipoCartao());
//			response.setNumeroCartao(limiteAtualizado.getNumeroCartao());
//			response.setCategoriaConta(limiteAtualizado.getCategoriaConta());
//			response.setStatus(limiteAtualizado.isStatus());
//			response.setSenha(limiteAtualizado.getSenha());
//			response.setTipoConta(limiteAtualizado.getTipoConta());
//
//			if (limiteAtualizado instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			if (limiteAtualizado instanceof CartaoDebito cd) {
//				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
//			}
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
//		}

	}

	//ADMIN pode alterar senha dele e de todos
	//BASIC só pode alterar dele
	@PutMapping("/cartoes/{cartaoId}/senha")
	public ResponseEntity<?> alterarSenhaCartao(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarSenhaCartaoDTO dto) {
				return null;

//		Cartao limiteAtualizado = cartaoService.alterarSenhaC(cartaoId, dto);
//
//		if (limiteAtualizado != null) {
//			UserCartaoResponseDTO response = new UserCartaoResponseDTO();
//
//			response.setId(cartaoId);
//
//			if (limiteAtualizado instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			response.setTipoCartao(limiteAtualizado.getTipoCartao());
//			response.setNumeroCartao(limiteAtualizado.getNumeroCartao());
//			response.setCategoriaConta(limiteAtualizado.getCategoriaConta());
//			response.setStatus(limiteAtualizado.isStatus());
//			response.setSenha(limiteAtualizado.getSenha());
//			response.setTipoConta(limiteAtualizado.getTipoConta());
//
//			if (limiteAtualizado instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			if (limiteAtualizado instanceof CartaoDebito cd) {
//				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
//			}
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
//		}

	}

	//ADMIN pode alterar limite dele e de todos
	//BASIC não pode alterar limite
	@PutMapping("/cartoes/{cartaoId}/limite-diario")
	public ResponseEntity<?> alterarLimiteCartaoDebito(@PathVariable("cartaoId") Long cartaoId,
			@RequestBody UserCartaoAlterarLimiteCartaoDebitoDTO dto) {
				return null;

//		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoDebito(cartaoId, dto);
//
//		if (limiteAtualizado != null) {
//			UserCartaoResponseDTO response = new UserCartaoResponseDTO();
//
//			response.setId(cartaoId);
//
//			if (limiteAtualizado instanceof CartaoCredito cc) {
//				response.setLimiteCreditoPreAprovado(cc.getLimiteCreditoPreAprovado());
//			}
//
//			if (limiteAtualizado instanceof CartaoDebito cd) {
//				response.setLimiteDiarioTransacao(cd.getLimiteDiarioTransacao());
//			}
//
//			response.setTipoCartao(limiteAtualizado.getTipoCartao());
//			response.setNumeroCartao(limiteAtualizado.getNumeroCartao());
//			response.setCategoriaConta(limiteAtualizado.getCategoriaConta());
//			response.setStatus(limiteAtualizado.isStatus());
//			response.setSenha(limiteAtualizado.getSenha());
//			response.setTipoConta(limiteAtualizado.getTipoConta());
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
//		}

	}

	//ADMIN pode ver de todos e dele
	//BASIC só pode ver dele
	@GetMapping("/cartoes/{cartaoId}/fatura")
	public ResponseEntity<?> getFaturaCartaoDeCredito(@PathVariable("cartaoId") Long cartaoId) {
		return null;

//		Fatura fatura = cartaoService.getFaturaCartaoDeCreditoService(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Não existe fatura para esse cartão."));
//
//		return ResponseEntity.status(HttpStatus.OK).body(fatura);
	}

	//ADMIN só pode pagar a propria fatura
	//BASIC só pode pagar a propria fatura
	@PostMapping("/cartoes/{idCartao}/fatura/pagamento")
	public ResponseEntity<String> pagamentoFaturaCartaoCredito(@PathVariable("idCartao") Long idCartao) {
		return null;

//		boolean pagamentoFaturaOk = cartaoService.pagFaturaCartaoC(idCartao);
//
//		if (pagamentoFaturaOk) {
//			return new ResponseEntity<>("Fatura paga.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
	}
	
}
