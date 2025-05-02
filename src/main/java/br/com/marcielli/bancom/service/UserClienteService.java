package br.com.marcielli.bancom.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import br.com.marcielli.bancom.dao.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
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
		User user = userDao.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

		return org.springframework.security.core.userdetails.User.builder().username(user.getUsername())
				.password(user.getPassword()).roles(user.getRole()).build();
	}

	public User findByUsername(String username) {
		return userDao.findByUsername(username).orElse(null);
	}

	public User save(UserCreateDTO dto) {
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
	public User getUserById(Long id, Authentication authentication) {		
		
		// Verifica se o usuário não está autenticado
	    if (authentication == null) {
	        throw new ClienteNaoEncontradoException("Acesso negado: usuário não autenticado.");
	    }
	    
	    String username  = authentication.getName(); //nome do usuário autenticado
	    User loggedUser = findByUsername(username ); // encontra o usuário no banco
	    
	    //Long id (do parametro) = id pra ver, editar ou deletar
	    //admin = admin name
	    //usernameAutenticado.getUsername() = admin ou (outro usuario basic)
	    //usernameAutenticado.getRole() = ADMIN ou BASIC
	    //usernameAutenticado.getId() = id do user
	    //usernameAutenticado.getCliente() = cliente com id, endereço...

	    System.err.println("long id parametro: "+id);
	    System.err.println("username: "+username );
	    System.err.println("loggedUser getUsername: "+loggedUser.getUsername());
	    System.err.println("loggedUser getRole: "+loggedUser.getRole());
	    System.err.println("loggedUser getId: "+loggedUser.getId());
	    System.err.println("loggedUser getCliente: "+loggedUser.getCliente());
	    
	    // Se for BASIC, só pode acessar se for o próprio ID
	    if("BASIC".equals(loggedUser.getRole())) {
	    	if (!id.equals(loggedUser.getId().longValue())) {
	            throw new ClienteNaoEncontradoException("Acesso negado: você não tem permissão para acessar este usuário.");
	        }
	    }
		
	    // Se ADMIN, pode acessar qualquer um (sem restrição aqui)
		return userDao.findById(id).orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado."));
	}

	@Transactional
	public List<User> getAllUsers(Authentication authentication) {

		// Verifica se o usuário não está autenticado
	    if (authentication == null) {
	        throw new ClienteNaoEncontradoException("Acesso negado: usuário não autenticado.");
	    }
	   
	    String username  = authentication.getName(); //nome do usuário autenticado
	    User loggedUser = findByUsername(username ); // encontra o usuário no banco

	    // Se for BASIC, não pode acessar NADA
	    if("BASIC".equals(loggedUser.getRole())) {
	            throw new ClienteNaoEncontradoException("Acesso negado: você não tem permissão para ver todos os usuários.");
	    }
		
	    // Se ADMIN, pode acessar qualquer um (sem restrição aqui)
		return userDao.findAll();
	}

	public User update(Long id, UserCreateDTO dto, Authentication authentication) {
		
		// Verifica se o usuário não está autenticado
	    if (authentication == null) {
	        throw new ClienteNaoEncontradoException("Acesso negado: usuário não autenticado.");
	    }
	    
	    String username  = authentication.getName(); //nome do usuário autenticado
	    User loggedUser = findByUsername(username ); // encontra o usuário no banco
	    
	    // Se for BASIC, só pode atualizar se for o próprio ID
	    if("BASIC".equals(loggedUser.getRole())) {
	    	if (!id.equals(loggedUser.getId().longValue())) {
	            throw new ClienteNaoEncontradoException("Acesso negado: você não tem permissão para atualizar este usuário.");
	        }
	    }
		
	    // Se ADMIN, pode acessar qualquer um (sem restrição aqui)
				
		User user = userDao.findById(id).orElseThrow(() ->
	    new ClienteNaoEncontradoException("Usuário não encontrado para atualização")
		);
		
		User userExistente = findByUsername(dto.username());
		if (userExistente != null && !userExistente.getId().equals(user.getId())) {
		    throw new ClienteNaoEncontradoException("Este nome de usuário já está em uso.");
		}

		user.setUsername(dto.username());
		if (dto.password() != null && !dto.password().isEmpty()) {
			user.setPassword(passwordEncoder.encode(dto.password()));
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
	public boolean deleteUser(Long id, Authentication authentication) {
		// Verifica se o usuário não está autenticado
	    if (authentication == null) {
	        throw new ClienteNaoEncontradoException("Acesso negado: usuário não autenticado.");
	    }
	    
	    String username  = authentication.getName(); //nome do usuário autenticado
	    User loggedUser = findByUsername(username ); // encontra o usuário no banco

	    // Se for BASIC, só pode deletar se for o próprio ID
	    if("BASIC".equals(loggedUser.getRole())) {
	    	if (!id.equals(loggedUser.getId().longValue())) {
	            throw new ClienteNaoEncontradoException("Acesso negado: você não tem permissão para acessar este usuário.");
	        }
	    }
	    
	    //Ver se ta devendo (fazer uma logica pra não deixar deletar se tiver com fatura aberta, conta ou devendo de algum jeito
	    //Como ainda não arrumei a conta, nem transferencia vou deixar sem implementar
	    
	    
	    if("ADMIN".equals(loggedUser.getRole())) {
	    	if (id.equals(loggedUser.getId().longValue())) {
	    		throw new ClienteNaoEncontradoException("Administrador não pode deletar seu próprio usuário.");
	    	}
	    }
	   
	    //Administrador pode fazer tudo, menos deletar o próprio usuário
		return userDao.delete(id);
	}

}
