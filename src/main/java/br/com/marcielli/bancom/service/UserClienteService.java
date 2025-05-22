package br.com.marcielli.bancom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.com.marcielli.bancom.dao.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dto.security.UserCreateDTO;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import jakarta.annotation.PostConstruct;

@Service
public class UserClienteService implements UserDetailsService { //implements UserDetailsService 

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	private static final Logger logger = LoggerFactory.getLogger(UserClienteService.class);

	private final UserDao userDao; // e devolver pro Spring Security pra fazer a autenticação
	private final ClienteDao clienteDao;
	private final RoleDao roleDao;
	// private final BCryptPasswordEncoder passwordEncoder;

	public UserClienteService(UserDao userDao, RoleDao roleDao, ClienteDao clienteDao) {
		this.userDao = userDao;
		this.roleDao = roleDao;
		this.clienteDao = clienteDao;
	}

	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    User user = userDao.findByUsername(username)
	        .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado"));

	    System.err.println("Encontrou user: "+user);
	    String role = user.getRole();

	    if (role == null || role.isEmpty()) {
	        System.out.println("Usuário '" + username + "' não tem role associada!");
	        throw new ClienteNaoEncontradoException("Usuário não possui role associada");
	    }

	    System.err.println("ROLE ESTRANHA: " + role);

	    String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;

	    GrantedAuthority authority = new SimpleGrantedAuthority(roleName);

	    System.out.println("Usuário encontrado: " + user.getUsername());
	    System.out.println("Role do usuário: " + roleName);

