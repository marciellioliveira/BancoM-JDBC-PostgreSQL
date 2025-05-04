package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
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

    public ClienteDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        // Busca o cliente
        String clienteSql = "SELECT * FROM clientes WHERE id = ?";
        Cliente cliente = jdbcTemplate.queryForObject(
            clienteSql, 
            new BeanPropertyRowMapper<>(Cliente.class), 
            id
        );

        if (cliente != null) {
            // Busca as contas associadas a esse cliente
            String contasSql = "SELECT * FROM contas WHERE cliente_id = ?";
            List<Conta> contas = jdbcTemplate.query(
                contasSql,
                new BeanPropertyRowMapper<>(Conta.class),
                id
            );
            
            //  Associa as contas ao cliente
            cliente.setContas(contas);
        }

        return cliente;
    }
      
    
    public Cliente findByIdWithUser(Long id) {
        String sql = "SELECT c.*, u.id as user_id, u.username FROM clientes c " +
                     "LEFT JOIN users u ON c.user_id = u.id WHERE c.id = ?";
        
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Cliente cliente = new Cliente();
            
            // Tratamento infalível:
            try {
                Integer userId = rs.getObject("user_id", Integer.class);
                if (userId != null) {
                    User user = new User();
                    user.setId(userId); 
                    user.setUsername(rs.getString("username"));
                    cliente.setUser(user);
                }
            } catch (SQLException e) {
                throw new DataAccessException("Erro ao ler user_id", e) {};
            }
            
            return cliente;
        }, id);
    }
    
    public Cliente findByIdWithContasAndTransferencias(Long id) {
     
        String clienteSql = "SELECT * FROM clientes WHERE id = ?";
        Cliente cliente = jdbcTemplate.queryForObject(
            clienteSql, 
            new BeanPropertyRowMapper<>(Cliente.class), 
            id
        );

        if (cliente != null) {
         
            ContasRowMapper contasRowMapper = new ContasRowMapper();
            ContaWithTransferenciasRowMapper contaWithTransferenciasMapper = 
                new ContaWithTransferenciasRowMapper(contasRowMapper, this);            
           
            String contasSql = "SELECT c.*, cl.nome AS cliente_nome FROM contas c " +
                              "JOIN clientes cl ON c.cliente_id = cl.id " +
                              "WHERE c.cliente_id = ?";
            
            List<Conta> contas = jdbcTemplate.query(
                contasSql,
                contaWithTransferenciasMapper,
                id
            );
            
            cliente.setContas(contas);
        }

        return cliente;
    }

    // Método auxiliar para buscar transferências por conta
    public List<Transferencia> findByContaId(Long contaId) {
        String sql = "SELECT * FROM transferencias " +
                    "WHERE id_conta_origem = ? OR id_conta_destino = ? " +
                    "ORDER BY data DESC";
        
        return jdbcTemplate.query(
            sql,
            new TransferenciaRowMapper(),
            contaId, contaId
        );
    }
    
    
    
    
    


}