package br.com.marcielli.bancom.configuracao;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dao.UserDao;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.User;

@Component
public class AdminInitializer implements ApplicationListener<ApplicationReadyEvent> {	
   
    private UserDao userDao;
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);    

	public AdminInitializer( UserDao userDao, PasswordEncoder passwordEncoder) {		
		this.userDao = userDao;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void onApplicationEvent(ApplicationReadyEvent event) {

	    Optional<User> userAdminOpt = userDao.findByUsername("admin");
	    //usuário existe?
	    
	    if(userAdminOpt.isEmpty()) { //criar um usuário
	    	
	    	logger.error("Criando um usuário admin!");
	    	
	    	User userAdmin = new User();
	    	
	    	logger.error("Criando user admin vazio: '{}'.", userAdmin);
	    	
	        Cliente clienteAdmin = new Cliente();
	        
	        logger.error("Criando cliente admin vazio: '{}'.", clienteAdmin);
	        
	        Endereco adminEndereco = new Endereco();
	        
	        System.err.println("Criando endereço admin vazio: "+adminEndereco);

	        adminEndereco.setCep("01001000");
	        adminEndereco.setCidade("São Paulo");
	        adminEndereco.setEstado("SP");
	        adminEndereco.setRua("Praça da Sé");
	        adminEndereco.setNumero("100");
	        adminEndereco.setBairro("Sé");
	        adminEndereco.setComplemento("Próximo à estação Sé do metrô");
	        
	        logger.error("Preenchendo endereço:'{}'.", adminEndereco);

	        clienteAdmin.setCpf(12345678909L);
	        clienteAdmin.setEndereco(adminEndereco);
	        clienteAdmin.setClienteAtivo(true);
	        clienteAdmin.setNome("Admin");
	        
	        logger.error("Preenchendo cliente:'{}'.", clienteAdmin);

	        userAdmin.setUsername("admin");
	        userAdmin.setPassword(passwordEncoder.encode("minhasenhasuperhipermegapowersecreta11"));
	        userAdmin.setUserAtivo(true);
	        userAdmin.setRole("ADMIN");
	        
	        logger.error("Preenchendo user:'{}'.", userAdmin);

	        clienteAdmin.setEndereco(adminEndereco);
	        userAdmin.setCliente(clienteAdmin);
	        clienteAdmin.setUser(userAdmin);
	        
	        logger.error("User admin completo:'{}'.", userAdmin);
	        logger.error("Cliente admin completo:'{}'.", clienteAdmin);
	        logger.error("Endereço admin completo:'{}'.", adminEndereco);
	        
	        try {	        	
	        	
	            userDao.save(userAdmin);
	            
	            logger.info("Usuário admin criado com sucesso!");
	        } catch (Exception e) {
	            logger.error("Erro ao salvar admin: {}", e.getMessage(), e);
	        }
	    	
	    	
	    } else {
	    	logger.info("Admin já existe. Não é necessário cadastrar novamente!");
	    }
	    
	}
}
