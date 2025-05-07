package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;


@Component
public class ClienteDao {

    private final JdbcTemplate jdbcTemplate;
    private final ContasRowMapper contasRowMapper;
    
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    public ClienteDao(JdbcTemplate jdbcTemplate, ContasRowMapper contasRowMapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.contasRowMapper = contasRowMapper;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
    
    public Cliente findByIdWithContasAndTransferencias(Long clienteId) {
    	String sql = """
    		    SELECT
    		        c.id AS cliente_id,
    		        c.nome,
    		        c.cpf,
    		        c.cliente_ativo,
    		        co.id AS conta_id,
    		        co.numero_conta,
    		        co.tipo_conta,
    		        co.saldo_conta,
    		        t.id AS transferencia_id,
    		        t.valor,
    		        t.data,
    		        t.id_cliente_origem,
    		        t.id_cliente_destino,
    		        t.id_conta_origem,
    		        t.id_conta_destino,
    		        t.id_cartao,
    		        t.fatura_id,
    		        t.tipo_transferencia,
    		        t.codigo_operacao,
    		        t.tipo_cartao,
    		        ca.id AS cartao_id,               -- ID do cartão
    		        ca.numero_cartao,                 -- Número do cartão
    		        ca.tipo_cartao,                   -- Tipo do cartão
    		        ca.categoria_conta,               -- Categoria da conta do cartão
    		        ca.limite_credito_pre_aprovado,   -- Limite de crédito pré-aprovado
    		        ca.status,                         -- Status do cartão
    		        ca.total_gasto_mes,               -- Total gasto no mês
    		        ca.total_gasto_mes_credito        -- Total gasto no crédito
    		    FROM clientes c
    		    LEFT JOIN contas co ON co.cliente_id = c.id
    		    LEFT JOIN transferencias t ON t.id_conta_origem = co.id OR t.id_conta_destino = co.id
    		    LEFT JOIN cartoes ca ON t.id_cartao = ca.id   -- Junção com a tabela de cartões
    		    WHERE c.id = :clienteId
    		""";


        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("clienteId", clienteId);

        Map<Long, Cliente> clienteMap = new HashMap<>();
        Map<Long, Conta> contaMap = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, params, rs -> {
            try {
                Long clienteIdRs = rs.getLong("cliente_id");
                Cliente cliente = clienteMap.computeIfAbsent(clienteIdRs, id -> {
                    Cliente c = new Cliente();
                    c.setId(id);
                    try {
                        c.setNome(rs.getString("nome"));
                        String cpfStr = rs.getString("cpf");
                        if (cpfStr != null) {
                            c.setCpf(Long.valueOf(cpfStr));
                        }
                        c.setClienteAtivo(rs.getBoolean("cliente_ativo"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e); 
                    }
                    return c;
                });

                //Conta
                Long contaId = rs.getLong("conta_id");
                if (rs.wasNull()) {
                    contaId = null;
                }

                if (contaId != null) {
                    Conta conta = contaMap.computeIfAbsent(contaId, id -> {
                        Conta co = new Conta();
                        co.setId(id);
                        try {
                            co.setNumeroConta(rs.getString("numero_conta"));
                            co.setTipoConta(TipoConta.valueOf(rs.getString("tipo_conta")));
                            co.setSaldoConta(rs.getBigDecimal("saldo_conta"));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        cliente.getContas().add(co);
                        return co;
                    });
                    
                    
                    //Cartão
                    
                    Long cartaoId = rs.getLong("cartao_id");
                    if (!rs.wasNull()) {
                        String tipoCartaoStr = rs.getString("tipo_cartao");
                        TipoCartao tipoCartao = TipoCartao.valueOf(tipoCartaoStr);
                        Cartao cartao;

                        if (tipoCartao == TipoCartao.CREDITO) {
                            CartaoCredito cartaoCredito = new CartaoCredito();
                            cartaoCredito.setId(cartaoId);
                            cartaoCredito.setNumeroCartao(rs.getString("numero_cartao"));
                            cartaoCredito.setTipoCartao(tipoCartao);
                            cartaoCredito.setCategoriaConta(CategoriaConta.valueOf(rs.getString("categoria_conta")));
                            cartaoCredito.setStatus(Boolean.valueOf(rs.getString("status")));
                            cartaoCredito.setLimiteCreditoPreAprovado(rs.getBigDecimal("limite_credito_pre_aprovado"));
                            cartaoCredito.setTotalGastoMesCredito(rs.getBigDecimal("total_gasto_mes_credito"));
                            cartao = cartaoCredito;
                        } else if (tipoCartao == TipoCartao.DEBITO) {
                            CartaoDebito cartaoDebito = new CartaoDebito();
                            cartaoDebito.setId(cartaoId);
                            cartaoDebito.setNumeroCartao(rs.getString("numero_cartao"));
                            cartaoDebito.setTipoCartao(tipoCartao);
                            cartaoDebito.setCategoriaConta(CategoriaConta.valueOf(rs.getString("categoria_conta")));
                            cartaoDebito.setStatus(Boolean.valueOf(rs.getString("status")));
                            cartaoDebito.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));
                            cartao = cartaoDebito;
                        } else {
                            throw new IllegalArgumentException("Tipo de cartão desconhecido: " + tipoCartaoStr);
                        }

                        if (conta.getCartoes() == null) {
                            conta.setCartoes(new ArrayList<Cartao>());
                        }
                        conta.getCartoes().add(cartao); //associando o cartão a conta
                       
                    
                    
                    
                    
                    //Transferencia

                    Long transferenciaId = rs.getLong("transferencia_id");
                    if (rs.wasNull()) {
                        transferenciaId = null; 
                    }

                    if (transferenciaId != null) {
                        Transferencia transferencia = new Transferencia();
                        transferencia.setId(transferenciaId);
                        try {
                            transferencia.setValor(rs.getBigDecimal("valor"));
                            Timestamp timestamp = rs.getTimestamp("data");
                            if (timestamp != null) {
                                transferencia.setData(timestamp.toLocalDateTime());
                            }

                            // Preencher os campos restantes
                            transferencia.setIdClienteOrigem(rs.getLong("id_cliente_origem"));
                            transferencia.setIdClienteDestino(rs.getLong("id_cliente_destino"));
                            transferencia.setIdContaOrigem(rs.getLong("id_conta_origem"));
                            transferencia.setIdContaDestino(rs.getLong("id_conta_destino"));
                            transferencia.setIdCartao(rs.getLong("id_cartao"));
                            transferencia.setFaturaId(rs.getLong("fatura_id"));
                            transferencia.setTipoTransferencia(TipoTransferencia.valueOf(rs.getString("tipo_transferencia")));
                            transferencia.setCodigoOperacao(rs.getString("codigo_operacao"));
                            transferencia.setTipoCartao(TipoCartao.valueOf(rs.getString("tipo_cartao")));                            
                            
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        if (conta.getTransferencias() == null) {
                            conta.setTransferencias(new ArrayList<>());
                        }

                        conta.getTransferencias().add(transferencia);
                    }
                }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        return clienteMap.values().stream().findFirst().orElse(null);
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