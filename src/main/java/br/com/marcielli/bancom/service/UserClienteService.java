package br.com.marcielli.bancom.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import br.com.marcielli.bancom.dao.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.dto.security.UserCreateDTO;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteCpfInvalidoException;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.validation.ValidadorCPF;

@Service
public class UserClienteService {

	private final UserDao userDao;
	private final ClienteDao clienteDao;
	private final RoleDao roleDao;
	//private final BCryptPasswordEncoder passwordEncoder;

	public UserClienteService(UserDao userDao, RoleDao roleDao, ClienteDao clienteDao) {
		this.userDao = userDao;
		this.roleDao = roleDao;
		this.clienteDao = clienteDao;
	}



	@Transactional
	public User save(UserCreateDTO cliente,  JwtAuthenticationToken token) {

		if (cliente.cpf() != null) {
			String cpfClient = Long.toString(cliente.cpf());
			if (!ValidadorCPF.validar(cpfClient)) {
				throw new ClienteCpfInvalidoException("CPF inválido");
			}
		}

		if(clienteDao.cpfExists(cliente.cpf())){
			throw new ClienteCpfInvalidoException("CPF número "+cliente.cpf()+" já é cadastrado no sistema.");
		}

		var userFromDb = userDao.findByUsername(cliente.username());
		if (userFromDb.isPresent()) {
			throw new ClienteEncontradoException("Usuário já existe.");
		}

		var user = new User();
		user.setUsername(cliente.username());
		//user.setPassword(passwordEncoder.encode(cliente.password()));
		user.setPassword(cliente.password());
		var basicRole = roleDao.findByName(Role.Values.BASIC.name());
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

		return userDao.save(user);
	}

	@Transactional
	public User getUserById(Long id) {
		return userDao.findById(id)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado."));
	}


	@Transactional
	public List<User> getAllUsers(){
		return userDao.findAll();
	}


	@Transactional
	public User update(Long id, UserCreateDTO dto) {
		// Verifica se o usuário existe
		User user = userDao.findById(id)
				.orElseThrow(() -> new ClienteNaoEncontradoException("Usuário não encontrado"));

		// Verifica se o CPF já está em uso
		Optional<User> existingUserWithCpf = userDao.findByCpf(dto.cpf());
		if (existingUserWithCpf.isPresent() && !existingUserWithCpf.get().getId().equals(user.getId())) {
			throw new ClienteEncontradoException("CPF é único e não pode ser atualizado. " + dto.cpf());
		}

		Optional<User> existingUserWithUsername = userDao.findByUsername(dto.username());
		if(existingUserWithUsername.isPresent() && !existingUserWithUsername.get().equals(user.getId())){
			throw new ClienteEncontradoException("Já existe um cliente com esse username. " + dto.username());
		}

		// Atualiza os dados do cliente, endereço e usuário
		Cliente cliente = user.getCliente();
		cliente.setNome(dto.nome());

		Endereco endereco = cliente.getEndereco();
		if (endereco != null) {
			endereco.setCep(dto.cep());
			endereco.setCidade(dto.cidade());
			endereco.setEstado(dto.estado());
			endereco.setRua(dto.rua());
			endereco.setNumero(dto.numero());
			endereco.setBairro(dto.bairro());
			endereco.setComplemento(dto.complemento());
		}

		user.setUsername(dto.username());
//		user.setPassword(passwordEncoder.encode(dto.password())); // Criptografa a senha
		user.setPassword(dto.password()); // Criptografa a senha
		// Chama o metodo de atualização no repositório
		userDao.update(user); // Atualiza o usuário no banco

		return user;
	}

	@Transactional
	public boolean deleteUser(Long id) {
		return userDao.delete(id);
	}

}
