package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import br.com.marcielli.bancom.repository.*;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dto.security.UserCreateDTO;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteCpfInvalidoException;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.exception.ContaExibirSaldoErroException;
import br.com.marcielli.bancom.validation.ValidadorCPF;

@Profile("cliente")
@Service
public class UserClienteService {

	private final UserRepositoryJDBC userRepositoryJDBC;
	private final ClienteRepositoryJDBC clienteRepositoryJDBC;
	private final RoleRepositoryJDBC roleRepositoryJDBC;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserClienteService(UserRepositoryJDBC userRepositoryJDBC, RoleRepositoryJDBC roleRepositoryJDBC,
			BCryptPasswordEncoder passwordEncoder, ClienteRepositoryJDBC clienteRepositoryJDBC) {
		this.userRepositoryJDBC = userRepositoryJDBC;
		this.roleRepositoryJDBC = roleRepositoryJDBC;
		this.passwordEncoder = passwordEncoder;
		this.clienteRepositoryJDBC = clienteRepositoryJDBC;
	}

	@Transactional
	public User save(UserCreateDTO cliente,  JwtAuthenticationToken token) {

		if (cliente.cpf() != null) {
			String cpfClient = Long.toString(cliente.cpf());
			if (!ValidadorCPF.validar(cpfClient)) {
				throw new ClienteCpfInvalidoException("CPF inválido");
			}
		}

		var userFromDb = userRepositoryJDBC.findByUsername(cliente.username());
		if (userFromDb.isPresent()) {
			throw new ClienteEncontradoException("Usuário já existe.");
		}

		var user = new User();
		user.setUsername(cliente.username());
		user.setPassword(passwordEncoder.encode(cliente.password()));

		var basicRole = roleRepositoryJDBC.findByName(Role.Values.BASIC.name());
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

		return userRepositoryJDBC.save(user);
	}


	@Transactional
	public Cliente getClienteById(Long id) {
		return clienteRepositoryJDBC.findById(id).orElse(null);
	}

	@Transactional
	public Cliente update(Long id, UserCreateDTO cliente) {
		
		Optional<User> existingUser = userRepositoryJDBC.findByUsername(cliente.username());
	    if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
	    	throw new ClienteNaoEncontradoException("Esse username já existe.");
	    }

		Cliente clienteExistente = clienteRepositoryJDBC.findById(id)
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

		userRepositoryJDBC.save(user);
		clienteRepositoryJDBC.save(clienteExistente);

		return clienteExistente;
	}

	@Transactional
	public boolean delete(Long id) {
		
		Cliente clienteExistente = clienteRepositoryJDBC.findById(id)
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

		clienteRepositoryJDBC.save(clienteExistente);

		return true;
	}
}
