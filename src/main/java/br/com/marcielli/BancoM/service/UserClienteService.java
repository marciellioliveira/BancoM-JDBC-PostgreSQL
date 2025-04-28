package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.security.UserCreateDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.entity.Role;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.exception.ClienteCpfInvalidoException;
import br.com.marcielli.BancoM.exception.ClienteEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ContaExibirSaldoErroException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.RoleRepository;
import br.com.marcielli.BancoM.repository.UserRepository;
import br.com.marcielli.BancoM.validation.ValidadorCPF;

@Service
public class UserClienteService {

	private final UserRepository userRepository;
	private final ClienteRepository clienteRepository;
	private final RoleRepository roleRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserClienteService(UserRepository userRepository, RoleRepository roleRepository,
			BCryptPasswordEncoder passwordEncoder, ClienteRepository clienteRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.clienteRepository = clienteRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public User save(UserCreateDTO cliente,  JwtAuthenticationToken token) {

		if (cliente.cpf() != null) {
			String cpfClient = Long.toString(cliente.cpf());
			if (!ValidadorCPF.validar(cpfClient)) {
				throw new ClienteCpfInvalidoException("CPF inválido");
			}
		}

		var userFromDb = userRepository.findByUsername(cliente.username());
		if (userFromDb.isPresent()) {
			throw new ClienteEncontradoException("Usuário já existe.");
		}

		var user = new User();
		user.setUsername(cliente.username());
		user.setPassword(passwordEncoder.encode(cliente.password()));

		var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
		user.setRoles(Set.of(basicRole));

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

		return userRepository.save(user);
	}


	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cliente getClienteById(Long id) {
		return clienteRepository.findById(id).orElse(null);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cliente update(Long id, UserCreateDTO cliente) {
		
		Optional<User> existingUser = userRepository.findByUsername(cliente.username());
	    if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
	    	throw new ClienteNaoEncontradoException("Esse username já existe.");
	    }

		Cliente clienteExistente = clienteRepository.findById(id)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));

		if (!clienteExistente.isClienteAtivo() || !clienteExistente.getUser().isUserAtivo()) {
			throw new ClienteNaoEncontradoException("Não é possível atualizar um usuário/cliente desativado");
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
		
		Cliente clienteExistente = clienteRepository.findById(id)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));

		if (!clienteExistente.isClienteAtivo() || !clienteExistente.getUser().isUserAtivo()) {
			throw new ClienteNaoEncontradoException("O usuário/cliente já está desativado");
		}

		for(Conta contas : clienteExistente.getContas()) {
			if(contas.getSaldoConta().compareTo(BigDecimal.ZERO) > 0) {
				throw new ContaExibirSaldoErroException("A conta possui um saldo de R$ "+contas.getSaldoConta()+". Faça o saque antes de remover o cliente.");
			}
		}
		
		clienteExistente.setClienteAtivo(false);
		clienteExistente.getUser().setUserAtivo(false);

		clienteRepository.save(clienteExistente);

		return true;
	}

}
