package br.com.marcielli.bancom.repository;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.repository.mappers.ClienteRowMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("cliente")
@Repository
public class ClienteRepositoryJDBC {

    private final JdbcTemplate jdbcTemplate;

    public ClienteRepositoryJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Cliente> findByCpf(Long cpf) {
        String sql = "SELECT id, nome, cpf, cliente_ativo FROM clientes WHERE cpf = ?";

        List<Cliente> clientes = jdbcTemplate.query(sql, new ClienteRowMapper(), cpf);
        return clientes.stream().findFirst(); // pega o primeiro se existir, senão retorna Optional.empty()
    }


    public List<Cliente> findByNomeContainingIgnoreCase(String nome) {
        String sql = "SELECT id, nome, cpf, cliente_ativo FROM clientes WHERE nome ILIKE ?";
        return jdbcTemplate.query(sql, new ClienteRowMapper(), "%" + nome + "%");
    }

    public void save(Cliente cliente) {
        String sql = "INSERT INTO clientes (nome, cpf, cliente_ativo, user_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, cliente.getNome(), cliente.getCpf(), cliente.isClienteAtivo(), cliente.getUser().getId());
    }

    public Optional<Cliente> findById(Long id) {
        String sql = "SELECT " +
                "c.id AS cliente_id, c.nome AS cliente_nome, c.cpf AS cliente_cpf, c.cliente_ativo, " +
                "u.id AS user_id, u.username AS user_username, u.password AS user_password, u.user_ativo AS user_ativo, " +
                "e.id AS endereco_id, e.cep, e.cidade, e.estado, e.rua, e.numero, e.bairro, e.complemento " +
                "FROM clientes c " +
                "LEFT JOIN users u ON c.user_id = u.id " +
                "LEFT JOIN enderecos e ON c.endereco_id = e.id " +
                "WHERE c.user_id = ?";

        List<Cliente> clientes = jdbcTemplate.query(sql, new ClienteRowMapper(), id);
        return clientes.stream().findFirst();
    }

}
