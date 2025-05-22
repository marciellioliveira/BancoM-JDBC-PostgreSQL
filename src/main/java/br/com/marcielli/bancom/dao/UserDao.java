package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.mappers.ClienteRowMapper;
import br.com.marcielli.bancom.mappers.UserRowMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserDao {

	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

	private final JdbcTemplate jdbcTemplate;

	public UserDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public User save(User user) {

		logger.info(
				"Criando uma classe anônima para implementar o CallableStatementCreator() - Vai montar o comando SQL do { call criar_usuario_completo...");

		// Criando uma classe anônima para implementar o CallableStatementCreator()
		// Vai montar o comando SQL do { call criar_usuario_completo...
		CallableStatementCreator creator = new CallableStatementCreator() {

			@Override
			public CallableStatement createCallableStatement(Connection connection) throws SQLException {

				logger.info("Monta o CallableStatement com os parâmetros que defini dentro da procedure no banco");
				// Monta o CallableStatement com os parâmetros que defini dentro da procedure no
				// banco
				CallableStatement cs = connection
						.prepareCall("CALL criar_usuario_completo_v2(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

				// Abaixo faz igual no anterior, quando não tinha function e procedure
				// Define user, cliente, endereço...
				cs.setString(1, user.getUsername());
				cs.setString(2, user.getPassword());
				cs.setBoolean(3, user.isUserAtivo());

				// logger.info("User Role: {}", user.getRole());
				logger.info("Define user: {}", cs);

				Cliente cliente = user.getCliente();
				cs.setString(4, cliente.getNome());
				cs.setLong(5, cliente.getCpf());
				cs.setBoolean(6, cliente.isClienteAtivo());

				logger.info("Cliente Endereço: {}", cliente.getEndereco());
				logger.info("Define cliente: {}", cs);

				logger.info("User recebido no save(): {}", user);
				logger.info("Cliente recebido no user: {}", user.getCliente());
				logger.info("Endereço recebido no cliente: {}", user.getCliente().getEndereco());

				Endereco endereco = cliente.getEndereco();
				if (endereco == null) {
					logger.error("Endereço é null! O cliente dentro do usuário é: {}", cliente);
					throw new ClienteNaoEncontradoException("Endereço está null na hora de salvar o usuário");
				}

				cs.setString(7, endereco.getRua());
				cs.setString(8, endereco.getNumero());
				cs.setString(9, endereco.getBairro());
				cs.setString(10, endereco.getCidade());
				cs.setString(11, endereco.getEstado());
				cs.setString(12, endereco.getComplemento());
				cs.setString(13, endereco.getCep());
				cs.setString(14, user.getRole());
				logger.info("Define endereço: {}", cs);

				// É o cursor de saída, que na verdade é o ResultSet já com User inserido nele
				// Fiz as functions separadas e uma procedure para chamar as functions e
				// a procedure chama uma function final que retorna um SELECT do user completo.
				logger.info("Registrando REF_CURSOR no índice 15");
				cs.registerOutParameter(15, Types.REF_CURSOR); // PostgreSQL
				logger.info("REF_CURSOR registrado com sucesso no índice 15");

				logger.info("Retorna o cliente completo: {}", cs);
				return cs;
			}
		};

		// A função recebe um CallableStatement e define o que será feito após a
		// execução da procedure
		// que já ta executando. Ela devolve um objeto user.
		CallableStatementCallback<User> callback = new CallableStatementCallback<User>() {

			@Override
			public User doInCallableStatement(CallableStatement cs) throws SQLException {

				// Executa a procedure: criar_usuario_completo.
				cs.execute();

				logger.info("Executa a procedure criar_usuario_completo: {}", cs);

				// Pega o cursor de saída da procedure e retorna um ResultSet
				// ou seja, esse cursor/ponteiro é convertido para um ResultSet
				// porque o ResultSet é uma estrutura que permite percorrer dados
				try (ResultSet rs = (ResultSet) cs.getObject(15)) {

					// Verificando se o ResultSet tem pelo menos uma linha para percorrer
					if (rs.next()) {
						logger.info("Dados encontrados no ResultSet na primeira linha");

						// Cria uma instancia do meu ClienteRowMapper, meio que
						// convertendo o ResultSet em um Objeto Java
						// populando os dados de User, Cliente, Endereço...
						ClienteRowMapper rowMapper = new ClienteRowMapper();

						// Transforma em um Ojeto
						Cliente cliente = rowMapper.mapRow(rs, 0);
						logger.info("Cliente rowMapper: {}", cliente);
						// Pega um User e associa ao cliente
						User result = cliente.getUser();
						// Retorna o user completo
						logger.info("Retorna usuário completo: {}", result);
						return result;
					} else {
						logger.warn("ResultSet está vazio. Nenhum usuário retornado pela procedure.");
						throw new RuntimeException("Usuário não retornado pela procedure");
					}

				} catch (SQLException e) {
					logger.error("Erro ao obter ou percorrer o ResultSet no índice 15", e);
					throw e;
				}
			}
		};

		return jdbcTemplate.execute(creator, callback);
	}

	public User update(User user) {
		logger.info("Iniciando método update(User user)");

		if (user == null || user.getCliente() == null || user.getCliente().getEndereco() == null) {
			logger.error("Usuário, Cliente ou Endereço estão nulos. Interrompendo execução.");
			throw new IllegalArgumentException("Usuário, Cliente ou Endereço não podem ser nulos");
		}

		CallableStatementCreator creator = connection -> {
			logger.info("Montando CallableStatement para procedure: atualizar_usuario_completo_v1");

			CallableStatement cs = connection
					.prepareCall("CALL atualizar_usuario_completo_v1(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			cs.setInt(1, user.getId());
			cs.setString(2, user.getUsername());
			cs.setBoolean(3, user.isUserAtivo());
			logger.debug("Parâmetros do usuário definidos");

			Cliente cliente = user.getCliente();
			cs.setLong(4, cliente.getId());
			cs.setString(5, cliente.getNome());
			cs.setBoolean(6, cliente.isClienteAtivo());
			logger.debug("Parâmetros do cliente definidos");

			Endereco endereco = cliente.getEndereco();
			cs.setString(7, endereco.getRua());
			cs.setString(8, endereco.getNumero());
			cs.setString(9, endereco.getBairro());
			cs.setString(10, endereco.getCidade());
			cs.setString(11, endereco.getEstado());
			cs.setString(12, endereco.getComplemento());
			cs.setString(13, endereco.getCep());
			logger.debug("Parâmetros do endereço definidos");

			cs.registerOutParameter(14, Types.REF_CURSOR);
			logger.debug("REF_CURSOR registrado com sucesso");

			return cs;
		};

		CallableStatementCallback<User> callback = cs -> {
			logger.info("Executando procedure atualizar_usuario_completo_v1");
			cs.execute();

			try (ResultSet rs = (ResultSet) cs.getObject(14)) {
				if (rs != null && rs.next()) {
					logger.info("Dados encontrados no ResultSet");

					ClienteRowMapper rowMapper = new ClienteRowMapper();
					Cliente clienteAtualizado = rowMapper.mapRow(rs, 0);
					User userAtualizado = clienteAtualizado.getUser();

					logger.info("Usuário atualizado retornado com sucesso: {}", userAtualizado);
					return userAtualizado;
				} else {
					logger.warn("Nenhum dado retornado no ResultSet");
					throw new RuntimeException("Usuário não retornado pela procedure");
				}
			} catch (SQLException e) {
				logger.error("Erro ao processar ResultSet", e);
				throw e;
			}
		};

		try {
			return jdbcTemplate.execute(creator, callback);
		} catch (Exception e) {
			logger.error("Erro ao executar update no banco de dados", e);
			throw new RuntimeException("Erro ao atualizar usuário no banco de dados", e);
		}
	}

	public Optional<User> findByUsername(String username) {
		String sql = "SELECT * FROM get_usuario_completo_by_username_v1(?)";

		logger.info("Chamando função get_usuario_completo_by_username_v1 com username: {}", username);

		try {
			User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				logger.debug("Mapeando resultado da função para User...");

				User u = new User();
				u.setId(rs.getInt("user_id"));
				u.setUsername(rs.getString("username"));
				u.setPassword(rs.getString("password"));
				u.setUserAtivo(rs.getBoolean("user_ativo"));

				String roleName = rs.getString("role_name");
				u.setRole(roleName != null ? roleName : "ROLE_NENHUMA");

				// Cliente
				Cliente cliente = null;
				long clienteId = rs.getLong("cliente_id");
				if (!rs.wasNull()) {
					cliente = new Cliente();
					cliente.setId(clienteId);
					String cpfStr = rs.getString("cpf");
					if (cpfStr != null) {
						try {
							cliente.setCpf(Long.parseLong(cpfStr.replaceAll("\\D", "")));
						} catch (NumberFormatException e) {
							logger.error("Erro ao converter CPF '{}' para Long", cpfStr, e);
							cliente.setCpf(0L);
						}
					}
					cliente.setNome(rs.getString("nome"));
					cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));

					// Endereço
					Endereco endereco = null;
					long enderecoId = rs.getLong("endereco_id");
					if (!rs.wasNull()) {
						endereco = new Endereco();
						endereco.setId(enderecoId);
						endereco.setRua(rs.getString("rua"));
						endereco.setNumero(rs.getString("numero"));
						endereco.setBairro(rs.getString("bairro"));
						endereco.setCidade(rs.getString("cidade"));
						endereco.setEstado(rs.getString("estado"));
						endereco.setComplemento(rs.getString("complemento"));
						endereco.setCep(rs.getString("cep"));
					}
					cliente.setEndereco(endereco);
				}

				u.setCliente(cliente);

				logger.debug("User mapeado com sucesso: {}", u.getUsername());
				return u;
			}, username);

			logger.info("Usuário encontrado via função: {}", user.getUsername());
			return Optional.ofNullable(user);

		} catch (EmptyResultDataAccessException e) {
			logger.info("Nenhum usuário encontrado para username: {}", username);
			return Optional.empty();
		} catch (Exception e) {
			logger.error("Erro ao buscar usuário por username via função: {}", username, e);
			return Optional.empty();
		}
	}

	public List<User> findAll() {
		logger.info("Iniciando busca de todos os usuários via função PostgreSQL");

		CallableStatementCreator creator = connection -> {
			logger.debug("Preparando CallableStatement para função get_all_usuarios_completos_v1");
			CallableStatement cs = connection.prepareCall("SELECT * FROM get_all_usuarios_completos_v1()");
			return cs;
		};

		CallableStatementCallback<List<User>> callback = cs -> {
			List<User> users = new ArrayList<User>();

			boolean hasResult = cs.execute();
			if (hasResult) {
				try (ResultSet rs = cs.getResultSet()) {
					UserRowMapper mapper = new UserRowMapper();
					int rowNum = 0;
					while (rs.next()) {
						users.add(mapper.mapRow(rs, rowNum++));
					}
				}
			}

			logger.info("Total de usuários encontrados: {}", users.size());
			return users;
		};

		try {
			return jdbcTemplate.execute(creator, callback);
		} catch (Exception e) {
			logger.error("Erro ao buscar todos os usuários via função PostgreSQL", e);
			throw new RuntimeException("Erro ao buscar usuários", e);
		}
	}

	public Optional<User> findById(Long id) {
		logger.info("Iniciando busca do usuário pelo ID: {}", id);

		try {
			String sql = "SELECT * FROM find_user_by_id_v1(?)";
			logger.debug("Executando SQL: {}", sql);

			List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id.intValue());

			if (users.isEmpty()) {
				logger.warn("Nenhum usuário encontrado com ID: {}", id);
				return Optional.empty();
			} else {
				logger.info("Usuário encontrado com sucesso para o ID: {}", id);
				return Optional.of(users.get(0));
			}
		} catch (Exception e) {
			logger.error("Erro ao buscar usuário com ID: {}", id, e);
			throw new RuntimeException("Erro ao buscar usuário por ID", e);
		}
	}

	public Cliente findByIdWithContas(Long clienteId) {
		logger.info("Buscando cliente completo via função PostgreSQL");

		CallableStatementCreator creator = connection -> {
			logger.debug("Preparando CallableStatement para get_cliente_completo_by_id_v1");
			CallableStatement cs = connection.prepareCall("SELECT * FROM get_cliente_completo_by_id_v1(?)");
			cs.setLong(1, clienteId);
			return cs;
		};

		CallableStatementCallback<Cliente> callback = cs -> {
			cs.execute();
			try (ResultSet rs = cs.getResultSet()) {
				Cliente cliente = null;
				List<Conta> contas = new ArrayList<>();

				while (rs.next()) {
					if (cliente == null) {
						cliente = new Cliente();
						cliente.setId(rs.getLong("cliente_id"));
						cliente.setNome(rs.getString("cliente_nome"));
						cliente.setCpf(rs.getLong("cliente_cpf"));
						cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));

						User user = new User();
						user.setId(rs.getInt("user_id"));
						user.setUsername(rs.getString("user_username"));
						user.setPassword(rs.getString("user_password"));
						user.setUserAtivo(rs.getBoolean("user_ativo"));
						cliente.setUser(user);

						Endereco endereco = new Endereco();
						endereco.setId(rs.getLong("endereco_id"));
						endereco.setCep(rs.getString("cep"));
						endereco.setCidade(rs.getString("cidade"));
						endereco.setEstado(rs.getString("estado"));
						endereco.setRua(rs.getString("rua"));
						endereco.setNumero(rs.getString("numero"));
						endereco.setBairro(rs.getString("bairro"));
						endereco.setComplemento(rs.getString("complemento"));
						cliente.setEndereco(endereco);
					}

					Long contaId = rs.getLong("conta_id");
					if (contaId != 0) {
						Conta conta = new Conta();
						conta.setId(contaId);
						conta.setNumeroConta(rs.getString("numero_conta"));
						conta.setSaldoConta(rs.getBigDecimal("saldo"));
						conta.setStatus(rs.getBoolean("status"));
						try {
							String tipoStr = rs.getString("tipo");
							if (tipoStr != null) {
								conta.setTipoConta(TipoConta.valueOf(tipoStr.toUpperCase()));
							}
						} catch (IllegalArgumentException e) {
							logger.warn("Tipo de conta inválido: {}", rs.getString("tipo"), e);
						}

						contas.add(conta);
					}
				}

				if (cliente != null) {
					cliente.setContas(contas);
				}

				return cliente;
			}
		};

		try {
			return jdbcTemplate.execute(creator, callback);
		} catch (Exception e) {
			logger.error("Erro ao buscar cliente completo via função", e);
			throw new RuntimeException("Erro ao buscar cliente com contas", e);
		}
	}

	public Optional<User> findByCpf(Long cpf) {
		if (cpf == null) {
			return Optional.empty();
		}
		String sql = "SELECT * FROM find_user_by_cpf_v1(?)";

		List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), cpf);

		return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
	}

	public boolean desativarCliente(Long clienteId) {
		logger.info("Iniciando desativação do cliente via procedure");

		CallableStatementCreator creator = connection -> {
			logger.debug("Preparando CallableStatement para desativar_cliente_procedure");
			CallableStatement cs = connection.prepareCall("CALL desativar_cliente_procedure(?)");
			cs.setLong(1, clienteId);
			return cs;
		};

		CallableStatementCallback<Boolean> callback = cs -> {
			cs.execute();
			logger.info("Procedure desativar_cliente_procedure executada com sucesso");
			return true;
		};

		try {
			return jdbcTemplate.execute(creator, callback);
		} catch (Exception e) {
			logger.error("Erro ao executar procedure desativar_cliente_procedure", e);
			return false;
		}
	}

	public boolean existeCliente(Long clienteId) {
		logger.info("Verificando existência do cliente via função PostgreSQL");

		CallableStatementCreator creator = connection -> {
			logger.debug("Preparando CallableStatement para função existe_cliente");
			CallableStatement cs = connection.prepareCall("{ ? = call existe_cliente(?) }");
			cs.registerOutParameter(1, Types.BOOLEAN);
			cs.setLong(2, clienteId);
			return cs;
		};

		CallableStatementCallback<Boolean> callback = cs -> {
			cs.execute();
			boolean existe = cs.getBoolean(1);
			logger.info("Resultado da verificação: {}", existe);
			return existe;
		};

		try {
			return jdbcTemplate.execute(creator, callback);
		} catch (Exception e) {
			logger.error("Erro ao verificar existência do cliente", e);
			throw new RuntimeException("Erro ao verificar existência do cliente", e);
		}
	}

	public boolean ativarCliente(Long clienteId) {
		logger.info("Iniciando ativação do cliente via procedure");

		CallableStatementCreator creator = connection -> {
			logger.debug("Preparando CallableStatement para ativar_cliente_procedure");
			CallableStatement cs = connection.prepareCall("CALL ativar_cliente_procedure(?)");
			cs.setLong(1, clienteId);
			return cs;
		};

		CallableStatementCallback<Boolean> callback = cs -> {
			cs.execute();
			logger.info("Procedure ativar_cliente_procedure executada com sucesso");
			return true;
		};

		try {
			return jdbcTemplate.execute(creator, callback);
		} catch (Exception e) {
			logger.error("Erro ao executar procedure ativar_cliente_procedure", e);
			return false;
		}
	}

}
