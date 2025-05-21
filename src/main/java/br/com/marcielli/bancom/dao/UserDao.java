package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.mappers.ClienteRowMapper;
import br.com.marcielli.bancom.mappers.ContasRowMapper;
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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Component
public class UserDao {

	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
	//Exemplo: logger.info("Fatura {} associada ao cartão {} com sucesso", faturaId, cartaoId);
	
    private final JdbcTemplate jdbcTemplate;
    private final RoleDao roleDao;

    public UserDao(JdbcTemplate jdbcTemplate, RoleDao roleDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleDao = roleDao;
    }
    
    
    public User save(User user) {
	
    	logger.info("Criando uma classe anônima para implementar o CallableStatementCreator() - Vai montar o comando SQL do { call criar_usuario_completo...");
    	
    	//Criando uma classe anônima para implementar o CallableStatementCreator()
    	//Vai montar o comando SQL do { call criar_usuario_completo...
        CallableStatementCreator creator = new CallableStatementCreator() {
        	
            @Override
            public CallableStatement createCallableStatement(Connection connection) throws SQLException {
            	
            	logger.info("Monta o CallableStatement com os parâmetros que defini dentro da procedure no banco");
            	//Monta o CallableStatement com os parâmetros que defini dentro da procedure no banco
            	CallableStatement cs = connection.prepareCall("CALL criar_usuario_completo_v2(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                
                //Abaixo faz igual no anterior, quando não tinha function e procedure
                //Define user, cliente, endereço...
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
           
                //É o cursor de saída, que na verdade é o ResultSet já com User inserido nele
                //Fiz as functions separadas e uma procedure para chamar as functions e 
                //a procedure chama uma function final que retorna um SELECT do user completo.                    
                logger.info("Registrando REF_CURSOR no índice 15");
                cs.registerOutParameter(15, Types.REF_CURSOR); // PostgreSQL
                logger.info("REF_CURSOR registrado com sucesso no índice 15");
                
                logger.info("Retorna o cliente completo: {}", cs);
                return cs;
            }
        };
        
        //A função recebe um CallableStatement e define o que será feito após a execução da procedure
        //que já ta executando. Ela devolve um objeto user.
        CallableStatementCallback<User> callback = new CallableStatementCallback<User>() {
        	
            @Override
            public User doInCallableStatement(CallableStatement cs) throws SQLException {
            	
            	//Executa a procedure: criar_usuario_completo.
                cs.execute();
                
                logger.info("Executa a procedure criar_usuario_completo: {}", cs);
                
                
                
                //Pega o cursor de saída da procedure e retorna um ResultSet
                //ou seja, esse cursor/ponteiro é convertido para um ResultSet
                //porque o ResultSet é uma estrutura que permite percorrer dados
                try (ResultSet rs = (ResultSet) cs.getObject(15)) {
                	               	
                	//Verificando se o ResultSet tem pelo menos uma linha para percorrer
                    if (rs.next()) {
                    	logger.info("Dados encontrados no ResultSet na primeira linha");
                    	
                    	//Cria uma instancia do meu ClienteRowMapper, meio que
                    	//convertendo o ResultSet em um Objeto Java
                    	//populando os dados de User, Cliente, Endereço...
                        ClienteRowMapper rowMapper = new ClienteRowMapper();
                        
                        //Transforma em um Ojeto
                        Cliente cliente = rowMapper.mapRow(rs, 0);
                        logger.info("Cliente rowMapper: {}", cliente);
                        //Pega um User e associa ao cliente
                        User result = cliente.getUser();
                        //Retorna o user completo
                        logger.info("Retorna usuário completo: {}", result);
                        return result;
                    } else {
                    	logger.warn("ResultSet está vazio. Nenhum usuário retornado pela procedure.");
                        throw new RuntimeException("Usuário não retornado pela procedure");
                    }
                    
                } catch (SQLException e) {
                    logger.error("Erro ao obter ou percorrer o ResultSet no índice 13", e);
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

            CallableStatement cs = connection.prepareCall("CALL atualizar_usuario_completo_v1(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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





    
//    public User update(User user) {
//        // Atualiza o usuário
//        String sqlUser = "UPDATE users SET username = ?, user_ativo = ? WHERE id = ?";
//        jdbcTemplate.update(
//            sqlUser,
//            user.getUsername(),
//            user.isUserAtivo(),
//            user.getId()
//        );
//
//        // Atualiza os dados do cliente
//        Cliente cliente = user.getCliente();
//        String sqlCliente = "UPDATE clientes SET nome = ?, cliente_ativo = ? WHERE id = ?";
//        jdbcTemplate.update(
//            sqlCliente,
//            cliente.getNome(),
//            cliente.isClienteAtivo(),
//            cliente.getId()
//        );
//
//        // Atualiza o endereço do cliente
//        Endereco endereco = cliente.getEndereco();
//        String sqlEndereco = "UPDATE enderecos SET rua = ?, numero = ?, bairro = ?, cidade = ?, estado = ?, complemento = ?, cep = ? WHERE cliente_id = ?";
//        jdbcTemplate.update(
//            sqlEndereco,
//            endereco.getRua(),
//            endereco.getNumero(),
//            endereco.getBairro(),
//            endereco.getCidade(),
//            endereco.getEstado(),
//            endereco.getComplemento(),
//            endereco.getCep(),
//            cliente.getId()
//        );
//
//        return user;
//    }
    
//    public Optional<User> findByUsername(String username) {
//        String sql = """
//            SELECT u.id AS user_id, u.username, u.password, u.user_ativo, 
//                   c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo,
//                   r.name AS role_name
//            FROM users u
//            JOIN user_roles ur ON u.id = ur.user_id
//            JOIN roles r ON r.id = ur.role_id
//            JOIN clientes c ON c.user_id = u.id
//            WHERE u.username = ?""";
//
//        try {
//            User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
//                User u = new User();
//                u.setId(rs.getInt("user_id"));
//                u.setUsername(rs.getString("username"));
//                u.setPassword(rs.getString("password"));
//                u.setUserAtivo(rs.getBoolean("user_ativo"));
//                u.setRole(rs.getString("role_name"));
//                
//                Cliente cliente = new Cliente();
//                cliente.setId(rs.getLong("cliente_id")); // Usando getLong diretamente
//                
//                // Conversão segura de String para Long no CPF
//                String cpfStr = rs.getString("cpf");
//                if (cpfStr != null) {
//                    try {
//                        cliente.setCpf(Long.parseLong(cpfStr.replaceAll("\\D", "")));
//                    } catch (NumberFormatException e) {
//                        cliente.setCpf(0L); // Ou outro valor padrão
//                    }
//                }
//                
//                cliente.setNome(rs.getString("nome"));
//                cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));
//                u.setCliente(cliente);
//                
//                return u;
//            }, username);
//            
//            return Optional.ofNullable(user);
//        } catch (EmptyResultDataAccessException e) {
//            return Optional.empty();
//        }
//    }
    
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

    
//    public Optional<User> findByUsername(String username) {
//        String sql = """
//            SELECT u.id AS user_id, u.username, u.password, u.user_ativo, 
//                   c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo,
//                   r.name AS role_name
//            FROM users u
//            LEFT JOIN user_roles ur ON u.id = ur.user_id
//            LEFT JOIN roles r ON r.id = ur.role_id
//            LEFT JOIN clientes c ON c.user_id = u.id
//            WHERE u.username = ?
//        """;
//
//        logger.info("Buscando usuário pelo username: {}", username);
//
//        try {
//            User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
//                logger.debug("Mapeando resultado da query para User...");
//
//                User u = new User();
//                u.setId(rs.getInt("user_id"));
//                u.setUsername(rs.getString("username"));
//                u.setPassword(rs.getString("password"));
//                u.setUserAtivo(rs.getBoolean("user_ativo"));
//
//                String roleName = rs.getString("role_name");
//                if (roleName == null) {
//                    logger.warn("Usuário '{}' não tem role associada.", username);
//                    u.setRole("ROLE_NENHUMA"); // ou null, conforme seu modelo
//                } else {
//                    u.setRole(roleName);
//                }
//
//                Cliente cliente = new Cliente();
//                long clienteId = rs.getLong("cliente_id");
//                if (rs.wasNull()) {
//                    logger.warn("Usuário '{}' não tem cliente associado.", username);
//                    cliente = null;
//                } else {
//                    cliente.setId(clienteId);
//                    
//                    String cpfStr = rs.getString("cpf");
//                    if (cpfStr != null) {
//                        try {
//                            cliente.setCpf(Long.parseLong(cpfStr.replaceAll("\\D", "")));
//                        } catch (NumberFormatException e) {
//                            logger.error("Erro ao converter CPF '{}' para Long", cpfStr, e);
//                            cliente.setCpf(0L);
//                        }
//                    }
//                    
//                    cliente.setNome(rs.getString("nome"));
//                    cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));
//                }
//                u.setCliente(cliente);
//
//                logger.debug("User mapeado: {}", u.getUsername());
//                return u;
//            }, username);
//
//            logger.info("Usuário encontrado: {}", user.getUsername());
//            return Optional.ofNullable(user);
//
//        } catch (EmptyResultDataAccessException e) {
//            logger.info("Nenhum usuário encontrado para username: {}", username);
//            return Optional.empty();
//        } catch (Exception e) {
//            logger.error("Erro ao buscar usuário por username: {}", username, e);
//            return Optional.empty();
//        }
//    }
    


    public List<User> findAll() {
        String sql = """
            SELECT 
                u.id AS user_id, 
                u.username, 
                u.password, 
                u.user_ativo, 
                c.id AS cliente_id, 
                c.nome, 
                c.cpf, 
                c.cliente_ativo,
                r.name AS role_name,
                e.id AS endereco_id, 
                e.cep, 
                e.cidade, 
                e.estado, 
                e.rua, 
                e.numero, 
                e.bairro, 
                e.complemento
            FROM users u
            JOIN clientes c ON c.user_id = u.id
            LEFT JOIN user_roles ur ON u.id = ur.user_id
            LEFT JOIN roles r ON r.id = ur.role_id
            LEFT JOIN enderecos e ON e.cliente_id = c.id
        """;

        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT u.id AS user_id, u.username, u.password, u.user_ativo, " +
            "c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo, " +
            "e.id AS endereco_id, e.rua, e.numero, e.bairro, e.cidade, e.estado, e.complemento, e.cep, " +
            "r.name AS role_name " +
            "FROM users u " +
            "JOIN clientes c ON c.user_id = u.id " +
            "LEFT JOIN enderecos e ON e.cliente_id = c.id " +
            "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
            "LEFT JOIN roles r ON r.id = ur.role_id " +
            "WHERE u.id = ?";

        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id);

        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
    
    public Cliente findByIdWithContas(Long clienteId) {
        // Busca o cliente com seus dados (user e endereço)
        String clienteSql = """
            SELECT 
                c.id AS cliente_id, 
                c.nome AS cliente_nome, 
                c.cpf AS cliente_cpf, 
                c.cliente_ativo,
                u.id AS user_id, 
                u.username AS user_username, 
                u.password AS user_password, 
                u.user_ativo AS user_ativo,
                e.id AS endereco_id, 
                e.cep, 
                e.cidade, 
                e.estado, 
                e.rua, 
                e.numero, 
                e.bairro, 
                e.complemento
            FROM clientes c
            JOIN users u ON c.user_id = u.id
            LEFT JOIN enderecos e ON e.cliente_id = c.id
            WHERE c.id = ?
        """;

        Cliente cliente = jdbcTemplate.queryForObject(clienteSql, new ClienteRowMapper(), clienteId);

        // Busca as contas do cliente
        String contasSql = """
            SELECT 
                c.*,
                cl.id AS cliente_id,
                cl.nome AS cliente_nome
            FROM contas c
            JOIN clientes cl ON c.cliente_id = cl.id
            WHERE c.cliente_id = ?
        """;

        List<Conta> contas = jdbcTemplate.query(contasSql, new ContasRowMapper(), clienteId);
        cliente.setContas(contas);

        return cliente;
    }
    
    public Optional<User> findByCpf(Long cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        String sql = "SELECT u.id AS user_id, u.username, u.password, u.user_ativo, " +
            "c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo, " +
            "e.id AS endereco_id, e.rua, e.numero, e.bairro, e.cidade, e.estado, e.complemento, e.cep, " +
            "r.name AS role_name " +
            "FROM users u " +
            "JOIN clientes c ON c.user_id = u.id " +
            "LEFT JOIN enderecos e ON e.cliente_id = c.id " +
            "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
            "LEFT JOIN roles r ON r.id = ur.role_id " +
            "WHERE c.cpf = ?";

        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), cpf);

        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
    
    
    public boolean desativarCliente(Long clienteId) {
        Integer userId = jdbcTemplate.queryForObject(
            "SELECT user_id FROM clientes WHERE id = ?", 
            Integer.class, 
            clienteId
        );
        
        if (userId == null) {
            throw new IllegalArgumentException("Cliente não encontrado com ID: " + clienteId);
        }

        // Desativando seguros
        jdbcTemplate.update(
            "UPDATE seguros SET ativo = false " +
            "WHERE cartao_id IN (SELECT id FROM cartoes WHERE conta_id IN (SELECT id FROM contas WHERE cliente_id = ?))", 
            clienteId
        );

        // Desativando os cartões do cliente
        jdbcTemplate.update(
            "UPDATE cartoes SET status = false " +
            "WHERE conta_id IN (SELECT id FROM contas WHERE cliente_id = ?)", 
            clienteId
        );

        // Desativando as contas 
        jdbcTemplate.update(
            "UPDATE contas SET status = false WHERE cliente_id = ?", 
            clienteId
        );

        // Desativando o cliente
        int clientesAtualizados = jdbcTemplate.update(
            "UPDATE clientes SET cliente_ativo = false WHERE id = ?", 
            clienteId
        );

        // Desativando usuário
        int usersAtualizados = jdbcTemplate.update(
            "UPDATE users SET user_ativo = false WHERE id = ?", 
            userId
        );

        return clientesAtualizados > 0 && usersAtualizados > 0;
    }

        
    public boolean existeCliente(Long id) {
        String sql = "SELECT COUNT(1) FROM clientes WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
    
    public boolean ativarCliente(Long clienteId) {
        Integer userId = jdbcTemplate.queryForObject(
            "SELECT user_id FROM clientes WHERE id = ?", 
            Integer.class, 
            clienteId
        );
        
        if (userId == null) {
            throw new IllegalArgumentException("Cliente não encontrado com ID: " + clienteId);
        }
     
        int clientesAtualizados = jdbcTemplate.update(
            "UPDATE clientes SET cliente_ativo = true WHERE id = ?", 
            clienteId
        );

        int usersAtualizados = jdbcTemplate.update(
            "UPDATE users SET user_ativo = true WHERE id = ?", 
            userId
        );

        return clientesAtualizados > 0 && usersAtualizados > 0;
    }

//    public boolean delete(Long id) {
//        String sql = "DELETE FROM users WHERE id = ?";
//        int rowsAffected = jdbcTemplate.update(sql, id);
//        return rowsAffected > 0;
//    }
}
