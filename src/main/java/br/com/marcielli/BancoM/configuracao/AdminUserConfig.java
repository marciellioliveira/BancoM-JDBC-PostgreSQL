package br.com.marcielli.BancoM.configuracao;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Role;
import br.com.marcielli.BancoM.entity.User;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.RoleRepository;
import br.com.marcielli.BancoM.repository.UserRepository;
import jakarta.transaction.Transactional;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

	private RoleRepository roleRepository;

	private UserRepository userRepository;

	private BCryptPasswordEncoder passwordEncoder;

	private final ClienteRepository clienteRepository;

	public AdminUserConfig(RoleRepository roleRepository, UserRepository userRepository,
			BCryptPasswordEncoder passwordEncoder, ClienteRepository clienteRepository) {
		super();
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.clienteRepository = clienteRepository;
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {

		// Como ele é um admin, preciso criar o seu usuario e vincular a Role de admin
		

		createRoleIfNotExists("ADMIN");
		createRoleIfNotExists("BASIC");

		Cliente clienteAdmin = new Cliente();
		clienteAdmin.setClienteAtivo(true);
		clienteAdmin.setNome("Admin");
		

		var roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());

		var userAdmin = userRepository.findByUsername("admin");

		userAdmin.ifPresentOrElse(user -> {
			System.err.println("Admin já existe.");
		}, () -> {
			var user = new User();
			user.setUsername("admin");
			user.setPassword(passwordEncoder.encode("123"));
			user.setUserAtivo(true);
			user.setRoles(Set.of(roleAdmin));
			clienteAdmin.setUser(user);
			user.setCliente(clienteAdmin);
			clienteRepository.save(clienteAdmin);
			userRepository.save(user);
		});
	}

	private void createRoleIfNotExists(String name) {
		if (roleRepository.findByName(name) == null) {
			roleRepository.save(new Role(null, name));
		}
	}

}
