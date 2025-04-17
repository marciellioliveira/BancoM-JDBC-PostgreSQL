package br.com.marcielli.BancoM.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.SeguroCreateDTO;
import br.com.marcielli.BancoM.dto.SeguroMapper;
import br.com.marcielli.BancoM.dto.SeguroResponseDTO;
import br.com.marcielli.BancoM.entity.Seguro;
import br.com.marcielli.BancoM.service.SeguroService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/seguros")
public class SeguroController {
	
		@Autowired
	    private SeguroService seguroService;

	    @Autowired
	    private SeguroMapper seguroMapper;

	    @PostMapping
	    public ResponseEntity<SeguroResponseDTO> contratarSeguro(@RequestBody @Valid SeguroCreateDTO dto) {
	       System.err.println("teste");
	    	Seguro seguro = seguroService.contratarSeguro(dto.getIdCartao(), dto.getTipo());
	        SeguroResponseDTO response = seguroMapper.toDTO(seguro);
	        
	        if(response != null) {
	        	
	        }
	        
	        return ResponseEntity.status(HttpStatus.CREATED).body(response);
	        
	    }

	    @GetMapping("/{id}")
	    public ResponseEntity<SeguroResponseDTO> buscarSeguroPorId(@PathVariable Long id) {
	        Seguro seguro = seguroService.buscarPorId(id);
	        SeguroResponseDTO response = seguroMapper.toDTO(seguro);
	        return ResponseEntity.ok(response);
	    }

	    @GetMapping
	    public ResponseEntity<List<SeguroResponseDTO>> listarTodosSeguros() {
	        List<Seguro> seguros = seguroService.listarTodos();
	        List<SeguroResponseDTO> response = seguroMapper.toDTO(seguros);
	        return ResponseEntity.ok(response);
	    }

	    @PutMapping("/{id}/cancelar")
	    public ResponseEntity<SeguroResponseDTO> cancelarSeguro(@PathVariable Long id) {
	        Seguro seguro = seguroService.cancelarSeguro(id);
	        SeguroResponseDTO response = seguroMapper.toDTO(seguro);
	        return ResponseEntity.ok(response);
	    }
}
