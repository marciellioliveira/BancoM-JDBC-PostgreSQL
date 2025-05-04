package br.com.marcielli.bancom.service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import br.com.marcielli.bancom.dao.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dto.security.UserCreateDTO;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteCpfInvalidoException;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.validation.ValidadorCPF;
import jakarta.annotation.PostConstruct;

@Service
public class UserClienteService implements UserDetailsService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final UserDao userDao; // e devolver pro Spring Security pra fazer a autenticação
	private final ClienteDao clienteDao;
	private final RoleDao roleDao;
	// private final BCryptPasswordEncoder passwordEncoder;

	public UserClienteService(UserDao userDao, RoleDao roleDao, ClienteDao clienteDao) {
		this.userDao = userDao;
		this.roleDao = roleDao;
		this.clienteDao = clienteDao;
	}

	@PostConstruct
	@Transactional
	public void initAdminUser() {
		createRoleIfNotExists("ADMIN", 1L);
		createRoleIfNotExists("BASIC", 2L);

		Cliente clienteAdmin = new Cliente();
		clienteAdmin.setClienteAtivo(true);
		clienteAdmin.setNome("Admin");

		Role roleAdmin = roleDao.findByName(Role.Values.ADMIN.name());
		if (roleAdmin == null) {
			throw new RuntimeException("Role ADMIN não encontrada");
		}

		var userAdmin = userDao.findByUsername("admin");

		userAdmin.ifPresentOrElse(user -> {
			System.err.println("Admin já existe.");
		}, () -> {
			var user = new User();
			user.setUsername("admin");
			user.setPassword(passwordEncoder.encode("minhasenhasuperhipermegapowersecreta"));
			user.setUserAtivo(true);
			user.setRole(roleAdmin.getName()); // Definir role como String ("ADMIN")
			user.setCliente(clienteAdmin);
			clienteAdmin.setUser(user);

			userDao.save(user); // UserDao já insere em user_roles
			System.err.println("Usuário admin criado com sucesso!");
		});
	}

	private void createRoleIfNotExists(String name, Long id) {
		Role existingRole = roleDao.findByName(name);
		if (existingRole == null) {
			Role role = new Role();
			role.setId(id);
			role.setName(name);
			roleDao.save(role);
		}
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    System.out.println("===== Tentando autenticar: " + username + " =====");
	    
	    User user = userDao.findByUsername(username)
	        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
	    
	    String role = user.getRole().startsWith("ROLE_") 
	            ? user.getRole() 
	            : "ROLE_" + user.getRole();
	    
	    System.out.println("Usuário encontrado: " + user.getUsername());
	    System.out.println("Role do usuário: " + user.getRole());
	    
	    return org.springframework.security.core.userdetails.User.builder()
	        .username(user.getUsername())
	        .password(user.getPassword())
	        .roles(role.replace("ROLE_", "")) // Certifique-se que retorna "ADMIN" para o admin
	        .build();
	}
	
	
//	@Component
//	public class TempPasswordGenerator implements CommandLineRunner {
//		@Override
//	    public void run(String... args) throws Exception {
//	        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//	        String senha = "minhasenhasuperhipermegapowersecreta2";
//	        String hash = encoder.encode(senha);
//	        
//	        System.out.println("\n\n=================================");
//	        System.out.println("NOVO HASH PARA COPIAR NO BANCO:");
//	        System.out.println(hash);
//	        System.out.println("=================================\n\n");
//	    }
//	}

