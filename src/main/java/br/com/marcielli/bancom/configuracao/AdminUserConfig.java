package br.com.marcielli.bancom.configuracao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;

import java.util.Set;

import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.transaction.annotation.Transactional;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

	private RoleRepositoryJDBC roleRepositoryJDBC;

	private UserRepositoryJDBC userRepositoryJDBC;

	private BCryptPasswordEncoder passwordEncoder;

	private final ClienteRepositoryJDBC clienteRepositoryJDBC;

	public AdminUserConfig(RoleRepositoryJDBC roleRepositoryJDBC, UserRepositoryJDBC userRepositoryJDBC,
			BCryptPasswordEncoder passwordEncoder, ClienteRepositoryJDBC clienteRepositoryJDBC) {
		super();
		this.roleRepositoryJDBC = roleRepositoryJDBC;
		this.userRepositoryJDBC = userRepositoryJDBC;
		this.passwordEncoder = passwordEncoder;
		this.clienteRepositoryJDBC = clienteRepositoryJDBC;
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {

		// Como ele é um admin, preciso criar o seu usuario e vincular a Role de admin		

		createRoleIfNotExists("ADMIN");
		createRoleIfNotExists("BASIC");

		var userAdmin = userRepositoryJDBC.findByUsername("admin");

		if (userAdmin.isPresent()) {
			//Como esse é o admin geral, ele só pode ser criado uma vez
			//Então sempre vai ser login admin e senha 123
			System.out.println("Usuário admin já existe.");
			return;
		}

		// Verifica se o cliente admin já existe
		var clienteExiste = clienteRepositoryJDBC.findByCpf(12345678901L); // Verifica pelo CPF, que é único

		if (clienteExiste.isPresent()) {
			// Se o cliente admin já existe, não cria novamente
			System.out.println("Cliente admin já existe.");
			return;
		}

		Endereco address = new Endereco();
		address.setCep("12345-678"); // CEP fictício
		address.setCidade("São Paulo");
		address.setEstado("SP");
		address.setRua("Rua dos Devs");
		address.setNumero("42");
		address.setBairro("Jardim Spring Boot");
		address.setComplemento("Apto 101");

		Cliente clienteAdmin = new Cliente();
		clienteAdmin.setClienteAtivo(true);
		clienteAdmin.setNome("Admin");
		clienteAdmin.setCpf(12345678901L); // CPF válido
		clienteAdmin.setEndereco(address);

		var roleAdmin = roleRepositoryJDBC.findByName(Role.Values.ADMIN.name());
		//var userAdmin = userRepositoryJDBC.findByUsername("admin");

		userAdmin.ifPresentOrElse(user -> {
			throw  new ClienteEncontradoException("Cliente já existe.");
		}, () -> {


			var user = new User();
			user.setUsername("admin");
			user.setPassword(passwordEncoder.encode("123"));
			user.setUserAtivo(true);
			user.setRoles(Set.of(roleAdmin));
			clienteAdmin.setUser(user);
			user.setCliente(clienteAdmin);


			//clienteRepositoryJDBC.save(clienteAdmin);
			userRepositoryJDBC.save(user);


			System.out.println("Cliente ID antes de salvar: " + clienteAdmin.getId());

		});
	}

	private void createRoleIfNotExists(String name) {
		if (roleRepositoryJDBC.findByName(name) == null) {
			roleRepositoryJDBC.save(new Role(null, name));
		}
	}

}
