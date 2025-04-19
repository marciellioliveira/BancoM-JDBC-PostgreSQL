package br.com.marcielli.BancoM.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.CartaoPagarFaturaDTO;
import br.com.marcielli.BancoM.dto.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.CartaoCreateTedDTO;
import br.com.marcielli.BancoM.dto.CartaoDeleteDTO;
import br.com.marcielli.BancoM.dto.CartaoMapper;
import br.com.marcielli.BancoM.dto.CartaoResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateLimiteDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateLimiteMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateLimiteResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateSenhaDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateSenhaMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateSenhaResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateStatusDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateStatusMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateStatusResponseDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.Fatura;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.service.CartaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/cartoes")
public class CartaoController {

	@Autowired
	private CartaoService cartaoService;

	@Autowired
	private CartaoMapper cartaoMapper;

	@Autowired
	private CartaoUpdateMapper cartaoUpdateMapper;

	@Autowired
	private CartaoUpdateLimiteMapper cartaoUpdateLimiteMapper;

	@Autowired
	private CartaoUpdateStatusMapper cartaoUpdateStatusMapper;

	@Autowired
	private CartaoUpdateSenhaMapper cartaoUpdateSenhaMapper;

	@PostMapping("") 
	public ResponseEntity<CartaoResponseDTO> adicionarCartao(@Valid @RequestBody CartaoCreateDTO cartaoCreateDTO) {

		Cartao cartaoGravado = cartaoService.save(cartaoCreateDTO);

		CartaoResponseDTO cartaoResponseDTO = cartaoMapper.toDTO(cartaoGravado);

		return ResponseEntity.status(HttpStatus.CREATED).body(cartaoResponseDTO);
	}

	@GetMapping("/{cartaoId}") 
	public Optional<CartaoResponseDTO> getCartaoById(@PathVariable("cartaoId") Long cartaoId,
			HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return Optional.empty(); // Retorna um Optional vazio em caso de acesso não autorizado
		}

		// Mapeia o cartão para um DTO
		CartaoResponseDTO cartaoResponseDTO = cartaoMapper.toDTO(cartao);

