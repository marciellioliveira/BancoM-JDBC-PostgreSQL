package br.com.marcielli.BancoM.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.marcielli.BancoM.dto.security.CreateUserDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.entity.Role;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.RoleRepository;
import br.com.marcielli.BancoM.repository.UserRepository;
import jakarta.transaction.Transactional;

@RestController
public class UserClienteController {

	private final UserRepository userRepository;
	private final ClienteRepository clienteRepository;
	private final RoleRepository roleRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserClienteController(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder, ClienteRepository clienteRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.clienteRepository = clienteRepository;
	}
	
	@PostMapping("/users")
	@Transactional
	public ResponseEntity<Void> newUser(@RequestBody CreateUserDTO dto){
		
		var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
		var userFromDb = userRepository.findByUsername(dto.username());
		
		if(userFromDb.isPresent()) { //É uma entidade com erro de negócio da requisição da API
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
		}
		
		//Se não existe, cria um novo usuário
		var user = new User();
		user.setUsername(dto.username());
		user.setPassword(passwordEncoder.encode(dto.password()));
		user.setRoles(Set.of(basicRole));
				
		//Marcielli inseriu
		Cliente client = new Cliente();
		client.setNome(dto.nome());
		client.setCpf(dto.cpf());
	
		Endereco address = new Endereco();
		address.setCep(dto.cep());
		address.setCidade(dto.cidade());
		address.setEstado(dto.estado());
		address.setRua(dto.rua());
		address.setNumero(dto.numero());
		address.setBairro(dto.bairro());
		address.setComplemento(dto.complemento());
		
		client.setEndereco(address);
		client.setUser(user);
		user.setCliente(client);
		
		clienteRepository.save(client);
		//Fecha Marcielli Inseriu
		
		userRepository.save(user);
		
		return ResponseEntity.ok().build();
		
	}
	
	@GetMapping("/users")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<User>> listUsers() {		
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}
	
	
	
	
	
	
	
	
}
