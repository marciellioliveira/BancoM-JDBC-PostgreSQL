package br.com.marcielli.bancom.configuracao;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dao.RoleDao;
import br.com.marcielli.bancom.dao.UserDao;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;

@Component
public class AdminInitializer implements ApplicationListener<ApplicationReadyEvent> {
	
    private RoleDao roleDao;
    private UserDao userDao;
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);    

	public AdminInitializer(RoleDao roleDao, UserDao userDao, PasswordEncoder passwordEncoder) {
		this.roleDao = roleDao;
		this.userDao = userDao;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void onApplicationEvent(ApplicationReadyEvent event) {

	    createRoleIfNotExists("ADMIN", 1L);
	    createRoleIfNotExists("BASIC", 2L);

	    Optional<User> userAdminOpt = userDao.findByUsername("admin");

	    Role roleAdmin = roleDao.findByName("ADMIN");
	    if (roleAdmin == null) {
	        throw new RuntimeException("Role ADMIN não encontrada");
	    }

	    if (userAdminOpt.isPresent()) {
	        User userAdmin = userAdminOpt.get();
	        logger.warn("Usuário admin já existe.");

	        if (userAdmin.getRole() == null || !userAdmin.getRole().equals(roleAdmin.getName())) {
	            userAdmin.setRole(roleAdmin.getName());
	            logger.info("Role ADMIN atualizada para o usuário admin existente.");
	        }

	    } else {

	        User userAdmin = new User();
	        Cliente clienteAdmin = new Cliente();
	        Endereco adminEndereco = new Endereco();

	        adminEndereco.setCep("01001000");
	        adminEndereco.setCidade("São Paulo");
	        adminEndereco.setEstado("SP");
	        adminEndereco.setRua("Praça da Sé");
	        adminEndereco.setNumero("100");
	        adminEndereco.setBairro("Sé");
	        adminEndereco.setComplemento("Próximo à estação Sé do metrô");

	        clienteAdmin.setCpf(12345678909L);
	        clienteAdmin.setEndereco(adminEndereco);
	        clienteAdmin.setClienteAtivo(true);
	        clienteAdmin.setNome("Admin");

	        userAdmin.setUsername("admin");
	        userAdmin.setPassword(passwordEncoder.encode("minhasenhasuperhipermegapowersecreta11"));
	        userAdmin.setUserAtivo(true);
	        userAdmin.setRole(roleAdmin.getName());

	        clienteAdmin.setEndereco(adminEndereco);
	        userAdmin.setCliente(clienteAdmin);
	        clienteAdmin.setUser(userAdmin);

	        try {
	            userDao.save(userAdmin);
	            logger.info("Usuário admin criado com sucesso!");
	        } catch (Exception e) {
	            logger.error("Erro ao salvar admin: {}", e.getMessage(), e);
	        }
	    }
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
}
