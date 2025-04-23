//package br.com.marcielli.BancoM.controller;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import br.com.marcielli.BancoM.dto.security.SeguroCreateDTO;
//import br.com.marcielli.BancoM.entity.Cartao;
//import br.com.marcielli.BancoM.entity.Cliente;
//import br.com.marcielli.BancoM.entity.Conta;
//import br.com.marcielli.BancoM.entity.Seguro;
//import br.com.marcielli.BancoM.entity.User;
//import br.com.marcielli.BancoM.enuns.CategoriaConta;
//import br.com.marcielli.BancoM.enuns.TipoSeguro;
//import br.com.marcielli.BancoM.repository.SeguroRepository;
//import br.com.marcielli.BancoM.repository.UserRepository;
//import br.com.marcielli.BancoM.service.UserSeguroService;
//
//@RestController
//@RequestMapping("/seguros")
//public class SeguroController {
//	
//	
//	
//	
////	private final SeguroRepository seguroRepository;
////	private final UserRepository userRepository;
////	
////	public SeguroController(SeguroRepository seguroRepository, UserRepository userRepository) {
////		this.seguroRepository = seguroRepository;
////		this.userRepository = userRepository;
////	}
////	
////	@PostMapping("")
////	public ResponseEntity<String> createSeguro(@RequestBody SeguroCreateDTO dto, JwtAuthenticationToken token){
////		
////		//Receber o usuário que está logado e criar a conta desse usuário.
////		Integer userId = null;
////		 BigDecimal valorMensal = BigDecimal.ZERO;
////	     BigDecimal valorApolice = BigDecimal.ZERO;
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
////		Optional<Cartao> cartaoDaConta = user
////			    .map(User::getCliente)
////			    .map(Cliente::getContas)
////			    .flatMap(contas -> contas.stream()
////			        .flatMap(conta -> conta.getCartoes().stream())
////			        .filter(cartao -> cartao.getId().equals(dto.idCartao()))
////			        .findFirst());
////		
////		Seguro seguro = new Seguro();
////		seguro.setTipo(dto.tipoSeguro());
////		seguro.setAtivo(true);
////		
////		if(cartaoDaConta.isPresent()) {
////			
////			Cartao cartaodaContaDoUser = cartaoDaConta.get();
////			
////			if(dto.tipoSeguro() == TipoSeguro.SEGURO_VIAGEM && cartaodaContaDoUser.getCategoriaConta() == CategoriaConta.PREMIUM) {
////				 valorMensal = BigDecimal.ZERO;
////			} else {
////				 valorMensal = new BigDecimal("50.00");
////			}
////			
////			if(dto.tipoSeguro() == TipoSeguro.SEGURO_FRAUDE) {
////				valorApolice = new BigDecimal("5000.00");
////			}
////			
////			seguro.setCartao(cartaodaContaDoUser);
////			
////		} else {
////			throw new RuntimeException("Cartão não está vinculado a uma conta.");
////		}
////
////        seguroRepository.save(seguro);
////		
////		return new ResponseEntity<>("Seguro criado com sucesso", HttpStatus.OK);
////	}
//	
//	
//	private final UserSeguroService seguroService;
//	
//	public SeguroController(UserSeguroService seguroService) {
//		this.seguroService = seguroService;
//	}
//	
//	@PostMapping("")
//	public ResponseEntity<String> createSeguro(@RequestBody SeguroCreateDTO dto, JwtAuthenticationToken token){
//		// Pegar o clienteCreateDTO e transformá-lo em uma entidade
//		Seguro seguroAdicionado = seguroService.save(dto, token);
//		
//		if(seguroAdicionado != null) {
//			return new ResponseEntity<String>("Seguro adicionado com sucesso", HttpStatus.CREATED);
//		} else {
//			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
//		}			
//	}
//	
//	
//	
////		@Autowired
////	    private SeguroService seguroService;
////
////	    @Autowired
////	    private SeguroMapper seguroMapper;
////	    
////	    // Verificação de autorização de acesso
////	    private boolean podeAcessarSeguro(Long seguroId, Long clienteIdToken, Authentication auth) {
////	    	// Busca o seguro pelo ID. Retorna um Optional, então se não encontrar, lança a exceção.
////	        Seguro seguro = seguroService.buscarPorId(seguroId)
////	            .orElseThrow(() -> new SeguroNaoEncontradoException("Seguro não encontrado."));
////
////	        // Verifica se o cliente logado é o proprietário do seguro ou se é um administrador
////	        return seguro.getCartao().getConta().getCliente().getId().equals(clienteIdToken) || 
////	               auth.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
////	    }
////
////	    @PostMapping
////	    public ResponseEntity<SeguroResponseDTO> contratarSeguro(@RequestBody @Valid SeguroCreateDTO dto, HttpServletRequest request) {
////	      
////	    	Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	        // Contratar o seguro
////	        Seguro seguro = seguroService.contratarSeguro(dto.getIdCartao(), dto.getTipo());
////	        SeguroResponseDTO response = seguroMapper.toDTO(seguro);
////
////	        // Verifica se o cliente tem permissão para contratar o seguro
////	        if (seguro == null || !podeAcessarSeguro(seguro.getId(), clienteIdToken, auth)) {
////	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
////	        }
////
////	        return ResponseEntity.status(HttpStatus.CREATED).body(response);
////	        
////	    }
////
////	    @GetMapping("/{id}")
////	    public ResponseEntity<Optional<SeguroResponseDTO>> buscarSeguroPorId(@PathVariable Long id, HttpServletRequest request) {
////	    	Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	        // Buscar seguro pelo ID
////	        Optional<Seguro> seguroOptional = seguroService.buscarPorId(id);
////
////	        // Se o seguro não existir
////	        if (seguroOptional.isEmpty()) {
////	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty());
////	        }
////
////	        Seguro seguro = seguroOptional.get();
////
////	        // Verifica se o cliente logado pode acessar este seguro
////	        if (!podeAcessarSeguro(seguro.getId(), clienteIdToken, auth)) {
////	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Optional.empty());
////	        }
////
////	        // Mapeia e retorna o seguro
////	        SeguroResponseDTO response = seguroMapper.toDTO(seguro);
////	        return ResponseEntity.ok(Optional.of(response));
////	    }
////
////	    @GetMapping
////	    public ResponseEntity<List<SeguroResponseDTO>> listarTodosSeguros(HttpServletRequest request) {
////	    	Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	        // Verifica se o cliente tem permissão para listar seguros
////	        if (!temPermissaoListarSeguros(clienteIdToken, auth)) {
////	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
////	        }
////
////	        List<Seguro> seguros = seguroService.listarTodos();
////	        List<SeguroResponseDTO> response = seguroMapper.toDTO(seguros);
////	        return ResponseEntity.ok(response);
////	    }
////
////	    @PutMapping("/{id}/cancelar")
////	    public ResponseEntity<SeguroResponseDTO> cancelarSeguro(@PathVariable Long id, HttpServletRequest request) {
////	    	Long clienteIdToken = (Long) request.getAttribute("clienteId");
////	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////
////	        // Buscar seguro pelo ID
////	        Optional<Seguro> seguroOptional = seguroService.buscarPorId(id);
////
////	        // Se o seguro não existir
////	        if (seguroOptional.isEmpty()) {
////	            // Retorna uma resposta 404 sem um corpo
////	            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
////	        }
////
////	        Seguro seguro = seguroOptional.get();
////
////	        // Verifica se o cliente logado pode acessar este seguro
////	        if (!podeAcessarSeguro(seguro.getId(), clienteIdToken, auth)) {
////	            // Retorna uma resposta 403 sem um corpo
////	            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
////	        }
////
////	        // Cancelar o seguro
////	        Seguro seguroCancelado = seguroService.cancelarSeguro(id);
////	        SeguroResponseDTO response = seguroMapper.toDTO(seguroCancelado);
////
////	        // Retorna uma resposta 200 com o DTO do seguro cancelado
////	        return ResponseEntity.ok(response);
////	    }
////	    
////	 // Verifica se o cliente tem permissão para listar seguros
////	    private boolean temPermissaoListarSeguros(Long clienteIdToken, Authentication auth) {
////	        // Verifica se o cliente logado é o mesmo do clienteIdToken ou se é um administrador
////	        return auth.getAuthorities().stream()
////	            .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN") || clienteIdToken.equals(clienteIdToken));
////	    }
//}