		return Optional.of(cartaoResponseDTO); // Retorna o DTO dentro de um Optional

	}

	@PutMapping("/{cartaoId}") 
	public ResponseEntity<CartaoUpdateResponseDTO> atualizar(@PathVariable("cartaoId") Long cartaoId,
			@Valid @RequestBody CartaoUpdateDTO cartaoUpdateDTO, HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		Cartao cartaoAtualizado = cartaoService.update(cartaoId, cartaoUpdateDTO);
		CartaoUpdateResponseDTO cartaoResponseDTO = cartaoUpdateMapper.toDTO(cartaoAtualizado);
		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}

	@DeleteMapping("/{cartaoId}") 
	public ResponseEntity<String> deletar(@PathVariable("cartaoId") Long cartaoId,
			@Valid @RequestBody CartaoDeleteDTO cartaoDeleteDTO, HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
		}

		boolean cartaoDeletado = cartaoService.deleteCartao(cartaoId, cartaoDeleteDTO);
		if (cartaoDeletado) {
			return new ResponseEntity<>("Cartão deletado com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("") 
	public ResponseEntity<List<Cartao>> getContas(HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Verifica se o cliente logado tem permissão para listar os cartões
		if (!temPermissaoListarCartoes(clienteIdToken, auth)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		List<Cartao> cartoes = cartaoService.getAll();
		return new ResponseEntity<>(cartoes, HttpStatus.OK);
	}

	@PostMapping("/{idContaReceber}/pagamento")
	public ResponseEntity<String> pagamentoCartao(@PathVariable("idContaReceber") Long idContaReceber,
			@Valid @RequestBody CartaoCreateTedDTO cartaoTransCreateDTO, HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Verifica se o cliente logado tem permissão para realizar o pagamento
		if (!temPermissaoPagamentoCartao(idContaReceber, clienteIdToken, auth)) {
			return new ResponseEntity<>("Acesso negado", HttpStatus.FORBIDDEN);
		}

		// Realiza o pagamento do cartão
		boolean pagamentoRealizado = cartaoService.pagCartao(idContaReceber, cartaoTransCreateDTO);

		if (pagamentoRealizado) {
			return new ResponseEntity<>("Pagamento realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@PutMapping("/{cartaoId}/limite") 
	public ResponseEntity<CartaoUpdateLimiteResponseDTO> alterarLimiteCartaoCredito(
			@PathVariable("cartaoId") Long cartaoId, @Valid @RequestBody CartaoUpdateLimiteDTO cartaoUpdateLimiteDTO,
			HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoCredito(cartaoId, cartaoUpdateLimiteDTO);
		CartaoUpdateLimiteResponseDTO cartaoResponseDTO = cartaoUpdateLimiteMapper.toDTO(limiteAtualizado);
		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}

	@PutMapping("/{cartaoId}/status") 
	public ResponseEntity<CartaoUpdateStatusResponseDTO> alterarStatusCartao(@PathVariable("cartaoId") Long cartaoId,
			@Valid @RequestBody CartaoUpdateStatusDTO cartaoUpdateStatusDTO, HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		Cartao statusAtualizado = cartaoService.alterarStatusC(cartaoId, cartaoUpdateStatusDTO);
		CartaoUpdateStatusResponseDTO cartaoResponseDTO = cartaoUpdateStatusMapper.toDTO(statusAtualizado);
		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}

	@PutMapping("/{cartaoId}/senha") 
	public ResponseEntity<CartaoUpdateSenhaResponseDTO> alterarSenhaCartao(@PathVariable("cartaoId") Long cartaoId,
			@Valid @RequestBody CartaoUpdateSenhaDTO cartaoUpdateSenhaDTO, HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		Cartao statusAtualizado = cartaoService.alterarSenhaC(cartaoId, cartaoUpdateSenhaDTO);
		CartaoUpdateSenhaResponseDTO cartaoResponseDTO = cartaoUpdateSenhaMapper.toDTO(statusAtualizado);
		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}

	@GetMapping("/{cartaoId}/fatura") 
	public ResponseEntity<Fatura> getFaturaCartaoDeCredito(@PathVariable("cartaoId") Long cartaoId,
			HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		// Obtém a fatura do cartão
		Fatura fatura = cartaoService.getFaturaCartaoDeCreditoService(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Não existe fatura para esse cartão."));

		return ResponseEntity.status(HttpStatus.OK).body(fatura);
	}

	@PostMapping("/{idCartao}/fatura/pagamento") 
	public ResponseEntity<String> pagamentoFaturaCartaoCredito(@PathVariable("idCartao") Long idCartao,
			@RequestBody @Valid CartaoPagarFaturaDTO cartaoPagarFaturaDTO, HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(idCartao)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return new ResponseEntity<>("Acesso negado", HttpStatus.FORBIDDEN);
		}

		// Realiza o pagamento da fatura
		boolean pagamentoFaturaOk = cartaoService.pagFaturaCartaoC(idCartao, cartaoPagarFaturaDTO);

		if (pagamentoFaturaOk) {
			return new ResponseEntity<>("Fatura paga.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@PutMapping("/{cartaoId}/limite-diario") 
	public ResponseEntity<CartaoUpdateLimiteResponseDTO> alterarLimiteCartaoDebito(
			@PathVariable("cartaoId") Long cartaoId, @Valid @RequestBody CartaoUpdateLimiteDTO cartaoUpdateLimiteDTO,
			HttpServletRequest request) {
		Long clienteIdToken = (Long) request.getAttribute("clienteId");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Obtém o cartão pelo ID
		Cartao cartao = cartaoService.getCartaoById(cartaoId)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado pode acessar este cartão
		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		// Realiza a alteração do limite diário
		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoDebito(cartaoId, cartaoUpdateLimiteDTO);

		// Retorna a resposta com o novo limite
		CartaoUpdateLimiteResponseDTO cartaoResponseDTO = cartaoUpdateLimiteMapper.toDTO(limiteAtualizado);
		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}

	// Verificação de autorização de acesso
	private boolean podeAcessarCliente(Long clienteId, Long clienteIdToken, Authentication auth) {
		return clienteId.equals(clienteIdToken)
				|| auth.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
	}

	// Verifica se o cliente tem permissão para listar cartões
	private boolean temPermissaoListarCartoes(Long clienteIdToken, Authentication auth) {
		// Verifica se o cliente logado é o mesmo do clienteIdToken ou se é um
		// administrador
		return auth.getAuthorities().stream()
				.anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN") || clienteIdToken.equals(clienteIdToken));
	}

	// Verifica se o cliente tem permissão para realizar o pagamento do cartão
	private boolean temPermissaoPagamentoCartao(Long idContaReceber, Long clienteIdToken, Authentication auth) {
		// Verifica se o cliente logado é o mesmo do clienteIdToken ou se é um
		// administrador
		Cartao cartao = cartaoService.getCartaoById(idContaReceber)
				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));

		// Verifica se o cliente logado é o dono do cartão ou se é um administrador
		return auth.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN")
				|| cartao.getConta().getCliente().getId().equals(clienteIdToken));
	}
}
