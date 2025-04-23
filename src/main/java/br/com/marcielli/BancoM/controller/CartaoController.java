package br.com.marcielli.BancoM.controller;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.Random;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import br.com.marcielli.BancoM.dto.security.CartaoCreateDTO;
//import br.com.marcielli.BancoM.entity.Cartao;
//import br.com.marcielli.BancoM.entity.CartaoCredito;
//import br.com.marcielli.BancoM.entity.CartaoDebito;
//import br.com.marcielli.BancoM.entity.Cliente;
//import br.com.marcielli.BancoM.entity.Conta;
//import br.com.marcielli.BancoM.entity.User;
//import br.com.marcielli.BancoM.enuns.TipoCartao;
//import br.com.marcielli.BancoM.exception.ContaExisteNoBancoException;
//import br.com.marcielli.BancoM.repository.CartaoRepository;
//import br.com.marcielli.BancoM.repository.UserRepository;
//import br.com.marcielli.BancoM.service.UserCartaoService;
//import br.com.marcielli.BancoM.service.UserContaService;
//
//@RestController
//@RequestMapping("/cartoes")
//public class CartaoController {
//	
////	private final CartaoRepository cartaoRepository;
////	private final UserRepository userRepository;
//	private final UserCartaoService cartaoService;
//	
//
//	public CartaoController(UserCartaoService cartaoService) {
//		this.cartaoService = cartaoService;
//	}

//	public CartaoController(CartaoRepository cartaoRepository, UserRepository userRepository, UserCartaoService cartaoService) {
//		this.userRepository = userRepository;
//		this.cartaoRepository = cartaoRepository;
//		this.cartaoService = cartaoService;
//	}
	
//	private BigDecimal limiteCredito = new BigDecimal("600");
//	private BigDecimal limiteDiarioTransacao = new BigDecimal("600");
	

//	@PostMapping("")
//	public ResponseEntity<String> createCartao(@RequestBody CartaoCreateDTO dto, JwtAuthenticationToken token){
//		
//		// Pegar o clienteCreateDTO e transformá-lo em uma entidade
//		Cartao cartaoAdicionado = cartaoService.save(dto, token);
//		
//		if(cartaoAdicionado != null) {
//			return new ResponseEntity<String>("Cartão adicionado com sucesso", HttpStatus.CREATED);
//		} else {
//			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
//		}	
//		
//	}
	
//	@PostMapping("")
//	public ResponseEntity<String> createCartao(@RequestBody CartaoCreateDTO dto, JwtAuthenticationToken token){
//		
//		//Receber o usuário que está logado e criar a conta desse usuário.
//		Integer userId = null;
//		
//		Cartao cartao = null;
//		List<Cartao> cartoes = new ArrayList<Cartao>();
//		String numCartao = gerarNumCartao();
//		
//		try {
//			userId = Integer.parseInt(token.getName());
//			
//		} catch (NumberFormatException e) {			
//			System.out.println("ID inválido no token: " + token.getName());
//		}
//		
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
//		cartaoRepository.save(cartao);
//		
//		return new ResponseEntity<>("Cartão criado com sucesso", HttpStatus.OK);
//	}
	
//	
//	public String gerarNumCartao() {
//
//		int[] sequencia = new int[8];
//		Random random = new Random();
//		String meucartao = "";
//
//		for (int i = 0; i < sequencia.length; i++) {
//			sequencia[i] = 1 + random.nextInt(8);
//		}
//
//		for (int i = 0; i < sequencia.length; i++) {
//			meucartao += Integer.toString(sequencia[i]);
//		}
//
//		return meucartao;
//	}
//	
	