//	@Override
//	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		User user = userDao.findByUsername(username)
//				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
//		
//		System.out.println("UserDetailsService achou usuário: " + user.getUsername() + " com senha: " + user.getPassword());
//
//		return org.springframework.security.core.userdetails.User.builder().username(user.getUsername())
//				.password(user.getPassword()).roles(user.getRole()).build();
//	}

	@Transactional
	public User findByUsername(String username) {
		return userDao.findByUsername(username).orElse(null);
	}

	@Transactional
	public User save(UserCreateDTO dto) {
		
		Optional<User> userCpf = userDao.findByCpf(dto.cpf());
		
		if(userCpf.isPresent()) {
			 throw new ClienteEncontradoException("Já existe uma conta com o CPF no sistema");
		}
		
		User user = new User();
		user.setUsername(dto.username());
		user.setPassword(passwordEncoder.encode(dto.password()));
		user.setUserAtivo(true);
		user.setRole("BASIC");

		Cliente cliente = new Cliente();
		cliente.setNome(dto.nome());
		cliente.setCpf(dto.cpf());
		cliente.setClienteAtivo(true);
		cliente.setUser(user);
		user.setCliente(cliente);

		Endereco endereco = new Endereco();
		endereco.setRua(dto.rua());
		endereco.setNumero(dto.numero());
		endereco.setBairro(dto.bairro());
		endereco.setCidade(dto.cidade());
		endereco.setEstado(dto.estado());
		endereco.setComplemento(dto.complemento());
		endereco.setCep(dto.cep());
		cliente.setEndereco(endereco);

		try {
			return userDao.save(user);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional
	public User getUserById(Long id, Authentication authentication) throws ClienteEncontradoException {    
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    String username = authentication.getName();
	    
	    // Busca o usuário logado pelo username
	    User loggedInUser = userDao.findByUsername(username)
	        .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

	    User user;
	    
	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode acessar qualquer ID        
	        user = userDao.findById(id)
	                .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado."));
	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode acessar o próprio ID
	        if (!id.equals(loggedInUser.getId().longValue())) {
	            throw new ClienteEncontradoException("Você não tem permissão para acessar esse usuário.");
	        }
	        user = userDao.findById(id)
	                .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado."));
	    } else {
	        throw new ClienteEncontradoException("Role não autorizada para esta ação.");
	    }

	    // Carrega as contas do cliente associado ao usuário
	    if (user.getCliente() != null) {
	        Cliente clienteComContas = clienteDao.findByIdWithContas(user.getCliente().getId());
	        user.setCliente(clienteComContas);
	        // Log para depuração
	        System.out.println("Contas do cliente ID " + clienteComContas.getId() + ":");
	        clienteComContas.getContas().forEach(conta -> 
	            System.out.println("Conta ID " + conta.getId() + ", clienteNome: " + conta.getClienteNome())
	        );
	    }

	    return user;
	}

	@Transactional
	public List<User> getAllUsers(Authentication authentication) throws ClienteEncontradoException {
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    String username = authentication.getName();
	    
	    List<User> users;
	    
	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin pode ver todos os usuários
	        users = userDao.findAll();
	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode ver o próprio usuário
	        User loggedInUser = userDao.findByUsername(username)
	                .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));
	        users = List.of(loggedInUser);
	    } else {
	        throw new ClienteEncontradoException("Você não tem permissão para acessar a lista de usuários.");
	    }

	    // Carrega as contas para cada cliente associado aos usuários
	    for (User user : users) {
	        if (user.getCliente() != null) {
	            Cliente clienteComContas = clienteDao.findByIdWithContas(user.getCliente().getId());
	            user.setCliente(clienteComContas);
	            // Log para depuração
	            System.out.println("Contas do cliente ID " + clienteComContas.getId() + ":");
	            clienteComContas.getContas().forEach(conta -> 
	                System.out.println("Conta ID " + conta.getId() + ", clienteNome: " + conta.getClienteNome())
	            );
	        }
	    }

	    return users;
	}

	@Transactional
	public User update(Long id, UserCreateDTO dto, Authentication authentication ) throws ClienteEncontradoException {	
		
		String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");
		
		String username = authentication.getName(); // Agora é só o username mesmo
		
		 // Busca o usuário logado pelo username
	    User loggedInUser = userDao.findByUsername(username)
	        .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));
		
		// Se for BASIC e está tentando atualizar outro usuário, bloqueia
	    if ("ROLE_BASIC".equals(role) && !id.equals(loggedInUser.getId().longValue())) {
	        throw new ClienteEncontradoException("Usuário BASIC não tem permissão para atualizar outros usuários.");
	    }
				
		User user = userDao.findById(id).orElseThrow(() ->
	    new ClienteNaoEncontradoException("Usuário não encontrado para atualização")
		);
		
		User userExistente = findByUsername(dto.username());
		if (userExistente != null && !userExistente.getId().equals(user.getId())) {
		    throw new ClienteNaoEncontradoException("Este nome de usuário já está em uso.");
		}

		

		Cliente cliente = user.getCliente();
		cliente.setNome(dto.nome());
		cliente.setCpf(dto.cpf());

		Endereco endereco = cliente.getEndereco();
		if (endereco == null) {
			endereco = new Endereco();
			cliente.setEndereco(endereco);
		}
		endereco.setRua(dto.rua());
		endereco.setNumero(dto.numero());
		endereco.setBairro(dto.bairro());
		endereco.setCidade(dto.cidade());
		endereco.setEstado(dto.estado());
		endereco.setComplemento(dto.complemento());
		endereco.setCep(dto.cep());

		try {
			return userDao.update(user);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional
	public boolean deleteUser(Long id, Authentication authentication) throws ClienteEncontradoException {
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    String username = authentication.getName(); // Agora é só o username mesmo
	    
	    // Busca o usuário logado pelo username
	    User loggedInUser = userDao.findByUsername(username)
	        .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin não pode se deletar
	        if (id.equals(loggedInUser.getId().longValue())) {
	            throw new ClienteEncontradoException("Administradores não podem deletar a si mesmos.");
	        }
	        return userDao.delete(id);
	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode deletar a si mesmo
	        if (!id.equals(loggedInUser.getId().longValue())) {
	            throw new ClienteEncontradoException("Usuário BASIC não tem permissão para deletar outros usuários.");
	        }
	        return userDao.delete(id);
	    } else {
	        throw new ClienteEncontradoException("Role não autorizada para deletar usuários.");
	    }
	}


}
