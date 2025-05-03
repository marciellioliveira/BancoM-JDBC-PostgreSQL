package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
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


//    public Optional<User> findByUsername(String username) {
//        String sql = "SELECT u.id AS user_id, u.username, u.password, u.user_ativo, " +
//            "c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo, " +
//            "e.id AS endereco_id, e.rua, e.numero, e.bairro, e.cidade, e.estado, e.complemento, e.cep, " +
//            "r.name AS role_name " +
//            "FROM users u " +
//            "JOIN clientes c ON c.user_id = u.id " +
//            "LEFT JOIN enderecos e ON e.cliente_id = c.id " +
//            "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
//            "LEFT JOIN roles r ON r.id = ur.role_id " +
//            "WHERE u.username = ?";
//
//        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), username);
//
//        return users.isEmpty() ? Optional.empty() : Optional.of(users.getFirst());
//    }
    
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
        String sql = "SELECT u.id AS user_id, u.username, u.password, u.user_ativo, " +
            "c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo, " +
            "e.id AS endereco_id, e.rua, e.numero, e.bairro, e.cidade, e.estado, e.complemento, e.cep, " +
            "r.name AS role_name " +
            "FROM users u " +
            "JOIN clientes c ON c.user_id = u.id " +
            "LEFT JOIN enderecos e ON e.cliente_id = c.id " +
            "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
            "LEFT JOIN roles r ON r.id = ur.role_id";

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

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }
}
