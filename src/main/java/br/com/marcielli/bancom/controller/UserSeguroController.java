package br.com.marcielli.bancom.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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

@RestController
public class UserSeguroController {
	
//	private final UserSeguroService seguroService;
//
//	public UserSeguroController(UserSeguroService seguroService) {
//		this.seguroService = seguroService;
//	}

	//ADMIN pode criar pra ele e pra todos
	//BASIC só pode criar pra ele
	@PostMapping("/seguros")
	public ResponseEntity<?> createSeguro(@RequestBody SeguroCreateDTO dto) {
		return null; 
//		try {
//			Seguro seguroAdicionado = seguroService.save(dto);
//
//			UserSeguroResponseDTO response = new UserSeguroResponseDTO();
//			response.setId(seguroAdicionado.getId());
//			response.setTipo(seguroAdicionado.getTipo());
//			response.setAtivo(seguroAdicionado.getAtivo());
//			response.setValorMensal(seguroAdicionado.getValorMensal());
//			response.setValorApolice(seguroAdicionado.getValorApolice());
//			response.setIdCartao(seguroAdicionado.getCartao().getId());
//
//			return ResponseEntity.status(HttpStatus.CREATED).body(response);
//
//		} catch (CartaoNaoEncontradoException e) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//		} catch (PermissaoNegadaException e) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
//		} catch (IllegalArgumentException e) {
//			return ResponseEntity.badRequest().body(e.getMessage());
//		}
	}

	//ADMIN pode ver o dele e de todos
	//BASIC só pode ver dele
	@GetMapping("/seguros")
	public ResponseEntity<List<Seguro>> listSeguros() {
		return null;
//		var seguros = seguroService.getSeguros();
//		return ResponseEntity.status(HttpStatus.OK).body(seguros);
	}


	//ADMIN pode ver de todos
	//BASIC só pode ver dele
	@GetMapping("/seguros/{id}")
	public ResponseEntity<?> getSegurosById(@PathVariable("id") Long id) {
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
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody SeguroUpdateDTO dto) {
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
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto) {
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
	public ResponseEntity<ApoliceResponseDTO> getApolice(@PathVariable Long id) {
		return null;
	    //return ResponseEntity.ok(seguroService.gerarApoliceEletronica(id));
	}

}
