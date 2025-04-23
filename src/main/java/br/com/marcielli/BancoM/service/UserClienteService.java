package br.com.marcielli.BancoM.service;

import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.security.UserCreateDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.entity.Role;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.exception.ClienteEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.RoleRepository;
import br.com.marcielli.BancoM.repository.UserRepository;

@Service
public class UserClienteService {
	
	private final UserRepository userRepository;
	private final ClienteRepository clienteRepository;
	private final RoleRepository roleRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserClienteService(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder, ClienteRepository clienteRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.clienteRepository = clienteRepository;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public User save(UserCreateDTO cliente) {
//		
//		//Vejo o usuário logado para deixar apenas o admin criar vários clientes
//		 String usernameLogado = SecurityContextHolder.getContext().getAuthentication().getName();
//		 User usuarioLogado = userRepository.findByUsername(usernameLogado)
//			        .orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado."));
//		 
//		 boolean isAdmin = usuarioLogado.getRoles().stream()
//			        .anyMatch(role -> role.getName().equals(Role.Values.ADMIN.name()));
//		 
//		 if (!isAdmin) {
//		        if (usuarioLogado.getCliente() != null) {
//		            throw new ClienteEncontradoException("Usuário comum só pode criar um cliente.");
//		        }
//		    }
		 
		var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
		var userFromDb = userRepository.findByUsername(cliente.username());
		
		if(userFromDb.isPresent()) { //É uma entidade com erro de negócio da requisição da API
			throw new ClienteNaoEncontradoException("Erro. Tente novamente mais tarde.");
		}
		
		//Se não existe, cria um novo usuário
		var user = new User();
		user.setUsername(cliente.username());
		user.setPassword(passwordEncoder.encode(cliente.password()));
		user.setRoles(Set.of(basicRole)); //Preciso para saber se ele é basic e poder acessar certas paginas
				
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
		
		userRepository.save(user);
		
		return user;
		
	}

	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cliente getClienteById(Long id) {
		return clienteRepository.findById(id).orElse(null);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cliente update(Long id, UserCreateDTO cliente) {
		
		Cliente clienteExistente = clienteRepository.findById(id).orElse(null);
		
		if (clienteExistente == null) {
			 return null;
		}
		
		clienteExistente.setNome(cliente.nome());
	    clienteExistente.setCpf(cliente.cpf());
	    
	    Endereco endereco = clienteExistente.getEndereco();
	    if (endereco != null) {
	        endereco.setCep(cliente.cep());
	        endereco.setCidade(cliente.cidade());
	        endereco.setEstado(cliente.estado());
	        endereco.setRua(cliente.rua());
	        endereco.setNumero(cliente.numero());
	        endereco.setBairro(cliente.bairro());
	        endereco.setComplemento(cliente.complemento());
	    }
	    
	    User user = clienteExistente.getUser();
	    if (user != null) {
	        user.setUsername(cliente.username());
	        user.setPassword(passwordEncoder.encode(cliente.password()));
	      
	    } else {
	      
	        user = new User();
	        user.setUsername(cliente.username());
	        user.setPassword(passwordEncoder.encode(cliente.password()));
	  
	        user.setCliente(clienteExistente);
	        clienteExistente.setUser(user);
	    }
	    
	    userRepository.save(user);
	    clienteRepository.save(clienteExistente);
		
		return clienteExistente;
		
	}
	
	@Transactional
	public boolean delete(Long id) {
		
		Cliente clienteExistente = clienteRepository.findById(id).orElse(null);
		
		boolean isAdmin = clienteExistente.getUser().getRoles().stream()
			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
		
		if(isAdmin) {
			throw new ClienteNaoEncontradoException("Não é possível deletar o admin do sistema.");
		}
		
		
		clienteExistente.setClienteAtivo(false);
		clienteExistente.getUser().setUserAtivo(false);
		
		//userRepository.deleteById(clienteExistente.getUser().getId());
		
	    return true;
	}

}
