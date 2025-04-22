package br.com.marcielli.BancoM.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;
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
	
	//@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Cliente save(Cliente cliente) {
//		
//	}

}