	    return new org.springframework.security.core.userdetails.User(
	        user.getUsername(),
	        user.getPassword(),
	        List.of(authority)
	    );
	}



	
	@Transactional
	public User findByUsername(String username) {
		return userDao.findByUsername(username).orElse(null);
	}

	@Transactional
	public User save(UserCreateDTO dto) {
		
		Optional<User> cpfExiste = userDao.findByCpf(Long.parseLong(dto.cpf()));
		Optional<User> usernameExiste = userDao.findByUsername(dto.username());
		
		if(cpfExiste.isPresent()) {
			 throw new ClienteEncontradoException("Já existe uma conta com o CPF no sistema");
		}
		
		if (usernameExiste.isPresent()) {
		    throw new ClienteEncontradoException("Username já existe. Escolha outro.");
		}
		
		User user = new User();
		user.setUsername(dto.username());
		user.setPassword(passwordEncoder.encode(dto.password()));
		user.setUserAtivo(true);
		user.setRole("BASIC");
		Cliente cliente = new Cliente();
		cliente.setNome(dto.nome());
		cliente.setCpf(Long.parseLong(dto.cpf()));
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
			
		} catch (DataIntegrityViolationException e) {
			String message = e.getMessage();
			
			// Se ainda assim der erro de duplicidade, trata com uma exceção customizada mais clara
	        if (message != null && message.contains("users_username_key")) {
	        	logger.error("Username '{}' já existe.", dto.username());
	            throw new ClienteEncontradoException("Username já existe. Escolha outro.");
	        }
	        if (message != null && message.contains("clientes_cpf_key")) {
	        	logger.error("Cpf '{}' já existe.", dto.cpf());
	            throw new ClienteEncontradoException("CPF já está cadastrado no sistema.");
	        }
			
	        logger.error("Outro erro: ", e);
	        
			throw e; // Outros erros são lançados normalmente
		}
	}

	@Transactional
	public User getUserById(Long id, Authentication authentication) throws ClienteEncontradoException {    
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    String username = authentication.getName();
	    
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

	    if (user.getCliente() != null) {
	        Cliente clienteComContas = clienteDao.findByIdWithContasAndTransferencias(user.getCliente().getId());
	        user.setCliente(clienteComContas);
	       
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
	        
	        users = userDao.findAll();
	    } else if ("ROLE_BASIC".equals(role)) {
	   
	        User loggedInUser = userDao.findByUsername(username)
	                .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));
	        users = List.of(loggedInUser);
	    } else {
	        throw new ClienteEncontradoException("Você não tem permissão para acessar a lista de usuários.");
	    }
	   
	    for (User user : users) {
	        if (user.getCliente() != null) {
	        	
	        	// carregando o cliente completo com as contas e transferências
	            Cliente clienteCompleto = clienteDao.findByIdWithContasAndTransferencias(user.getCliente().getId());
	            
	            //se a lista de contas for null, ela precisa ser iniciada vazia
	            if(clienteCompleto.getContas() == null) {
	            	 clienteCompleto.setContas(new ArrayList<Conta>());
	            }
	            
	            user.setCliente(clienteCompleto);
	            
	            // carregando as transferências enviadas para cada conta.
	            clienteCompleto.getContas().forEach(conta -> {
	                // usando rowmapper existente para carregar as transferências
	               // System.out.println("Conta ID " + conta.getId() + ", clienteNome: " + conta.getClienteNome());
	              
	                // iterando sobre as transferências de cada conta
	                conta.getTransferencias().forEach(transferencia -> {
	                    System.out.println("  Transferência Enviada ID: " + transferencia.getId() + ", Valor: " + transferencia.getValor());
	                });
	            });

	        }
	    }

	    return users;
	}
	
	@Transactional
	public User update(Long id, UserCreateDTO dto, Authentication authentication) throws ClienteEncontradoException {

	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    String username = authentication.getName(); // username do usuário logado

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

	    // Valida username
	    user.setUsername(dto.username());
	    if (userExistente != null && !userExistente.getId().equals(user.getId())) {
	        throw new ClienteNaoEncontradoException("Este nome de usuário já está em uso.");
	    }

	    // Valida CPF se diferente do atual
	    Long novoCpf = Long.parseLong(dto.cpf());
	    Cliente cliente = user.getCliente();
	    Long cpfAtual = cliente.getCpf();

	    if (!novoCpf.equals(cpfAtual)) {
	        Optional<User> userComCpf = userDao.findByCpf(novoCpf);
	        if (userComCpf.isPresent() && !userComCpf.get().getId().equals(user.getId())) {
	            throw new ClienteEncontradoException("CPF já está cadastrado no sistema.");
	        }
	    }

	    // Atualiza CPF e outros dados do cliente
	    cliente.setNome(dto.nome());
	    cliente.setCpf(novoCpf);

	    // Atualiza senha, se enviada
	    if (dto.password() != null && !dto.password().isEmpty()) {
	        String encodedPassword = passwordEncoder.encode(dto.password());
	        user.setPassword(encodedPassword);
	    }

	    // Atualiza endereço
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
	    } catch (DataIntegrityViolationException e) {
	        String message = e.getMessage();

	        if (message != null && message.contains("clientes_cpf_key")) {
	            logger.error("Cpf '{}' já existe.", dto.cpf());
	            throw new ClienteEncontradoException("CPF já está cadastrado no sistema.");
	        }

	        logger.error("Outro erro: ", e);

	        throw e; // Outros erros são lançados normalmente
	    }
	}
	
	
	@Transactional
	public boolean deleteUser(Long id, Authentication authentication) throws ClienteEncontradoException {
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    String username = authentication.getName(); 
	    
	    User loggedInUser = userDao.findByUsername(username)
	        .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário logado não encontrado."));

	    if ("ROLE_ADMIN".equals(role)) {
	        // Admin não pode se deletar
	        if (id.equals(loggedInUser.getId().longValue())) {
	            throw new ClienteEncontradoException("Administradores não podem deletar a si mesmos.");
	        }
	        return userDao.desativarCliente(id);
	    } else if ("ROLE_BASIC".equals(role)) {
	        // Basic só pode deletar a si mesmo
	        if (!id.equals(loggedInUser.getId().longValue())) {
	            throw new ClienteEncontradoException("Usuário BASIC não tem permissão para deletar outros usuários.");
	        }
	        return userDao.desativarCliente(id);
	    } else {
	        throw new ClienteEncontradoException("Role não autorizada para deletar usuários.");
	    }
	}


	
	@Transactional
	public boolean ativarCliente(Long id, Authentication authentication) throws ClienteEncontradoException {
	    String role = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst()
	            .orElse("");

	    if (!"ROLE_ADMIN".equals(role)) {
	        // Somente admin pode ativar cliente
	    	throw new ClienteEncontradoException("Somente administradores podem ativar o cliente");	        
	    } 	    
	    
	    if (!userDao.existeCliente(id)) {
	        throw new ClienteEncontradoException("Cliente não encontrado.");
	    }	    
	    
	    return userDao.ativarCliente(id);
	}


}
