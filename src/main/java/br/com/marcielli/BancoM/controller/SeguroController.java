package br.com.marcielli.BancoM.controller;

import java.util.List;
import java.util.Optional;
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
import br.com.marcielli.BancoM.exception.SeguroNaoEncontradoException;
import br.com.marcielli.BancoM.service.SeguroService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/seguros")
public class SeguroController {
	
		@Autowired
	    private SeguroService seguroService;

	    @Autowired
	    private SeguroMapper seguroMapper;
	    
	    // Verificação de autorização de acesso
	    private boolean podeAcessarSeguro(Long seguroId, Long clienteIdToken, Authentication auth) {
	    	// Busca o seguro pelo ID. Retorna um Optional, então se não encontrar, lança a exceção.
	        Seguro seguro = seguroService.buscarPorId(seguroId)
	            .orElseThrow(() -> new SeguroNaoEncontradoException("Seguro não encontrado."));

	        // Verifica se o cliente logado é o proprietário do seguro ou se é um administrador
	        return seguro.getCartao().getConta().getCliente().getId().equals(clienteIdToken) || 
	               auth.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
	    }

	    @PostMapping
	    public ResponseEntity<SeguroResponseDTO> contratarSeguro(@RequestBody @Valid SeguroCreateDTO dto, HttpServletRequest request) {
	      
	    	Long clienteIdToken = (Long) request.getAttribute("clienteId");
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	        // Contratar o seguro
	        Seguro seguro = seguroService.contratarSeguro(dto.getIdCartao(), dto.getTipo());
	        SeguroResponseDTO response = seguroMapper.toDTO(seguro);

	        // Verifica se o cliente tem permissão para contratar o seguro
	        if (seguro == null || !podeAcessarSeguro(seguro.getId(), clienteIdToken, auth)) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	        }

	        return ResponseEntity.status(HttpStatus.CREATED).body(response);
	        
	    }

	    @GetMapping("/{id}")
	    public ResponseEntity<Optional<SeguroResponseDTO>> buscarSeguroPorId(@PathVariable Long id, HttpServletRequest request) {
	    	Long clienteIdToken = (Long) request.getAttribute("clienteId");
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	        // Buscar seguro pelo ID
	        Optional<Seguro> seguroOptional = seguroService.buscarPorId(id);

	        // Se o seguro não existir
	        if (seguroOptional.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty());
	        }

	        Seguro seguro = seguroOptional.get();

	        // Verifica se o cliente logado pode acessar este seguro
	        if (!podeAcessarSeguro(seguro.getId(), clienteIdToken, auth)) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Optional.empty());
	        }

	        // Mapeia e retorna o seguro
	        SeguroResponseDTO response = seguroMapper.toDTO(seguro);
	        return ResponseEntity.ok(Optional.of(response));
	    }

	    @GetMapping
	    public ResponseEntity<List<SeguroResponseDTO>> listarTodosSeguros(HttpServletRequest request) {
	    	Long clienteIdToken = (Long) request.getAttribute("clienteId");
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	        // Verifica se o cliente tem permissão para listar seguros
	        if (!temPermissaoListarSeguros(clienteIdToken, auth)) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	        }

	        List<Seguro> seguros = seguroService.listarTodos();
	        List<SeguroResponseDTO> response = seguroMapper.toDTO(seguros);
	        return ResponseEntity.ok(response);
	    }

	    @PutMapping("/{id}/cancelar")
	    public ResponseEntity<SeguroResponseDTO> cancelarSeguro(@PathVariable Long id, HttpServletRequest request) {
	    	Long clienteIdToken = (Long) request.getAttribute("clienteId");
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	        // Buscar seguro pelo ID
	        Optional<Seguro> seguroOptional = seguroService.buscarPorId(id);

	        // Se o seguro não existir
	        if (seguroOptional.isEmpty()) {
	            // Retorna uma resposta 404 sem um corpo
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	        }

	        Seguro seguro = seguroOptional.get();

	        // Verifica se o cliente logado pode acessar este seguro
	        if (!podeAcessarSeguro(seguro.getId(), clienteIdToken, auth)) {
	            // Retorna uma resposta 403 sem um corpo
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	        }

	        // Cancelar o seguro
	        Seguro seguroCancelado = seguroService.cancelarSeguro(id);
	        SeguroResponseDTO response = seguroMapper.toDTO(seguroCancelado);

	        // Retorna uma resposta 200 com o DTO do seguro cancelado
	        return ResponseEntity.ok(response);
	    }
	    
	 // Verifica se o cliente tem permissão para listar seguros
	    private boolean temPermissaoListarSeguros(Long clienteIdToken, Authentication auth) {
	        // Verifica se o cliente logado é o mesmo do clienteIdToken ou se é um administrador
	        return auth.getAuthorities().stream()
	            .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN") || clienteIdToken.equals(clienteIdToken));
	    }
}
