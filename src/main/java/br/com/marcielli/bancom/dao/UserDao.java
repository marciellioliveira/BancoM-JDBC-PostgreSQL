package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.mappers.ClienteRowMapper;
import br.com.marcielli.bancom.mappers.ContasRowMapper;
import br.com.marcielli.bancom.mappers.UserRowMapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RoleDao roleDao;

    public UserDao(JdbcTemplate jdbcTemplate, RoleDao roleDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleDao = roleDao;
    }

    public User save(User user) {
        // Insere usuário
        String sqlUser = "INSERT INTO users (username, password, user_ativo) VALUES (?, ?, ?) RETURNING id";
        Long userId = jdbcTemplate.queryForObject(
            sqlUser,
            Long.class,
            user.getUsername(),
            user.getPassword(),
            user.isUserAtivo()
        );
        user.setId(Math.toIntExact(userId));

        // Insere role
        Role role = roleDao.findByName(user.getRole());
        if (role == null) {
            throw new RuntimeException("Role não encontrada: " + user.getRole());
        }
        
        jdbcTemplate.update(
            "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
            userId,
            role.getId()
        );

        // Insere cliente
        Cliente cliente = user.getCliente();
        String sqlCliente = """
            INSERT INTO clientes (nome, cpf, cliente_ativo, user_id)
            VALUES (?, ?, ?, ?)
            RETURNING id
        """;
        Long clienteId = jdbcTemplate.queryForObject(
            sqlCliente,
            Long.class,
            cliente.getNome(),
            cliente.getCpf(),
            cliente.isClienteAtivo(),
            userId
        );
        cliente.setId(clienteId);

        // Insere endereço (se existir)
        Endereco endereco = cliente.getEndereco();
        if (endereco != null) {
            jdbcTemplate.update(
                "INSERT INTO enderecos (rua, numero, bairro, cidade, estado, complemento, cep, cliente_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getComplemento(),
                endereco.getCep(),
                clienteId
            );
        }

        return user;
    }
    
    public User update(User user) {
        // Atualiza o usuário
        String sqlUser = "UPDATE users SET username = ?, user_ativo = ? WHERE id = ?";
        jdbcTemplate.update(
            sqlUser,
            user.getUsername(),
            user.isUserAtivo(),
            user.getId()
        );

        // Atualiza os dados do cliente
        Cliente cliente = user.getCliente();
        String sqlCliente = "UPDATE clientes SET nome = ?, cliente_ativo = ? WHERE id = ?";
        jdbcTemplate.update(
            sqlCliente,
            cliente.getNome(),
            cliente.isClienteAtivo(),
            cliente.getId()
        );

        // Atualiza o endereço do cliente
        Endereco endereco = cliente.getEndereco();
        String sqlEndereco = "UPDATE enderecos SET rua = ?, numero = ?, bairro = ?, cidade = ?, estado = ?, complemento = ?, cep = ? WHERE cliente_id = ?";
        jdbcTemplate.update(
            sqlEndereco,
            endereco.getRua(),
            endereco.getNumero(),
            endereco.getBairro(),
            endereco.getCidade(),
            endereco.getEstado(),
            endereco.getComplemento(),
            endereco.getCep(),
            cliente.getId()
        );

        return user;
    }
    
    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT u.id AS user_id, u.username, u.password, u.user_ativo, 
                   c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo,
                   r.name AS role_name
            FROM users u
            JOIN user_roles ur ON u.id = ur.user_id
            JOIN roles r ON r.id = ur.role_id
            JOIN clientes c ON c.user_id = u.id
            WHERE u.username = ?""";

        try {
            User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setUserAtivo(rs.getBoolean("user_ativo"));
                u.setRole(rs.getString("role_name"));
                
                Cliente cliente = new Cliente();
                cliente.setId(rs.getLong("cliente_id")); // Usando getLong diretamente
                
                // Conversão segura de String para Long no CPF
                String cpfStr = rs.getString("cpf");
                if (cpfStr != null) {
                    try {
                        cliente.setCpf(Long.parseLong(cpfStr.replaceAll("\\D", "")));
                    } catch (NumberFormatException e) {
                        cliente.setCpf(0L); // Ou outro valor padrão
                    }
                }
                
                cliente.setNome(rs.getString("nome"));
                cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));
                u.setCliente(cliente);
                
                return u;
            }, username);
            
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    


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
        // Cliente existe?
        Integer userId = jdbcTemplate.queryForObject(
            "SELECT user_id FROM clientes WHERE id = ?", 
            Integer.class, 
            clienteId
        );
        
        if (userId == null) {
            throw new IllegalArgumentException("Cliente não encontrado com ID: " + clienteId);
        }

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
    
//    public boolean desativarCliente(Long clienteId) {
//       
//    	Integer userId = jdbcTemplate.queryForObject(
//    	        "SELECT user_id FROM clientes WHERE id = ?", 
//    	        Integer.class, 
//    	        clienteId
//    	    );
//    	    
//    	    if (userId == null) {
//    	        throw new IllegalArgumentException("Cliente não encontrado com ID: " + clienteId);
//    	    }
//
//    	    // atualizando cliente para false
//    	    int clientesAtualizados = jdbcTemplate.update(
//    	        "UPDATE clientes SET cliente_ativo = false WHERE id = ?", 
//    	        clienteId
//    	    );
//
//    	    // atualizando user para false
//    	    int usersAtualizados = jdbcTemplate.update(
//    	        "UPDATE users SET user_ativo = false WHERE id = ?", 
//    	        userId
//    	    );
//
//    	    //se os dois ficaram false, retorna true
//    	    return clientesAtualizados > 0 && usersAtualizados > 0;
//    }
    
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
