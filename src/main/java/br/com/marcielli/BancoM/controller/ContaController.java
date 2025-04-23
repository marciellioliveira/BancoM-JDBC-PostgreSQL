//package br.com.marcielli.BancoM.controller;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import br.com.marcielli.BancoM.dto.security.ContaCreateDTO;
//import br.com.marcielli.BancoM.entity.Cliente;
//import br.com.marcielli.BancoM.entity.Conta;
//import br.com.marcielli.BancoM.entity.ContaCorrente;
//import br.com.marcielli.BancoM.entity.ContaPoupanca;
//import br.com.marcielli.BancoM.entity.TaxaManutencao;
//import br.com.marcielli.BancoM.enuns.TipoConta;
//import br.com.marcielli.BancoM.repository.ContaRepositoy;
//import br.com.marcielli.BancoM.repository.UserRepository;
//import br.com.marcielli.BancoM.service.ContaService;
//import br.com.marcielli.BancoM.service.UserContaService;
//
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//
//@RestController
//@RequestMapping("/contas")
//public class ContaController {
//	
//	private final UserContaService contaService;
//	
//	
//	
////	private final ContaRepositoy contaRepository;
////	private final UserRepository userRepository;
////	
////
////	public ContaController(ContaRepositoy contaRepository, UserRepository userRepository, UserContaService contaService) {
////		this.contaRepository = contaRepository;
////		this.userRepository = userRepository;
////		this.contaService = contaService;
////	}
//	
//	public ContaController(UserContaService contaService) {
//		super();
//		this.contaService = contaService;
//	}
//
//
//	@PostMapping("")
//	public ResponseEntity<String> createConta(@RequestBody ContaCreateDTO dto, JwtAuthenticationToken token){
//		
//		// Pegar o clienteCreateDTO e transformá-lo em uma entidade
//		Conta contaAdicionada = contaService.save(dto, token);
//		
//		if(contaAdicionada != null) {
//			return new ResponseEntity<String>("Conta adicionada com sucesso", HttpStatus.CREATED);
//		} else {
//			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
//		}	
//	}
//	
//	
////	@PostMapping("")
////	public ResponseEntity<String> createConta(@RequestBody ContaCreateDTO dto, JwtAuthenticationToken token){
////		
////		//Receber o usuário que está logado e criar a conta desse usuário.
////		Integer userId = null;
////		TaxaManutencao taxa = new TaxaManutencao(dto.saldoConta(), dto.tipoConta());
////		List<TaxaManutencao> novaTaxa = new ArrayList<TaxaManutencao>();
////		novaTaxa.add(taxa);
////		
////		String numeroConta =  gerarNumeroDaConta();
////		String numeroPix = gerarPixAleatorio();
////		String novoPix = numeroPix.concat("-PIX");
////		
////		Conta conta = null;
////		
////		try {
////			userId = Integer.parseInt(token.getName());
////			
////			
////		} catch (NumberFormatException e) {			
////			System.out.println("ID inválido no token: " + token.getName());
////		}
////		
////		var user = userRepository.findById(userId);
////		
////		if (dto.tipoConta() == TipoConta.CORRENTE) {
////			
////			conta = new ContaCorrente(taxa.getTaxaManutencaoMensal());
////			conta.setTaxas(novaTaxa);
////			String numContaCorrente = numeroConta.concat("-CC");
////			conta.setNumeroConta(numContaCorrente);
////			
////		} else if (dto.tipoConta() == TipoConta.POUPANCA) {
////			
////			conta = new ContaPoupanca(taxa.getTaxaAcrescRend(), taxa.getTaxaMensal());
////			conta.setTaxas(novaTaxa);
////			String numContaPoupanca = numeroConta.concat("-PP");
////			conta.setNumeroConta(numContaPoupanca);
////		}
////		
////		conta.setPixAleatorio(novoPix);
////		conta.setCategoriaConta(taxa.getCategoria());
////		conta.setCliente(user.get().getCliente());
////		conta.setTipoConta(dto.tipoConta());
////		conta.setSaldoConta(dto.saldoConta());
////		conta.setStatus(true);
////		
////		contaRepository.save(conta);
////	
////		return new ResponseEntity<>("Conta criada com sucesso", HttpStatus.OK);
////		
////	}
//	
//	
//	
////	// Outros métodos
////	public String gerarNumeroDaConta() {
////
////		int[] sequencia = new int[8];
////		Random random = new Random();
////		String minhaConta = "";
////
////		for (int i = 0; i < sequencia.length; i++) {
////			sequencia[i] = 1 + random.nextInt(8);
////		}
////
////		for (int i = 0; i < sequencia.length; i++) {
////			minhaConta += Integer.toString(sequencia[i]);
////		}
////
////		return minhaConta;
////	}
////
////	public String gerarPixAleatorio() {
////
////		int[] sequencia = new int[8];
////		Random random = new Random();
////		String meuPix = "";
////
////		for (int i = 0; i < sequencia.length; i++) {
////			sequencia[i] = 1 + random.nextInt(8);
////		}
////
////		for (int i = 0; i < sequencia.length; i++) {
////			meuPix += Integer.toString(sequencia[i]);
////		}
////
////		return meuPix;
////	}
//
//	
//
////	@Autowired
////	private ContaService contaService;
////
////	@Autowired
////	private ContaMapper contaMapper;
////	
////	@Autowired
////	private ContaUpdatePixMapper contaUpdatePixMapper;
////	
////	private boolean isAdmin(Authentication auth) { //Verifica se o usuário autenticado é admin
////	    return auth.getAuthorities().stream()
////	        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
////	}
////	
////	private boolean podeAcessarCliente(Long clienteIdRequest, Long clienteIdToken, Authentication auth) { //verifica se o usuário pode acessar a conta com base no clienteId
////	    return isAdmin(auth) || clienteIdToken != null && clienteIdRequest.equals(clienteIdToken);
////	}
////	
////	@PostMapping("") 
////	public ResponseEntity<ContaResponseDTO> adicionarConta(@Valid @RequestBody ContaCreateDTO contaCreateDTO) {		
////
////		Conta conta = contaMapper.toEntity(contaCreateDTO);
////
////		Conta contaGravada = contaService.save(conta);
////
////		ContaResponseDTO contaResponseDTO = contaMapper.toDTO(contaGravada);
////
////		return ResponseEntity.status(HttpStatus.CREATED).body(contaResponseDTO);
////
////	}	
////	
////	@GetMapping("/{contaId}") 
////	public ResponseEntity<?> getContaById(@PathVariable("contaId") Long contaId, HttpServletRequest request) {
////		
////		Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	    Conta conta = contaService.getContaById(contaId)
////	        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada."));
////
////	    // Verifica se o usuário logado pode acessar a conta
////	    if (!podeAcessarCliente(conta.getCliente().getId(), clienteIdToken, auth)) {
////	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////	    }
////
////	    return ResponseEntity.ok(conta);
////
////	}
////	
////	@PutMapping("/{contaId}") 
////	public ResponseEntity<ContaUpdatePixResponseDTO> atualizar(@PathVariable("contaId") Long contaId, @Valid @RequestBody ContaUpdatePixDTO contaUpdatePixDTO, HttpServletRequest request) {
////
////		Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	    Conta conta = contaService.getContaById(contaId)
////	        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada."));
////
////	    // Verifica se o usuário logado pode acessar a conta
////	    if (!podeAcessarCliente(conta.getCliente().getId(), clienteIdToken, auth)) {
////	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
////	    }
////
////	    Conta contaAtualizada = contaService.update(contaId, contaUpdatePixDTO);
////	    ContaUpdatePixResponseDTO contaResponseDTO = contaUpdatePixMapper.toDTO(contaAtualizada);
////
////	    return ResponseEntity.status(HttpStatus.OK).body(contaResponseDTO);
////
////	}
////
////	@DeleteMapping("/{contaId}") 
////	public ResponseEntity<String> deletar(@PathVariable("contaId") Long contaId, HttpServletRequest request) {
////		 Long clienteIdToken = (Long) request.getAttribute("clienteId");
////		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////		    Conta conta = contaService.getContaById(contaId)
////		        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada."));
////
////		    // Verifica se o usuário logado pode acessar a conta
////		    if (!podeAcessarCliente(conta.getCliente().getId(), clienteIdToken, auth)) {
////		        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////		    }
////
////		    boolean contaDeletada = contaService.deleteConta(contaId);
////
////		    if (contaDeletada) {
////		        return new ResponseEntity<>("Conta deletada com sucesso", HttpStatus.OK);
////		    } else {
////		        return new ResponseEntity<>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
////		    }
////	}
////
////	@GetMapping("") 
////	public ResponseEntity<List<Conta>> getContas(HttpServletRequest request) {
////		 Long clienteIdToken = (Long) request.getAttribute("clienteId");
////		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////		    if (isAdmin(auth)) {
////		        List<Conta> contas = contaService.getAll();
////		        return new ResponseEntity<>(contas, HttpStatus.OK);
////		    }
////
////		    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
////	}
////
////
////	//Pagamentos
////
////	@PostMapping("/{idContaReceber}/transferencia")
////	public ResponseEntity<String> transferirTED(@PathVariable("idContaReceber") Long idContaReceber, @Valid @RequestBody ContaCreateTedDTO contaTransCreateDTO, HttpServletRequest request) {
////		
////		Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	    Conta contaReceber = contaService.getContaById(idContaReceber)
////	        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta de destino não encontrada."));
////
////	    // Verifica se o usuário logado pode acessar a conta de origem
////	    if (!podeAcessarCliente(contaReceber.getCliente().getId(), clienteIdToken, auth)) {
////	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////	    }
////		
////		boolean tedRealizada = contaService.transferirTED(idContaReceber, contaTransCreateDTO);
////		
////		if(tedRealizada) {
////			return new ResponseEntity<String>("Transferência realizada com sucesso.", HttpStatus.OK);
////		} else {
////			return new ResponseEntity<String>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
////		}
////	}	
////	
////	@GetMapping("/{clienteId}/saldo") 
////	public ResponseEntity<String> exibirSaldo(@PathVariable("clienteId") Long clienteId, HttpServletRequest request) {
////		
////		Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	    if (!podeAcessarCliente(clienteId, clienteIdToken, auth)) {
////	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////	    }
////
////	    BigDecimal saldoAtual = contaService.exibirSaldo(clienteId);
////
////	    if (saldoAtual.compareTo(BigDecimal.ZERO) >= 0) {
////	        return ResponseEntity.ok("Saldo Total: " + saldoAtual);
////	    } else {
////	        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Dados da conta são inválidos.");
////	    }
////	}
////	
////	@PostMapping("/{idContaReceber}/pix") 
////	public ResponseEntity<String> transferirPIX(@PathVariable("idContaReceber") Long idContaReceber, @Valid @RequestBody ContaCreatePixDTO contaPixCreateDTO, HttpServletRequest request) {
////		Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	    Conta contaReceber = contaService.getContaById(idContaReceber)
////	        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta de destino não encontrada."));
////
////	    // Verifica se o usuário logado pode acessar a conta de origem
////	    if (!podeAcessarCliente(contaReceber.getCliente().getId(), clienteIdToken, auth)) {
////	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////	    }
////	    
////		boolean pixRealizado = contaService.transferirPIX(idContaReceber, contaPixCreateDTO);
////		
////		if(pixRealizado) {
////			return new ResponseEntity<String>("Pix realizado com sucesso.", HttpStatus.OK);
////		} else {
////			return new ResponseEntity<String>("Dados do pix são inválidos.", HttpStatus.NOT_ACCEPTABLE);
////		}
////	}
////	
////	@PostMapping("/{idContaReceber}/deposito") 
////	public ResponseEntity<String> transferirDEPOSITO(@PathVariable("idContaReceber") Long idContaReceber, @Valid @RequestBody ContaCreateDepositoDTO contaDepositoCreateDTO, HttpServletRequest request) {
////		 Long clienteIdToken = (Long) request.getAttribute("clienteId");
////		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////		    Conta contaReceber = contaService.getContaById(idContaReceber)
////		        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta de destino não encontrada."));
////
////		    // Verifica se o usuário logado pode acessar a conta de origem
////		    if (!podeAcessarCliente(contaReceber.getCliente().getId(), clienteIdToken, auth)) {
////		        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////		    }
////		boolean depositoRealizado = contaService.transferirDEPOSITO(idContaReceber, contaDepositoCreateDTO);
////		
////		if(depositoRealizado) {
////			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);
////		} else {
////			return new ResponseEntity<String>("Dados do depósito são inválidos.", HttpStatus.NOT_ACCEPTABLE);
////		}
////	}
////	
////	@PostMapping("/{idContaReceber}/saque") 
////	public ResponseEntity<String> transferirSAQUE(@PathVariable("idContaReceber") Long idContaReceber, @Valid @RequestBody ContaCreateSaqueDTO contaSaqueCreateDTO, HttpServletRequest request) {
////		 Long clienteIdToken = (Long) request.getAttribute("clienteId");
////		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////		    Conta contaReceber = contaService.getContaById(idContaReceber)
////		        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta de destino não encontrada."));
////
////		    // Verifica se o usuário logado pode acessar a conta de origem
////		    if (!podeAcessarCliente(contaReceber.getCliente().getId(), clienteIdToken, auth)) {
////		        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////		    }
////		boolean saqueRealizado = contaService.transferirSAQUE(idContaReceber, contaSaqueCreateDTO);
////		
////		if(saqueRealizado) {
////			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);
////		} else {
////			return new ResponseEntity<String>("Dados do saque são inválidos.", HttpStatus.NOT_ACCEPTABLE);
////		}
////	}
////	
////	@PutMapping("/{idConta}/manutencao") 
////	public ResponseEntity<String> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta, @Valid @RequestBody ContaCorrenteTaxaManutencaoDTO contaCorrenteTaxaCreateDTO, HttpServletRequest request) {
////		Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	    // Obtém a conta pelo ID
////	    Conta contaCorrente = contaService.getContaById(idConta)
////	        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta corrente não encontrada."));
////
////	    // Verifica se o usuário logado pode acessar a conta de origem (somente conta corrente)
////	    if (!podeAcessarCliente(contaCorrente.getCliente().getId(), clienteIdToken, auth)) {
////	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////	    }
////		Conta manutencaoCCRealizada = contaService.manutencaoTaxaCC(idConta, contaCorrenteTaxaCreateDTO);
////		
////		if(manutencaoCCRealizada != null) {
////			return new ResponseEntity<String>("Taxas aplicadas com sucesso.", HttpStatus.OK);
////		} else {
////			return new ResponseEntity<String>("Taxas inválidas.", HttpStatus.NOT_ACCEPTABLE);
////		}
////	}
////	
////
////	@PutMapping("/{idConta}/rendimentos") 
////	public ResponseEntity<String> rendimentoTaxaContaPoupanca(@PathVariable("idConta") Long idConta, @Valid @RequestBody ContaCorrenteTaxaManutencaoDTO contaCorrenteTaxaCreateDTO, HttpServletRequest request) {
////		Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	    // Obtém a conta pelo ID
////	    Conta contaPoupanca = contaService.getContaById(idConta)
////	        .orElseThrow(() -> new ClienteNaoEncontradoException("Conta poupança não encontrada."));
////
////	    // Verifica se o usuário logado pode acessar a conta de origem (somente conta poupança)
////	    if (!podeAcessarCliente(contaPoupanca.getCliente().getId(), clienteIdToken, auth)) {
////	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
////	    }
////
////		Conta manutencaoCPRealizada = contaService.rendimentoTaxaCP(idConta, contaCorrenteTaxaCreateDTO);
////		
////		if(manutencaoCPRealizada != null) {
////			return new ResponseEntity<String>("Taxas aplicadas com sucesso.", HttpStatus.OK);
////		} else {
////			return new ResponseEntity<String>("Taxas inválidas.", HttpStatus.NOT_ACCEPTABLE);
////		}
////	    
////	}
//}
