package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.mappers.UserRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

        public User save(User user) {
        // Insere usuári
        String sqlUser = "INSERT INTO users (username, password, user_ativo) VALUES (?, ?, ?) RETURNING id";

        Long userId = jdbcTemplate.queryForObject(
                sqlUser,
                Long.class,
                user.getUsername(),
                user.getPassword(),
                user.isUserAtivo()
        );

        user.setId(Math.toIntExact(userId));

        // Insere os roles
        for (Role role : user.getRoles()) {
            jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                    userId,
                    role.getId()
            );
        }

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

        // Insere endereço
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

    public User update(User user) {
        // Atualiza o usuário
        String sqlUser = "UPDATE users SET username = ?, password = ?, user_ativo = ? WHERE id = ?";
        jdbcTemplate.update(
                sqlUser,
                user.getUsername(),
                user.getPassword(),
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

        // Atualiza os roles do usuário (caso haja alteração)
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            // Remove os roles antigos
            jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", user.getId());

            // Insere os novos roles
            for (Role role : user.getRoles()) {
                jdbcTemplate.update(
                        "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                        user.getId(),
                        role.getId()
                );
            }
        }

        return user;
    }


//    public User save(User user) {
//        // Insere usuári
//        String sqlUser = "INSERT INTO users (username, password, user_ativo) VALUES (?, ?, ?) RETURNING id";
//
//        Long userId = jdbcTemplate.queryForObject(
//                sqlUser,
//                Long.class,
//                user.getUsername(),
//                user.getPassword(),
//                user.isUserAtivo()
//        );
//
//        user.setId(Math.toIntExact(userId));
//
//        // Insere os roles
//        for (Role role : user.getRoles()) {
//            jdbcTemplate.update(
//                    "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
//                    userId,
//                    role.getId()
//            );
//        }
//
//        // Insere cliente
//        Cliente cliente = user.getCliente();
//        String sqlCliente = """
//        INSERT INTO clientes (nome, cpf, cliente_ativo, user_id)
//        VALUES (?, ?, ?, ?)
//        RETURNING id
//        """;
//
//        Long clienteId = jdbcTemplate.queryForObject(
//                sqlCliente,
//                Long.class,
//                cliente.getNome(),
//                cliente.getCpf(),
//                cliente.isClienteAtivo(),
//                userId
//        );
//        cliente.setId(clienteId);
//
//        // Insere endereço
//        Endereco endereco = cliente.getEndereco();
//        jdbcTemplate.update(
//                "INSERT INTO enderecos (rua, numero, bairro, cidade, estado, complemento, cep, cliente_id) " +
//                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
//                endereco.getRua(),
//                endereco.getNumero(),
//                endereco.getBairro(),
//                endereco.getCidade(),
//                endereco.getEstado(),
//                endereco.getComplemento(),
//                endereco.getCep(),
//                clienteId
//        );
//
//        return user;
//    }

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

        return jdbcTemplate.query(sql, new UserRowMapper()); //Executando a consulta e mapeando o resultado para uma lista
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT u.id AS user_id, u.username, u.password, u.user_ativo, " +
                "c.id AS cliente_id, c.nome, c.cpf, c.cliente_ativo, " +
                "e.id AS endereco_id, e.rua, e.numero, e.bairro, e.cidade, e.estado, e.complemento, e.cep, " +
                "r.id AS role_id, r.name AS role_name " +
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
                "r.id AS role_id, r.name AS role_name " +
                "FROM users u " +
                "JOIN clientes c ON c.user_id = u.id " +
                "LEFT JOIN enderecos e ON e.cliente_id = c.id " +
                "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
                "LEFT JOIN roles r ON r.id = ur.role_id " +
                "WHERE c.cpf = ?";

        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), cpf);

        if (users.isEmpty()) {
            return Optional.empty();  // Retorna vazio se não encontrar o usuário
        }

        return Optional.of(users.get(0));  // Retorna o primeiro usuário encontrado
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql, id);

        return rowsAffected > 0; // Retorna true se o usuário foi deletado, caso contrário retorna false
    }
}
