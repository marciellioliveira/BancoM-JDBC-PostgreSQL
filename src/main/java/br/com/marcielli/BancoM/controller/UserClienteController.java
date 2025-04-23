package br.com.marcielli.BancoM.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import jakarta.transaction.Transactional;

@RestController
public class UserClienteController {

	private final UserRepository userRepository;
	private final UserClienteService clienteService;

	public UserClienteController(UserClienteService clienteService, UserRepository userRepository) {
		this.clienteService = clienteService;
		this.userRepository = userRepository;
	}

	@PostMapping("/users")
	@Transactional
	public ResponseEntity<String> newUser(@RequestBody UserCreateDTO dto) {

		User clienteAdicionado = clienteService.save(dto);

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
	@Transactional
	public ResponseEntity<?> getClienteById(@PathVariable("id") Long id) {

		Cliente clienteUnico = clienteService.getClienteById(id);

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

	@PutMapping("/users/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody UserCreateDTO dto) {

		Cliente clienteUnico = clienteService.update(id, dto);

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
	//@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	@Transactional
	public ResponseEntity<?> deletar(@PathVariable("id") Long id) {
		
		if(id == null) {
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
