package br.com.marcielli.BancoM.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.security.SeguroCreateDTO;
import br.com.marcielli.BancoM.dto.security.SeguroUpdateDTO;
import br.com.marcielli.BancoM.dto.security.UserSeguroResponseDTO;
import br.com.marcielli.BancoM.entity.Seguro;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.repository.UserRepository;
import br.com.marcielli.BancoM.service.UserSeguroService;

@RestController
public class UserSeguroController {

	private final UserSeguroService seguroService;
	private final UserRepository userRepository;

	public UserSeguroController(UserSeguroService seguroService, UserRepository userRepository) {
		this.seguroService = seguroService;
		this.userRepository = userRepository;
	}

	@PostMapping("/seguros")
	public ResponseEntity<String> createSeguro(@RequestBody SeguroCreateDTO dto, JwtAuthenticationToken token) {

		Seguro seguroAdicionado = seguroService.save(dto, token);

		if (seguroAdicionado != null) {
			return new ResponseEntity<String>("Seguro adicionado com sucesso", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("/seguros")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<User>> listUsers() {
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}

	@GetMapping("/seguros/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> getSegurosById(@PathVariable("id") Long id) {

		Seguro seguro = seguroService.getSegurosById(id);

		if (seguro == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}

		boolean isAdmin = seguro.getCartao().getConta().getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserSeguroResponseDTO response = new UserSeguroResponseDTO();
			response.setId(id);
			response.setTipo(seguro.getTipo());
			response.setValorMensal(seguro.getValorMensal());
			response.setValorApolice(seguro.getValorApolice());
			response.setAtivo(seguro.getAtivo());

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}
	}

	@PutMapping("/seguros/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody SeguroUpdateDTO dto) {

		Seguro seguro = seguroService.update(id, dto);

		if (seguro == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O seguro não existe!");
		}

		boolean isAdmin = seguro.getCartao().getConta().getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserSeguroResponseDTO response = new UserSeguroResponseDTO();

			response.setId(id);
			response.setTipo(seguro.getTipo());
			response.setValorMensal(seguro.getValorMensal());
			response.setValorApolice(seguro.getValorApolice());
			response.setAtivo(seguro.getAtivo());

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O seguro não existe!");
		}
	}

	@DeleteMapping("/seguros/{id}")
	//@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	@Transactional
	public ResponseEntity<?> deletar(@PathVariable("id") Long id) {

		boolean seguro = seguroService.delete(id);

		if (seguro) {
			return ResponseEntity.status(HttpStatus.OK).body("Seguro deletado com sucesso!");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro. Tente novamente mais tarde.");
		}
	}

}
