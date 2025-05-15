package br.com.marcielli.bancom.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.bancom.dto.CartaoUpdateDTO;
import br.com.marcielli.bancom.dto.security.ApoliceResponseDTO;
import br.com.marcielli.bancom.dto.security.SeguroCreateDTO;
import br.com.marcielli.bancom.dto.security.SeguroUpdateDTO;
import br.com.marcielli.bancom.entity.Seguro;
import br.com.marcielli.bancom.service.UserSeguroService;

@RestController
public class UserSeguroController {

	private final UserSeguroService seguroService;

	public UserSeguroController(UserSeguroService seguroService) {
		this.seguroService = seguroService;
	}

	// ADMIN pode criar pra ele e pra todos
	// BASIC só pode criar pra ele
	@PostMapping("/seguros")
	public ResponseEntity<Object> createSeguro(@RequestBody SeguroCreateDTO dto, Authentication authentication) {

		Seguro seguroAdicionado = seguroService.save(dto, authentication);

		if (seguroAdicionado != null) {
			return ResponseEntity.status(HttpStatus.CREATED).body(seguroAdicionado);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Tente novamente mais tarde.");
		}

	}

	// ADMIN pode ver o dele e de todos
	// BASIC só pode ver dele
	@GetMapping("/seguros")
	public ResponseEntity<Object> listSeguros(Authentication authentication) {
	    List<Seguro> seguros = seguroService.getSeguros(authentication);

	    if (seguros != null && !seguros.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.OK).body(seguros);
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum seguro encontrado.");
	    }
	}


	// ADMIN pode ver de todos
	// BASIC só pode ver dele
	@GetMapping("/seguros/{id}")
	public ResponseEntity<Object> getSegurosById(@PathVariable("id") Long id, Authentication authentication) throws AccessDeniedException {
		
		Seguro seguro = seguroService.getSegurosById(id, authentication);

		if (seguro != null) {
			return ResponseEntity.status(HttpStatus.OK).body(seguro);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}
	}
	
	
	// ADMIN somente pra ele
	// BASIC somente pra ele
	@GetMapping("/seguros/{id}/apolice")
	public ResponseEntity<Object> getApolice(@PathVariable Long id, Authentication authentication) {
	    
	    ApoliceResponseDTO apolice = seguroService.gerarApoliceEletronica(id, authentication);
	    
	    if (apolice != null) {
	        return ResponseEntity.status(HttpStatus.OK).body(apolice);
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Seguro não encontrado!");
	    }
	}



	// ADMIN pode atualizar de todos e dele
	// BASIC só pode atualizar dele
	@PutMapping("/seguros/{id}")
	public ResponseEntity<Object> atualizar(@PathVariable("id") Long id, @RequestBody SeguroUpdateDTO dto,
	        Authentication authentication) {

	    Seguro seguroAtualizado = seguroService.update(id, dto, authentication);

	    if (seguroAtualizado != null) {
	        return ResponseEntity.ok(seguroAtualizado);
	    }
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Seguro não encontrado!");
	}


	// ADMIN pode atualizar dele e de todos
	// BASIC só pode atualizar dele
	@DeleteMapping("/seguros/{id}")
	public ResponseEntity<Object> deletar(@PathVariable("id") Long id, Authentication authentication) throws AccessDeniedException {

	    boolean deletado = seguroService.delete(id, authentication);

	    if (deletado) {
	        return ResponseEntity.ok("Seguro desativado com sucesso!");
	    }
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Seguro não encontrado!");
	}


	
}
