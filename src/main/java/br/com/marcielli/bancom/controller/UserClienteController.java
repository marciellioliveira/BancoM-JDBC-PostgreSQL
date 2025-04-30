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
	public ResponseEntity<Object> listUsers() {
		List<User> users = clienteService.getAllUsers();

		if (users == null || users.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Nenhum usuário encontrado.");
		}

		return ResponseEntity.ok(users);
	}

	@GetMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<Object> getUserById(@PathVariable("id") Long id) {
		User user = clienteService.getUserById(id);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
		}

		return ResponseEntity.ok(user);
	}

	@PutMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<Object> atualizar(@PathVariable("id") Long id, @RequestBody UserCreateDTO dto) {

		User updatedUser = clienteService.update(id, dto);

		if (updatedUser == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
		}

		return ResponseEntity.ok(updatedUser);
	}


	//Primeiro estou fazendo com delete normal. Ao finalizar o projeto
	//Vou alterar para ao pedir para deletar, apenas deixar o usuario como false
	//somente depois de um ano deletar definitivamente com o cron no automatico
	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
		try {
			boolean deleted = userRepositoryJDBC.delete(id);
			if (deleted) {
				return ResponseEntity.ok("Usuário deletado com sucesso.");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar o usuário.");
		}
	}


}
