package br.com.marcielli.bancom.controller;

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

	//ADMIN pode criar pra ele e pra todos
	//BASIC só pode criar pra ele
	@PostMapping("/seguros")
	public ResponseEntity<Object> createSeguro(@RequestBody SeguroCreateDTO dto, Authentication authentication) {
		
		Seguro seguroAdicionado = seguroService.save(dto, authentication);

	    if (seguroAdicionado != null) {
	        return ResponseEntity.status(HttpStatus.CREATED).body(seguroAdicionado);
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Tente novamente mais tarde.");
	    }
		
	}

	//ADMIN pode ver o dele e de todos
	//BASIC só pode ver dele
	@GetMapping("/seguros")
	public ResponseEntity<List<Seguro>> listSeguros(Authentication authentication) {
		return null;
//		var seguros = seguroService.getSeguros();
//		return ResponseEntity.status(HttpStatus.OK).body(seguros);
	}


	//ADMIN pode ver de todos
	//BASIC só pode ver dele
	@GetMapping("/seguros/{id}")
	public ResponseEntity<?> getSegurosById(@PathVariable("id") Long id, Authentication authentication) {
		return null;

//		Seguro seguro = seguroService.getSegurosById(id);
//
//		if (seguro != null) {
//			UserSeguroResponseDTO response = new UserSeguroResponseDTO();
//			response.setId(id);
//			response.setTipo(seguro.getTipo());
//			response.setValorMensal(seguro.getValorMensal());
//			response.setValorApolice(seguro.getValorApolice());
//			response.setAtivo(seguro.getAtivo());
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
//		}
	}

	//ADMIN pode atualizar de todos e dele
	//BASIC só pode atualizar dele
	@PutMapping("/seguros/{id}")
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody SeguroUpdateDTO dto, Authentication authentication) {
		return null;

//		Seguro seguroAtualizado = seguroService.update(id, dto);
//
//		if (seguroAtualizado != null) {
//			UserSeguroResponseDTO response = new UserSeguroResponseDTO();
//			response.setId(seguroAtualizado.getId());
//			response.setTipo(seguroAtualizado.getTipo());
//			response.setValorMensal(seguroAtualizado.getValorMensal());
//			response.setValorApolice(seguroAtualizado.getValorApolice());
//			response.setAtivo(seguroAtualizado.getAtivo());
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
//		}

	}

	//ADMIN pode atualizar dele e de todos
	//BASIC só pode atualizar dele
	@DeleteMapping("/seguros/{id}")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto, Authentication authentication) {
		return null;

//		boolean deletado = seguroService.delete(id, dto);
//
//		if (deletado) {
//			return ResponseEntity.ok("Seguro desativado com sucesso!");
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
//		}

	}
	
	//ADMIN  somente pra ele
	//BASIC somente pra ele
	@GetMapping("/seguros/{id}/apolice")
	public ResponseEntity<ApoliceResponseDTO> getApolice(@PathVariable Long id, Authentication authentication) {
		return null;
	    //return ResponseEntity.ok(seguroService.gerarApoliceEletronica(id));
	}

}
