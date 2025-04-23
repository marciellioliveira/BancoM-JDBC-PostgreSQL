package br.com.marcielli.BancoM.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.security.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.security.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.security.ContaUpdateDTO;
import br.com.marcielli.BancoM.dto.security.UserCartaoResponseDTO;
import br.com.marcielli.BancoM.dto.security.UserContaResponseDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.repository.UserRepository;
import br.com.marcielli.BancoM.service.UserCartaoService;

@RestController
public class UserCartaoController {
	
	private final UserCartaoService cartaoService;
	private final UserRepository userRepository;

	public UserCartaoController(UserCartaoService cartaoService, UserRepository userRepository) {
		this.cartaoService = cartaoService;
		this.userRepository = userRepository;
	}
	
	@PostMapping("/cartoes")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> createCartao(@RequestBody CartaoCreateDTO dto, JwtAuthenticationToken token){
		
		// Pegar o clienteCreateDTO e transformá-lo em uma entidade
		Cartao cartaoAdicionado = cartaoService.save(dto, token);
		
		if(cartaoAdicionado != null) {
			return new ResponseEntity<String>("Cartão adicionado com sucesso", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}	
		
	}
	
	@GetMapping("/cartoes")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<User>> listUsers() {		
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}
	
	@GetMapping("/cartoes/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> getCartoesById(@PathVariable("id") Long id) {
		
		Cartao cartao = cartaoService.getCartoesById(id);
		
		if (cartao == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
	    }
		
		boolean isAdmin = cartao.getConta().getCliente().getUser().getRoles().stream()
			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
		
		if(!isAdmin) {
			UserCartaoResponseDTO response = new UserCartaoResponseDTO();
			
			response.setId(id);
			response.setTipoConta(cartao.getConta().getTipoConta());
			response.setCategoriaConta(cartao.getCategoriaConta());
			response.setTipoCartao(cartao.getTipoCartao());
			response.setNumeroCartao(cartao.getNumeroCartao());
			response.setStatus(cartao.isStatus());
			response.setSenha(cartao.getSenha());
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {			
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}
	}
	
	
	@PutMapping("/cartoes/{id}") 
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody CartaoUpdateDTO dto) {
		
		Cartao cartao = cartaoService.update(id, dto);
		
		if (cartao == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
	    }
		
		boolean isAdmin = cartao.getConta().getCliente().getUser().getRoles().stream()
			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
		
		if(!isAdmin) {
			UserCartaoResponseDTO response = new UserCartaoResponseDTO();
			
			response.setId(id);
			response.setTipoConta(cartao.getConta().getTipoConta());
			response.setCategoriaConta(cartao.getCategoriaConta());
			response.setTipoCartao(cartao.getTipoCartao());
			response.setNumeroCartao(cartao.getNumeroCartao());
			response.setStatus(cartao.isStatus());
			response.setSenha(cartao.getSenha());
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {			
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O cartão não existe!");
		}
	}
	
	
	@DeleteMapping("/cartoes/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> deletar(@PathVariable("id") Long id) {

	    boolean conta = cartaoService.delete(id);

	    if (conta) {
	        return ResponseEntity.status(HttpStatus.OK).body("Cartão deletado com sucesso!");
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro. Tente novamente mais tarde.");
	    }
	}
	
	
	
	
	
	
	
	
	

}