//
//	@Autowired
//	private CartaoService cartaoService;
//
//	@Autowired
//	private CartaoMapper cartaoMapper;
//
//	@Autowired
//	private CartaoUpdateMapper cartaoUpdateMapper;
//
//	@Autowired
//	private CartaoUpdateLimiteMapper cartaoUpdateLimiteMapper;
//
//	@Autowired
//	private CartaoUpdateStatusMapper cartaoUpdateStatusMapper;
//
//	@Autowired
//	private CartaoUpdateSenhaMapper cartaoUpdateSenhaMapper;
//
//	@PostMapping("") 
//	public ResponseEntity<CartaoResponseDTO> adicionarCartao(@Valid @RequestBody CartaoCreateDTO cartaoCreateDTO) {
//
//		Cartao cartaoGravado = cartaoService.save(cartaoCreateDTO);
//
//		CartaoResponseDTO cartaoResponseDTO = cartaoMapper.toDTO(cartaoGravado);
//
//		return ResponseEntity.status(HttpStatus.CREATED).body(cartaoResponseDTO);
//	}
//
//	@GetMapping("/{cartaoId}") 
//	public Optional<CartaoResponseDTO> getCartaoById(@PathVariable("cartaoId") Long cartaoId,
//			HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return Optional.empty(); // Retorna um Optional vazio em caso de acesso não autorizado
//		}
//
//		// Mapeia o cartão para um DTO
//		CartaoResponseDTO cartaoResponseDTO = cartaoMapper.toDTO(cartao);
//
//		return Optional.of(cartaoResponseDTO); // Retorna o DTO dentro de um Optional
//
//	}
//
//	@PutMapping("/{cartaoId}") 
//	public ResponseEntity<CartaoUpdateResponseDTO> atualizar(@PathVariable("cartaoId") Long cartaoId,
//			@Valid @RequestBody CartaoUpdateDTO cartaoUpdateDTO, HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//		}
//
//		Cartao cartaoAtualizado = cartaoService.update(cartaoId, cartaoUpdateDTO);
//		CartaoUpdateResponseDTO cartaoResponseDTO = cartaoUpdateMapper.toDTO(cartaoAtualizado);
//		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);
//
//	}
//
//	@DeleteMapping("/{cartaoId}") 
//	public ResponseEntity<String> deletar(@PathVariable("cartaoId") Long cartaoId,
//			@Valid @RequestBody CartaoDeleteDTO cartaoDeleteDTO, HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
//		}
//
//		boolean cartaoDeletado = cartaoService.deleteCartao(cartaoId, cartaoDeleteDTO);
//		if (cartaoDeletado) {
//			return new ResponseEntity<>("Cartão deletado com sucesso", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//
//	@GetMapping("") 
//	public ResponseEntity<List<Cartao>> getContas(HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Verifica se o cliente logado tem permissão para listar os cartões
//		if (!temPermissaoListarCartoes(clienteIdToken, auth)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//		}
//
//		List<Cartao> cartoes = cartaoService.getAll();
//		return new ResponseEntity<>(cartoes, HttpStatus.OK);
//	}
//
//	@PostMapping("/{idContaReceber}/pagamento")
//	public ResponseEntity<String> pagamentoCartao(@PathVariable("idContaReceber") Long idContaReceber,
//			@Valid @RequestBody CartaoCreateTedDTO cartaoTransCreateDTO, HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Verifica se o cliente logado tem permissão para realizar o pagamento
//		if (!temPermissaoPagamentoCartao(idContaReceber, clienteIdToken, auth)) {
//			return new ResponseEntity<>("Acesso negado", HttpStatus.FORBIDDEN);
//		}
//
//		// Realiza o pagamento do cartão
//		boolean pagamentoRealizado = cartaoService.pagCartao(idContaReceber, cartaoTransCreateDTO);
//
//		if (pagamentoRealizado) {
//			return new ResponseEntity<>("Pagamento realizado com sucesso.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//
//	@PutMapping("/{cartaoId}/limite") 
//	public ResponseEntity<CartaoUpdateLimiteResponseDTO> alterarLimiteCartaoCredito(
//			@PathVariable("cartaoId") Long cartaoId, @Valid @RequestBody CartaoUpdateLimiteDTO cartaoUpdateLimiteDTO,
//			HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//		}
//
//		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoCredito(cartaoId, cartaoUpdateLimiteDTO);
//		CartaoUpdateLimiteResponseDTO cartaoResponseDTO = cartaoUpdateLimiteMapper.toDTO(limiteAtualizado);
//		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);
//
//	}
//
//	@PutMapping("/{cartaoId}/status") 
//	public ResponseEntity<CartaoUpdateStatusResponseDTO> alterarStatusCartao(@PathVariable("cartaoId") Long cartaoId,
//			@Valid @RequestBody CartaoUpdateStatusDTO cartaoUpdateStatusDTO, HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//		}
//
//		Cartao statusAtualizado = cartaoService.alterarStatusC(cartaoId, cartaoUpdateStatusDTO);
//		CartaoUpdateStatusResponseDTO cartaoResponseDTO = cartaoUpdateStatusMapper.toDTO(statusAtualizado);
//		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);
//
//	}
//
//	@PutMapping("/{cartaoId}/senha") 
//	public ResponseEntity<CartaoUpdateSenhaResponseDTO> alterarSenhaCartao(@PathVariable("cartaoId") Long cartaoId,
//			@Valid @RequestBody CartaoUpdateSenhaDTO cartaoUpdateSenhaDTO, HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//		}
//
//		Cartao statusAtualizado = cartaoService.alterarSenhaC(cartaoId, cartaoUpdateSenhaDTO);
//		CartaoUpdateSenhaResponseDTO cartaoResponseDTO = cartaoUpdateSenhaMapper.toDTO(statusAtualizado);
//		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);
//
//	}
//
//	@GetMapping("/{cartaoId}/fatura") 
//	public ResponseEntity<Fatura> getFaturaCartaoDeCredito(@PathVariable("cartaoId") Long cartaoId,
//			HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//		}
//
//		// Obtém a fatura do cartão
//		Fatura fatura = cartaoService.getFaturaCartaoDeCreditoService(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Não existe fatura para esse cartão."));
//
//		return ResponseEntity.status(HttpStatus.OK).body(fatura);
//	}
//
//	@PostMapping("/{idCartao}/fatura/pagamento") 
//	public ResponseEntity<String> pagamentoFaturaCartaoCredito(@PathVariable("idCartao") Long idCartao,
//			@RequestBody @Valid CartaoPagarFaturaDTO cartaoPagarFaturaDTO, HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(idCartao)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return new ResponseEntity<>("Acesso negado", HttpStatus.FORBIDDEN);
//		}
//
//		// Realiza o pagamento da fatura
//		boolean pagamentoFaturaOk = cartaoService.pagFaturaCartaoC(idCartao, cartaoPagarFaturaDTO);
//
//		if (pagamentoFaturaOk) {
//			return new ResponseEntity<>("Fatura paga.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//
//	@PutMapping("/{cartaoId}/limite-diario") 
//	public ResponseEntity<CartaoUpdateLimiteResponseDTO> alterarLimiteCartaoDebito(
//			@PathVariable("cartaoId") Long cartaoId, @Valid @RequestBody CartaoUpdateLimiteDTO cartaoUpdateLimiteDTO,
//			HttpServletRequest request) {
//		Long clienteIdToken = (Long) request.getAttribute("clienteId");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		// Obtém o cartão pelo ID
//		Cartao cartao = cartaoService.getCartaoById(cartaoId)
//				.orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//
//		// Verifica se o cliente logado pode acessar este cartão
//		if (!podeAcessarCliente(cartao.getConta().getCliente().getId(), clienteIdToken, auth)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//		}
//
//		// Realiza a alteração do limite diário
//		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoDebito(cartaoId, cartaoUpdateLimiteDTO);
//
//		// Retorna a resposta com o novo limite
//		CartaoUpdateLimiteResponseDTO cartaoResponseDTO = cartaoUpdateLimiteMapper.toDTO(limiteAtualizado);
//		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);
//
//	}
////
////	// Verificação de autorização de acesso
//	private boolean podeAcessarCliente(Long clienteId, Long clienteIdToken, Authentication auth) {
//	    // Verifica se o usuário é um administrador ou se o cliente logado é o dono do cartão
//	    return auth.getAuthorities().stream()
//	        .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN") || clienteId.equals(clienteIdToken));
//	}
////
////	// Verifica se o cliente tem permissão para listar cartões
//	private boolean temPermissaoListarCartoes(Long clienteIdToken, Authentication auth) {
//	    // Verifica se o cliente logado é o dono do cartão ou se ele é um administrador
//	    return auth.getAuthorities().stream()
//	        .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN") || clienteIdToken.equals(clienteIdToken));
//	}
////
////	// Verifica se o cliente tem permissão para realizar o pagamento do cartão
//	private boolean temPermissaoPagamentoCartao(Long idContaReceber, Long clienteIdToken, Authentication auth) {
//	    // Verifica se o cliente logado é o dono da conta do cartão ou se ele é um administrador
//	    Cartao cartao = cartaoService.getCartaoById(idContaReceber)
//	        .orElseThrow(() -> new CartaoNaoEncontradoException("Cartão não encontrado."));
//	    
//	    Long clienteIdDoCartao = cartao.getConta().getCliente().getId();
//	    
//	    return auth.getAuthorities().stream()
//	        .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN") || clienteIdDoCartao.equals(clienteIdToken));
//	}
//}
