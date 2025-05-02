package br.com.marcielli.bancom.controller;

import java.util.List;

import br.com.marcielli.bancom.dao.UserDao;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
import br.com.marcielli.bancom.dto.security.UserCreateDTO;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.service.UserClienteService;
import br.com.marcielli.bancom.service.UserSecurityService;

@RestController
public class UserClienteController {

	private final UserDao userDao;
	private final UserClienteService clienteService;
	private final UserSecurityService userSecurityService;
	
	public UserClienteController(UserClienteService clienteService, UserDao userDao, UserSecurityService userSecurityService) {
		this.clienteService = clienteService;
		this.userDao = userDao;
		this.userSecurityService = userSecurityService;
	}

	@PostMapping("/users")
	public ResponseEntity<String> newUser(@RequestBody UserCreateDTO dto) {
        User clienteAdicionado = clienteService.save(dto);
        if (clienteAdicionado != null) {
            return new ResponseEntity<>("Cliente adicionado com sucesso", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Erro ao adicionar cliente. Verifique os dados e tente novamente.", HttpStatus.BAD_REQUEST);
        }
    }
	

	@GetMapping("/users")
	public ResponseEntity<Object> listUsers(Authentication authentication) {
		
		List<User> users = clienteService.getAllUsers(authentication);

		if (users == null || users.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Nenhum usuário encontrado.");
		}

		return ResponseEntity.ok(users);
	}

	@GetMapping("/users/{id}")	
	public ResponseEntity<Object> getUserById(@PathVariable("id") Long id, Authentication authentication) {
				
		User user = clienteService.getUserById(id, authentication);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
		}

		return ResponseEntity.ok(user);
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<Object> atualizar(@PathVariable("id") Long id, @RequestBody UserCreateDTO dto, Authentication authentication) {

		User updatedUser = clienteService.update(id, dto, authentication);

		if (updatedUser == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
		}

		return ResponseEntity.ok(updatedUser);
	}


	@DeleteMapping("/users/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable("id") Long id, Authentication authentication) {	
		
		
		boolean deleted = clienteService.deleteUser(id, authentication);
		
		if (deleted) {
			return ResponseEntity.ok("Usuário deletado com sucesso.");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
		}

	}

	
//	try {
//		boolean deleted = userDao.delete(id, authentication);
//		if (deleted) {
//			return ResponseEntity.ok("Usuário deletado com sucesso.");
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
//		}
//	} catch (Exception e) {
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar o usuário.");
//	}

}
