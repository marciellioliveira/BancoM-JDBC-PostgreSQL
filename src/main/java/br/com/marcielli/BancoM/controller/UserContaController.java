package br.com.marcielli.BancoM.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.security.ContaCreateDTO;
import br.com.marcielli.BancoM.dto.security.ContaUpdateDTO;
import br.com.marcielli.BancoM.dto.security.UserClienteResponseDTO;
import br.com.marcielli.BancoM.dto.security.UserContaResponseDTO;
import br.com.marcielli.BancoM.dto.security.UserCreateDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.repository.UserRepository;
import br.com.marcielli.BancoM.service.UserContaService;
import jakarta.transaction.Transactional;

@RestController
public class UserContaController {

	private final UserContaService contaService;
	private final UserRepository userRepository;

	public UserContaController(UserContaService contaService, UserRepository userRepository) {
		this.contaService = contaService;
		this.userRepository = userRepository;
	}
	
	@PostMapping("/contas")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> createConta(@RequestBody ContaCreateDTO dto, JwtAuthenticationToken token){
		
		// Pegar o clienteCreateDTO e transformá-lo em uma entidade
		Conta contaAdicionada = contaService.save(dto, token);
		
		if(contaAdicionada != null) {
			return new ResponseEntity<String>("Conta adicionada com sucesso", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}	
	}
	
	@GetMapping("/contas")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<User>> listUsers() {		
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}
	
	@GetMapping("/contas/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> getContasById(@PathVariable("id") Long id) {
		
		Conta conta = contaService.getContasById(id);
		
		if (conta == null || conta.getCliente() == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
	    }
		
		boolean isAdmin = conta.getCliente().getUser().getRoles().stream()
			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
		
		if(!isAdmin) {
			UserContaResponseDTO response = new UserContaResponseDTO();
			
			response.setTipoConta(conta.getTipoConta());
			response.setCategoriaConta(conta.getCategoriaConta());
			
			if(conta instanceof ContaCorrente contaCorrente) {
				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
			}
			
			if(conta instanceof ContaPoupanca contaPoupanca) {
				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
				response.setTaxaMensal(contaPoupanca.getTaxaMensal());
				
			}
			response.setSaldoConta(conta.getSaldoConta());
			response.setNumeroConta(conta.getNumeroConta());
			response.setPixAleatorio(conta.getPixAleatorio());
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {			
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}
	}
	
	
	@PutMapping("/contas/{id}") 
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody ContaUpdateDTO dto) {
		
		Conta conta = contaService.update(id, dto);
		
		if (conta == null || conta.getCliente().getUser() == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
	    }
		
		boolean isAdmin = conta.getCliente().getUser().getRoles().stream()
			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
		
		if(!isAdmin) {
			UserContaResponseDTO response = new UserContaResponseDTO();
			
			if(conta instanceof ContaCorrente contaCorrente) {
				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
			}
			
			if(conta instanceof ContaPoupanca contaPoupanca) {
				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
				response.setTaxaMensal(contaPoupanca.getTaxaMensal());
				
			}
			
			response.setTipoConta(conta.getTipoConta());
			response.setCategoriaConta(conta.getCategoriaConta());
			response.setSaldoConta(conta.getSaldoConta());
			response.setNumeroConta(conta.getNumeroConta());
			response.setPixAleatorio(conta.getPixAleatorio());
			
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {			
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
