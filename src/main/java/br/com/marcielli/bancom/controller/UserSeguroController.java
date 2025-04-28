package br.com.marcielli.bancom.controller;

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

import br.com.marcielli.bancom.dto.security.ApoliceResponseDTO;
import br.com.marcielli.bancom.dto.security.CartaoUpdateDTO;
import br.com.marcielli.bancom.dto.security.SeguroCreateDTO;
import br.com.marcielli.bancom.dto.security.SeguroUpdateDTO;
import br.com.marcielli.bancom.dto.security.UserSeguroResponseDTO;
import br.com.marcielli.bancom.entity.Seguro;
import br.com.marcielli.bancom.exception.CartaoNaoEncontradoException;
import br.com.marcielli.bancom.exception.PermissaoNegadaException;
import br.com.marcielli.bancom.service.UserSeguroService;

@RestController
public class UserSeguroController {

	private final UserSeguroService seguroService;

	public UserSeguroController(UserSeguroService seguroService) {
		this.seguroService = seguroService;
	}

	@PostMapping("/seguros")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> createSeguro(@RequestBody SeguroCreateDTO dto) { // , JwtAuthenticationToken token
		try {
			Seguro seguroAdicionado = seguroService.save(dto);

			UserSeguroResponseDTO response = new UserSeguroResponseDTO();
			response.setId(seguroAdicionado.getId());
			response.setTipo(seguroAdicionado.getTipo());
			response.setAtivo(seguroAdicionado.getAtivo());
			response.setValorMensal(seguroAdicionado.getValorMensal());
			response.setValorApolice(seguroAdicionado.getValorApolice());
			response.setIdCartao(seguroAdicionado.getCartao().getId());

			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (CartaoNaoEncontradoException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (PermissaoNegadaException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/seguros")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<Seguro>> listSeguros() {
		var seguros = seguroService.getSeguros();
		return ResponseEntity.status(HttpStatus.OK).body(seguros);
	}
//	public ResponseEntity<List<User>> listUsers() {
//		var users = userRepository.findAll();
//		return ResponseEntity.ok(users);
//	}

	@GetMapping("/seguros/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> getSegurosById(@PathVariable("id") Long id) {

		Seguro seguro = seguroService.getSegurosById(id);

		if (seguro != null) {
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
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody SeguroUpdateDTO dto) {

		Seguro seguroAtualizado = seguroService.update(id, dto);

		if (seguroAtualizado != null) {
			UserSeguroResponseDTO response = new UserSeguroResponseDTO();
			response.setId(seguroAtualizado.getId());
			response.setTipo(seguroAtualizado.getTipo());
			response.setValorMensal(seguroAtualizado.getValorMensal());
			response.setValorApolice(seguroAtualizado.getValorApolice());
			response.setAtivo(seguroAtualizado.getAtivo());
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}

	}

	@DeleteMapping("/seguros/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto) {

		boolean deletado = seguroService.delete(id, dto);

		if (deletado) {
			return ResponseEntity.ok("Seguro desativado com sucesso!");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}

	}
	
	@GetMapping("/seguros/{id}/apolice")
	public ResponseEntity<ApoliceResponseDTO> getApolice(@PathVariable Long id) {
	    return ResponseEntity.ok(seguroService.gerarApoliceEletronica(id));
	}

}
