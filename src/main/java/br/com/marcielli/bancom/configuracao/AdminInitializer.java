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

	    Optional<User> userAdminOpt = userDao.findByUsername("admin");
	    //usuário existe?
	    
	    if(userAdminOpt.isEmpty()) { //criar um usuário
	    	System.err.println("Criando um usuário admin!");
	    	
	    	User userAdmin = new User();
	    	System.err.println("Criando user admin vazio: "+userAdmin);
	        Cliente clienteAdmin = new Cliente();
	        System.err.println("Criando cliente admin vazio: "+clienteAdmin);
	        Endereco adminEndereco = new Endereco();
	        System.err.println("Criando endereço admin vazio: "+adminEndereco);

	        adminEndereco.setCep("01001000");
	        adminEndereco.setCidade("São Paulo");
	        adminEndereco.setEstado("SP");
	        adminEndereco.setRua("Praça da Sé");
	        adminEndereco.setNumero("100");
	        adminEndereco.setBairro("Sé");
	        adminEndereco.setComplemento("Próximo à estação Sé do metrô");
	        
	        System.err.println("Preenchendo endereço: "+adminEndereco);

	        clienteAdmin.setCpf(12345678909L);
	        clienteAdmin.setEndereco(adminEndereco);
	        clienteAdmin.setClienteAtivo(true);
	        clienteAdmin.setNome("Admin");
	        
	        System.err.println("Preenchendo cliente: "+clienteAdmin);

	        userAdmin.setUsername("admin");
	        userAdmin.setPassword(passwordEncoder.encode("minhasenhasuperhipermegapowersecreta11"));
	        userAdmin.setUserAtivo(true);
	        userAdmin.setRole("ADMIN");
	        
	        System.err.println("Preenchendo user: "+userAdmin);

	        clienteAdmin.setEndereco(adminEndereco);
	        userAdmin.setCliente(clienteAdmin);
	        clienteAdmin.setUser(userAdmin);
	        
	        System.err.println("User admin completo: "+userAdmin);
	        System.err.println("Cliente admin completo: "+clienteAdmin);
	        System.err.println("Endereço admin completo: "+adminEndereco);
	        
	        try {
	        	System.err.println("\n\nUser admin completo: "+userAdmin);
	        	System.err.println("\n\nUser cliente admin completo: "+userAdmin.getCliente());
	        	System.err.println("\n\nUser cliente endereço admin completo: "+userAdmin.getCliente().getEndereco());
	        	
	            userDao.save(userAdmin);
	            
	            logger.info("Usuário admin criado com sucesso!");
	        } catch (Exception e) {
	            logger.error("Erro ao salvar admin: {}", e.getMessage(), e);
	        }
	    	
	    	
	    } else {
	    	System.err.println("Não precisa fazer nada se o admin já existe no banco");
	    }
	    
	}
}
