package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.mappers.CartaoRowMapper;
import br.com.marcielli.bancom.mappers.ClienteRowMapper;
import br.com.marcielli.bancom.mappers.ContaWithTransferenciasRowMapper;
import br.com.marcielli.bancom.mappers.ContasRowMapper;
import br.com.marcielli.bancom.mappers.TransferenciaRowMapper;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class ClienteDao {

    private final JdbcTemplate jdbcTemplate;
    private final ContasRowMapper contasRowMapper;
    
    public ClienteDao(JdbcTemplate jdbcTemplate, ContasRowMapper contasRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.contasRowMapper = contasRowMapper;
    }

    public Optional<Cliente> findByCpf(Long cpf) {
        String sql = "SELECT c.id AS cliente_id, c.nome AS cliente_nome, c.cpf AS cliente_cpf, c.cliente_ativo, c.user_id, " +
            "u.id AS user_id, u.username AS user_username, u.password AS user_password, u.user_ativo " +
            "FROM clientes c " +
            "JOIN users u ON u.id = c.user_id " +
            "WHERE c.cpf = ?";

        List<Cliente> clientes = jdbcTemplate.query(sql, new ClienteRowMapper(), cpf);
        return clientes.stream().findFirst();
    }


    public boolean cpfExists(Long cpf) {
        String sql = "SELECT 1 FROM clientes WHERE cpf = ? LIMIT 1";
        List<Integer> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), cpf);
        return !result.isEmpty();
    }

    public List<Cliente> findByNomeContainingIgnoreCase(String nome) {
        String sql = "SELECT id, nome, cpf, cliente_ativo FROM clientes WHERE nome ILIKE ?";
        return jdbcTemplate.query(sql, new ClienteRowMapper(), "%" + nome + "%");
    }

    public void save(Cliente cliente) {
        if (cliente.getCpf() != null && cpfExists(cliente.getCpf())) {
            throw new ClienteEncontradoException("Já existe um cliente com esse CPF: " + cliente.getCpf());
        }

        String sql = "INSERT INTO clientes (nome, cpf, cliente_ativo, user_id) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql,
                cliente.getNome(),
                cliente.getCpf(),
                cliente.isClienteAtivo(),
                cliente.getUser().getId()
            );
        } catch (DuplicateKeyException e) {
            throw new ClienteEncontradoException("Já existe um cliente com esse CPF: " + cliente.getCpf() + " - Exception:" + e);
        } catch (DataAccessException e) {
            throw new ClienteEncontradoException("Erro ao salvar cliente no banco de dados - Exception:" + e);
        }
    }

    public Optional<Cliente> findById(Long id) {
        String sql = "SELECT " +
                "c.id AS cliente_id, " +
                "c.nome AS cliente_nome, " +
                "c.cpf AS cliente_cpf, " +
                "c.cliente_ativo, " +
                "c.user_id, " +
                "u.username AS user_username, " +
                "u.password AS user_password, " +
                "u.user_ativo AS user_ativo, " +
                "e.id AS endereco_id, " +
                "e.cep, " +
                "e.cidade, " +
                "e.estado, " +
                "e.rua, " +
                "e.numero, " +
                "e.bairro, " +
                "e.complemento " +
                "FROM clientes c " +
                "JOIN users u ON c.user_id = u.id " +
                "LEFT JOIN enderecos e ON e.cliente_id = c.id " +
                "WHERE c.id = ?";

        List<Cliente> clientes = jdbcTemplate.query(sql, new ClienteRowMapper(), id);
        return clientes.isEmpty() ? Optional.empty() : Optional.of(clientes.get(0));
    }
    
    public Cliente findByIdWithContas(Long id) {
        String clienteSql = "SELECT * FROM clientes WHERE id = ?";
        Cliente cliente = jdbcTemplate.queryForObject(
            clienteSql,
            new BeanPropertyRowMapper<>(Cliente.class),
            id
        );

        if (cliente != null) {
            String contasSql = "SELECT * FROM contas WHERE cliente_id = ?";
            List<Conta> contas = jdbcTemplate.query(
                contasSql,
                new BeanPropertyRowMapper<>(Conta.class),
                id
            );
            cliente.setContas(contas);
        }

        return cliente;
    }
      
    
    public Cliente findByIdWithUser(Long id) {
        String sql = "SELECT c.*, u.id as user_id, u.username FROM clientes c " +
                     "LEFT JOIN users u ON c.user_id = u.id WHERE c.id = ?";
        
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Cliente cliente = new Cliente();
            try {
                Integer userId = rs.getObject("user_id", Integer.class);
                if (userId != null) {
                    User user = new User();
                    user.setId(userId);
                    user.setUsername(rs.getString("username"));
                    cliente.setUser(user);
                }
            } catch (SQLException e) {
                throw new DataAccessException("Erro ao ler user_id", e) {
					private static final long serialVersionUID = 1L;};
            }
            return cliente;
        }, id);
    }
    
    public Cliente findByIdWithContasAndTransferencias(Long id) {
        String clienteSql = "SELECT * FROM clientes WHERE id = ?";
        Cliente cliente = jdbcTemplate.queryForObject(clienteSql, new BeanPropertyRowMapper<>(Cliente.class), id);

        if (cliente != null) {
            String contasSql = """
                SELECT c.*, cl.nome AS cliente_nome 
                FROM contas c 
                JOIN clientes cl ON c.cliente_id = cl.id 
                WHERE c.cliente_id = ?""";
                
            List<Conta> contas = jdbcTemplate.query(
                contasSql,
                new ContaWithTransferenciasRowMapper(contasRowMapper, this),
                id
            );
            
            contas.forEach(conta -> {
                List<Cartao> cartoes = findCartoesByContaId(conta.getId());
                conta.setCartoes(cartoes);
            });
            
            cliente.setContas(contas);
        }

        return cliente;
    }

    public List<Transferencia> findByContaId(Long contaId) {
        String sql = "SELECT * FROM transferencias " +
                     "WHERE id_conta_origem = ? " +
                     "ORDER BY data DESC";
        
        return jdbcTemplate.query(
            sql,
            new TransferenciaRowMapper(),
            contaId
        );
    }
    
    public List<Transferencia> findTransferenciasCreditoByCartaoId(Long cartaoId) {
        String sql = """
            SELECT t.* FROM transferencias t
            JOIN fatura_transferencias ft ON t.id = ft.transferencia_id
            JOIN faturas f ON ft.fatura_id = f.id
            WHERE f.cartao_id = ? AND t.tipo_transferencia = 'CARTAO_CREDITO'
            ORDER BY t.data DESC""";
        
        return jdbcTemplate.query(sql, new TransferenciaRowMapper(), cartaoId);
    }

    public List<Cartao> findCartoesByContaId(Long contaId) {
        String cartaoSql = """
            SELECT 
                c.id, c.tipo_conta, c.categoria_conta, c.tipo_cartao, c.numero_cartao, 
                c.status, c.senha, c.conta_id, c.fatura_id, c.total_gasto_mes,
                c.limite_credito_pre_aprovado, c.taxa_utilizacao, c.taxa_seguro_viagem, 
                c.total_gasto_mes_credito, c.limite_diario_transacao,
                co.cliente_id, cl.nome AS cliente_nome, co.saldo_conta, co.status AS conta_status,
                co.tipo_conta AS conta_tipo_conta, co.categoria_conta AS conta_categoria_conta,
                f.id AS fatura_id, f.cartao_id AS fatura_cartao_id, f.valor_total AS fatura_valor_total,
                f.data_vencimento AS fatura_data_vencimento
            FROM cartoes c
            JOIN contas co ON c.conta_id = co.id
            JOIN clientes cl ON co.cliente_id = cl.id
            LEFT JOIN faturas f ON c.fatura_id = f.id
            WHERE c.conta_id = ?
        """;
        return jdbcTemplate.query(cartaoSql, new CartaoRowMapper(), contaId);
    }

    
    
    
    


}