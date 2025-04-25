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
import br.com.marcielli.BancoM.dto.security.UserClienteResponseDTO;
import br.com.marcielli.BancoM.dto.security.UserCreateDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.repository.UserRepository;
import br.com.marcielli.BancoM.service.UserClienteService;

@RestController
public class UserClienteController {

	private final UserRepository userRepository;
	private final UserClienteService clienteService;

	public UserClienteController(UserClienteService clienteService, UserRepository userRepository) {
		this.clienteService = clienteService;
		this.userRepository = userRepository;
	}

	@PostMapping("/users")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> newUser(@RequestBody UserCreateDTO dto, JwtAuthenticationToken token) {
		
		  // Log para verificar o token ANTES da transação
	    System.out.println("Token ANTES da transação - Expira em: " + token.getToken().getExpiresAt());

		// É admin?
		boolean isAdmin = false;
		if (token != null) {
			isAdmin = token.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("SCOPE_ADMIN"));
		}

		User clienteAdicionado = clienteService.save(dto, isAdmin, token);
		
		// Log para verificar o token DEPOIS da transação
	    System.out.println("Token DEPOIS da transação - Expira em: " + token.getToken().getExpiresAt());

		if (clienteAdicionado != null) {
			return new ResponseEntity<String>("Cliente adicionado com sucesso", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("/users")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<User>> listUsers() {
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}
	
	@GetMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> getClienteById(@PathVariable("id") Long id, JwtAuthenticationToken token) {
	    
	    boolean isCurrentUserAdmin = token.getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals("SCOPE_ADMIN"));
	    
	    Cliente clienteUnico = clienteService.getClienteById(id);
	    
	    if (clienteUnico == null || clienteUnico.getUser() == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cliente não existe!");
	    }
	    
	    boolean isTargetAdmin = clienteUnico.getUser().getRoles().stream()
	            .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
	    
	    if (isTargetAdmin && !isCurrentUserAdmin) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body("Você não pode visualizar a conta do administrador");
	    }
	    
	    UserClienteResponseDTO response = new UserClienteResponseDTO();
	    response.setId(id);
	    response.setNome(clienteUnico.getNome());
	    response.setCpf(clienteUnico.getCpf());
	    response.setClienteAtivo(clienteUnico.isClienteAtivo()); 
	    
	   
	    
	    Endereco endereco = clienteUnico.getEndereco();
	    if (endereco != null) {
	        response.setCep(endereco.getCep());
	        response.setCidade(endereco.getCidade());
	        response.setEstado(endereco.getEstado());
	        response.setRua(endereco.getRua());
	        response.setNumero(endereco.getNumero());
	        response.setBairro(endereco.getBairro());
	        response.setComplemento(endereco.getComplemento());
	        response.setClienteAtivo(clienteUnico.isClienteAtivo());
	    }
	    
	    return ResponseEntity.ok(response);
	}

//	@GetMapping("/users/{id}")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	public ResponseEntity<?> getClienteById(@PathVariable("id") Long id) {
//
//		Cliente clienteUnico = clienteService.getClienteById(id);
//
//		if (clienteUnico == null || clienteUnico.getUser() == null) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cliente não existe!");
//		}
//
//		boolean isAdmin = clienteUnico.getUser().getRoles().stream()
//				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
//
//		if (!isAdmin) {
//			UserClienteResponseDTO response = new UserClienteResponseDTO();
//			response.setId(id);
//			response.setNome(clienteUnico.getNome());
//			response.setCpf(clienteUnico.getCpf());
//
//			Endereco endereco = clienteUnico.getEndereco();
//			if (endereco != null) {
//				response.setCep(endereco.getCep());
//				response.setCidade(endereco.getCidade());
//				response.setEstado(endereco.getEstado());
//				response.setRua(endereco.getRua());
//				response.setNumero(endereco.getNumero());
//				response.setBairro(endereco.getBairro());
//				response.setComplemento(endereco.getComplemento());
//				response.setClienteAtivo(clienteUnico.isClienteAtivo());
//			}
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cliente não existe!");
//		}
//	}

	@PutMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody UserCreateDTO dto,
			JwtAuthenticationToken token) {

		Cliente clienteUnico = clienteService.update(id, dto, token);

		if (clienteUnico == null || clienteUnico.getUser() == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cliente não existe!");
		}
		boolean isAdmin = clienteUnico.getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserClienteResponseDTO response = new UserClienteResponseDTO();
			response.setId(id);
			response.setNome(clienteUnico.getNome());
			response.setCpf(clienteUnico.getCpf());
			response.setClienteAtivo(clienteUnico.isClienteAtivo()); 

			Endereco endereco = clienteUnico.getEndereco();
			if (endereco != null) {
				response.setCep(endereco.getCep());
				response.setCidade(endereco.getCidade());
				response.setEstado(endereco.getEstado());
				response.setRua(endereco.getRua());
				response.setNumero(endereco.getNumero());
				response.setBairro(endereco.getBairro());
				response.setComplemento(endereco.getComplemento());

			}

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cliente não existe!");
		}

	}

	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, JwtAuthenticationToken token) {

		if (id == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cliente não existe!");
		}

		boolean clienteUnico = clienteService.delete(id, token);

		if (clienteUnico) {
			return ResponseEntity.status(HttpStatus.OK).body("Cliente deletado com sucesso!");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro. Tente novamente mais tarde.");
		}
	}

}
