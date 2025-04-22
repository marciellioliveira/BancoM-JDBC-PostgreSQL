//package br.com.marcielli.BancoM.controller;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import br.com.marcielli.BancoM.dto.AuthenticationRequestDTO;
//import br.com.marcielli.BancoM.dto.ClienteCreateDTO;
//import br.com.marcielli.BancoM.dto.ClienteListarDTO;
//import br.com.marcielli.BancoM.dto.ClienteMapper;
//import br.com.marcielli.BancoM.dto.UserRegisterDTO;
//import br.com.marcielli.BancoM.entity.AuthenticationResponse;
//import br.com.marcielli.BancoM.entity.Cliente;
//import br.com.marcielli.BancoM.entity.User;
//import br.com.marcielli.BancoM.enuns.Role;
//import br.com.marcielli.BancoM.repository.UserRepository;
//import br.com.marcielli.BancoM.service.AuthenticationService;
//import br.com.marcielli.BancoM.service.ClienteService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@RestController
//public class AuthenticationController {
//
//	private final AuthenticationService authService;
//	private final ClienteService clienteService;
//	
//	@Autowired
//	private ClienteMapper clienteMapper;
//	
//	@Autowired UserRepository userRepository;
//
//	public AuthenticationController(AuthenticationService authService, ClienteService clienteService) {
//		this.authService = authService;
//		this.clienteService = clienteService;
//	}
//
//	@PostMapping("/register")
//	public ResponseEntity<AuthenticationResponse> register(@RequestBody UserRegisterDTO request) {
//		
//        ClienteCreateDTO clienteCreateDTO = new ClienteCreateDTO();
//        clienteCreateDTO.setNome(request.getNome());
//        clienteCreateDTO.setCpf(request.getCpf());
//        clienteCreateDTO.setCep(request.getCep());
//        clienteCreateDTO.setCidade(request.getCidade());
//        clienteCreateDTO.setEstado(request.getEstado());
//        clienteCreateDTO.setRua(request.getRua());
//        clienteCreateDTO.setNumero(request.getNumero());
//        clienteCreateDTO.setBairro(request.getBairro());
//        clienteCreateDTO.setComplemento(request.getComplemento());
//      
//		// USER ou novo ADMIN: cria usuário + cliente
//		Cliente cliente = clienteMapper.toEntity(clienteCreateDTO);
//		cliente = clienteService.save(cliente);
//		User user = new User();
//		user.setFirstName(request.getFirstName());
//	    user.setLastName(request.getLastName());
//	    user.setUsername(request.getUsername());
//	    user.setPassword(request.getPassword());
//	    user.setRole(request.getRole());
//	    user.setCliente(cliente);
//	    
//	    AuthenticationResponse response = authService.register(user);
//	    return ResponseEntity.ok(response);
//		
//	}
//	
//	@PostMapping("/login")
//	public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequestDTO request) {
//		
//		System.err.println("cntroller "+request.getUsername() + " - " + request.getPassword());
//		return ResponseEntity.ok(authService.authenticate(request));
//	}
//	
//
//	@PostMapping("/refresh_token")
//	public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {
//		return authService.refreshToken(request, response);
//	}
//	
//	
//	//Front end
//	@PostMapping("/register-form")
//	public String registerFromForm(UserRegisterDTO request) {
//	    User user = new User();
//	    user.setFirstName(request.getFirstName());
//	    user.setLastName(request.getLastName());
//	    user.setUsername(request.getUsername());
//	    user.setPassword(request.getPassword());
//	    user.setRole(request.getRole());
//
//	    if (Role.USER.equals(request.getRole())) {
//	        ClienteCreateDTO cliente = new ClienteCreateDTO();
//	        // (aqui você pode preencher cliente com valores fixos ou um novo form depois)
//	        user.setCliente(clienteService.save(clienteMapper.toEntity(cliente)));
//	    }
//
//	    authService.register(user);
//	    return "redirect:/cadastro?sucesso";
//	}
//
//}