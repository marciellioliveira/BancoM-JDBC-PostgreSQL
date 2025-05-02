package br.com.marcielli.bancom.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import br.com.marcielli.bancom.dao.*;

import org.springframework.beans.factory.annotation.Autowired;
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
	
//	
//	@Autowired
//    private JdbcUserDetailsManager jdbcUserDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;


	private final UserDao userDao; // e devolver pro Spring Security pra fazer a autenticação
	private final ClienteDao clienteDao;
	private final RoleDao roleDao;
	//private final BCryptPasswordEncoder passwordEncoder;

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

		return org.springframework.security.core.userdetails.User.builder()
	            .username(user.getUsername())
	            .password(user.getPassword())
	            .roles(user.getRole()) // Role é String
	            .build();
	    
    }
	
	@Transactional
	public User save(UserCreateDTO cliente,  JwtAuthenticationToken token) {

		if (cliente.cpf() != null) {
			String cpfClient = Long.toString(cliente.cpf());
			if (!ValidadorCPF.validar(cpfClient)) {
				throw new ClienteCpfInvalidoException("CPF inválido");
			}
		}

		if(clienteDao.cpfExists(cliente.cpf())){
			throw new ClienteCpfInvalidoException("CPF número "+cliente.cpf()+" já é cadastrado no sistema.");
		}

		var userFromDb = userDao.findByUsername(cliente.username());
		if (userFromDb.isPresent()) {
			throw new ClienteEncontradoException("Usuário já existe.");
		}

		var user = new User();
		user.setUsername(cliente.username());
		user.setPassword(passwordEncoder.encode(cliente.password()));
		//user.setPassword(cliente.password());
		var basicRole = roleDao.findByName(Role.Values.BASIC.name());
		//user.setRoles(Set.of(basicRole));
		user.setRole(basicRole.getName());

		Cliente client = new Cliente();
		client.setNome(cliente.nome());
		client.setCpf(cliente.cpf());

		Endereco address = new Endereco();
		address.setCep(cliente.cep());
		address.setCidade(cliente.cidade());
		address.setEstado(cliente.estado());
		address.setRua(cliente.rua());
		address.setNumero(cliente.numero());
		address.setBairro(cliente.bairro());
		address.setComplemento(cliente.complemento());

		client.setEndereco(address);
		client.setUser(user);
		user.setCliente(client);

		return userDao.save(user);
	}

	@Transactional
	public User getUserById(Long id) {
		return userDao.findById(id)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado."));
	}


	@Transactional
	public List<User> getAllUsers(){
		return userDao.findAll();
	}


	@Transactional
	public User update(Long id, UserCreateDTO dto) {
		// Verifica se o usuário existe
		User user = userDao.findById(id)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado"));

		// Verifica se o CPF já está em uso
		Optional<User> existingUserWithCpf = userDao.findByCpf(dto.cpf());
		if (existingUserWithCpf.isPresent() && !existingUserWithCpf.get().getId().equals(user.getId())) {
			throw new ClienteEncontradoException("CPF é único e não pode ser atualizado. " + dto.cpf());
		}

		Optional<User> existingUserWithUsername = userDao.findByUsername(dto.username());
		if(existingUserWithUsername.isPresent() && !existingUserWithUsername.get().equals(user.getId())){
			throw new ClienteEncontradoException("Já existe um cliente com esse username. " + dto.username());
		}

		// Atualiza os dados do cliente, endereço e usuário
		Cliente cliente = user.getCliente();
		cliente.setNome(dto.nome());

		Endereco endereco = cliente.getEndereco();
		if (endereco != null) {
			endereco.setCep(dto.cep());
			endereco.setCidade(dto.cidade());
			endereco.setEstado(dto.estado());
			endereco.setRua(dto.rua());
			endereco.setNumero(dto.numero());
			endereco.setBairro(dto.bairro());
			endereco.setComplemento(dto.complemento());
		}

		user.setUsername(dto.username());
		user.setPassword(passwordEncoder.encode(dto.password())); // Criptografa a senha
		//user.setPassword(dto.password()); // Criptografa a senha
		// Chama o metodo de atualização no repositório
		userDao.update(user); // Atualiza o usuário no banco

		return user;
	}

	@Transactional
	public boolean deleteUser(Long id) {
		return userDao.delete(id);
	}

}
