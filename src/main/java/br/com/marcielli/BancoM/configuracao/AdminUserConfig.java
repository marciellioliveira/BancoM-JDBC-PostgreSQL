package br.com.marcielli.BancoM.configuracao;

import br.com.marcielli.BancoM.entity.Role;
import br.com.marcielli.BancoM.entity.User;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.marcielli.BancoM.repository.RoleRepository;
import br.com.marcielli.BancoM.repository.UserRepository;
import jakarta.transaction.Transactional;

@Configuration
public class AdminUserConfig implements CommandLineRunner {
	
	private RoleRepository roleRepository;
	
	private UserRepository userRepository;
	
	private BCryptPasswordEncoder passwordEncoder;

	public AdminUserConfig(RoleRepository roleRepository, UserRepository userRepository,
			BCryptPasswordEncoder passwordEncoder) {
		super();
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}



	@Override
	@Transactional
	public void run(String... args) throws Exception {
		
		//Como ele é um admin, preciso criar o seu usuario e vincular a Role de admin
		
		createRoleIfNotExists("ADMIN");
	    createRoleIfNotExists("BASIC");
		
		var roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());
		
		var userAdmin = userRepository.findByUsername("admin");
		
		userAdmin.ifPresentOrElse(
				user -> {
					System.err.println("Admin já existe.");
				}, 
				() -> {
					var user = new User();
					user.setUsername("admin");
					user.setPassword(passwordEncoder.encode("123"));
					user.setRoles(Set.of(roleAdmin));
					userRepository.save(user);
				}
			);
	}
	
	private void createRoleIfNotExists(String name) {
	    if (roleRepository.findByName(name) == null) {
	        roleRepository.save(new Role(null, name));
	    }
	}

}
