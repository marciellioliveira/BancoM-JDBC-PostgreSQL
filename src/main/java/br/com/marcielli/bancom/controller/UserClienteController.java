package br.com.marcielli.bancom.controller;

import java.util.List;

import br.com.marcielli.bancom.repository.UserRepositoryJDBC;
import org.springframework.context.annotation.Profile;
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
import br.com.marcielli.bancom.dto.security.UserClienteResponseDTO;
import br.com.marcielli.bancom.dto.security.UserCreateDTO;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.service.UserClienteService;

@RestController
public class UserClienteController {

	private final UserRepositoryJDBC userRepositoryJDBC;
	private final UserClienteService clienteService;

	public UserClienteController(UserClienteService clienteService, UserRepositoryJDBC userRepositoryJDBC) {
		this.clienteService = clienteService;
		this.userRepositoryJDBC = userRepositoryJDBC;
	}

	@PostMapping("/users")
	public ResponseEntity<String> newUser(@RequestBody UserCreateDTO dto, JwtAuthenticationToken token) {

		User clienteAdicionado = clienteService.save(dto, token);

		if (clienteAdicionado != null) {
			return new ResponseEntity<String>("Cliente adicionado com sucesso", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("/users")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<User>> listUsers() {
		var users = userRepositoryJDBC.findAll();
		return ResponseEntity.ok(users);
	}

	@GetMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<Object> getClienteById(@PathVariable("id") Long id) { //, JwtAuthenticationToken token

		Cliente clienteUnico = clienteService.getClienteById(id);

		System.out.println("teste "+clienteUnico);

		if (clienteUnico == null || clienteUnico.getUser() == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cliente não existe!");
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

	@PutMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<Object> atualizar(@PathVariable("id") Long id, @RequestBody UserCreateDTO dto) {
		
//		
//		if(token.getAuthorities().stream()
//				.anyMatch(auth -> auth.getAuthority().equals("SCOPE_ADMIN"))) {
//			System.err.println("Admin");
//		} else {
//			System.err.println("Basic");
//		}
//		
	
		Cliente clienteUnico = clienteService.update(id, dto);

		if (clienteUnico != null) {
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
	public ResponseEntity<Object> deletar(@PathVariable("id") Long id) {

		if (id == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cliente não existe!");
		}

		boolean clienteUnico = clienteService.delete(id);

		if (clienteUnico) {
			return ResponseEntity.status(HttpStatus.OK).body("Cliente deletado com sucesso!");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro. Tente novamente mais tarde.");
		}
	}

}
