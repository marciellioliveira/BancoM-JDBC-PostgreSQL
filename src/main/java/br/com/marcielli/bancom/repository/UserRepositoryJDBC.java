package br.com.marcielli.bancom.repository;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.repository.mappers.UserRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryJDBC {

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User save(User user) {
        // 1. Inserir usuário (com abordagem específica para PostgreSQL)
        String sqlUser = "INSERT INTO users (username, password, user_ativo) VALUES (?, ?, ?) RETURNING id";

        Long userId = jdbcTemplate.queryForObject(
                sqlUser,
                Long.class,
                user.getUsername(),
                user.getPassword(),
                user.isUserAtivo()
        );

        user.setId(Math.toIntExact(userId));

        // 2. Inserir roles
        for (Role role : user.getRoles()) {
            jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                    userId,
                    role.getId()
            );
        }

        // 3. Inserir cliente (com RETURNING para PostgreSQL)
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

        // 4. Inserir endereço
        Endereco endereco = cliente.getEndereco();
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

        return user;
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.id AS user_id, u.username, u.password, u.user_ativo, " +
                "c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo, " +
                "e.id AS endereco_id, e.rua, e.numero, e.bairro, e.cidade, e.estado, e.complemento, e.cep, " +
                "r.id AS role_id, r.name AS role_name " +
                "FROM users u " +
                "JOIN clientes c ON c.user_id = u.id " +
                "LEFT JOIN enderecos e ON e.cliente_id = c.id " +
                "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
                "LEFT JOIN roles r ON r.id = ur.role_id " +
                "WHERE u.username = ?";

        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), username);

        if (users.isEmpty()) {
            return Optional.empty();  // Caso não haja usuário, retorna Optional vazio.
        }

        return Optional.of(users.getFirst());  // Retorna o primeiro usuário encontrado na lista
    }




    public List<User> findAll() {
        String sql = "SELECT u.id AS user_id, u.username, u.password, u.user_ativo, " +
                "c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo, " +
                "e.id AS endereco_id, e.rua, e.numero, e.bairro, e.cidade, e.estado, e.complemento, e.cep, " +
                "r.id AS role_id, r.name AS role_name " +
                "FROM users u " +
                "JOIN clientes c ON c.user_id = u.id " +
                "LEFT JOIN enderecos e ON e.cliente_id = c.id " +
                "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
                "LEFT JOIN roles r ON r.id = ur.role_id";

        // Executa a consulta e mapeia os resultados para uma lista de usuários
        return jdbcTemplate.query(sql, new UserRowMapper());
    }




}
