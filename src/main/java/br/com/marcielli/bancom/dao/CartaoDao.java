package br.com.marcielli.bancom.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.mappers.CartaoRowMapper;

@Component
public class CartaoDao {

	private final JdbcTemplate jdbcTemplate;

    public CartaoDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public Cartao saveWithRelations(Cartao cartao) {
       
        String sqlCartao = """
            INSERT INTO cartoes 
            (tipo_conta, categoria_conta, tipo_cartao, numero_cartao, status, senha, conta_id,
             limite_credito_pre_aprovado, limite_diario_transacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        
        Long cartaoId = jdbcTemplate.queryForObject(sqlCartao, Long.class,
            cartao.getTipoConta().toString(),
            cartao.getCategoriaConta().toString(),
            cartao.getTipoCartao().toString(),
            cartao.getNumeroCartao(),
            cartao.isStatus(),
            cartao.getSenha(),
            cartao.getConta().getId(),
            cartao instanceof CartaoCredito ? ((CartaoCredito) cartao).getLimiteCreditoPreAprovado() : null,
            cartao instanceof CartaoDebito ? ((CartaoDebito) cartao).getLimiteDiarioTransacao() : null
        );
        
        cartao.setId(cartaoId);
        
        if(cartao.getFatura() != null) {
            String sqlFatura = """
                INSERT INTO faturas 
                (cartao_id, valor_total, data_vencimento)
                VALUES (?, ?, ?)
                """;
                
            jdbcTemplate.update(sqlFatura,
                cartaoId,
               // cartao.getFatura().getValorTotal(),
                cartao.getFatura().getDataVencimento());
        }
        
        if(cartao.getSeguros() != null && !cartao.getSeguros().isEmpty()) {
            String sqlSeguro = """
                INSERT INTO seguros 
                (cartao_id, tipo, valor_cobertura)
                VALUES (?, ?, ?)
                """;
                
            List<Object[]> batchArgs = cartao.getSeguros().stream()
                .map(s -> new Object[]{
                    cartaoId,
                    s.getTipo().toString(),
                   // s.getValorCobertura()
                })
                .collect(Collectors.toList());
                
            jdbcTemplate.batchUpdate(sqlSeguro, batchArgs);
        }
        
        return cartao;
    }
        
    public Optional<Cartao> findById(Long id) {
        String sql = "SELECT * FROM cartoes WHERE id = ?";
        List<Cartao> cartoes = jdbcTemplate.query(sql, new CartaoRowMapper(), id);
        return cartoes.isEmpty() ? Optional.empty() : Optional.of(cartoes.get(0));
    }

    public List<Cartao> findAll() {
        String sql = "SELECT * FROM cartoes";
        return jdbcTemplate.query(sql, new CartaoRowMapper());
    }

    
    public List<Cartao> findByUsername(String username) {
        String sql = """
            SELECT c.*
            FROM cartoes c
            INNER JOIN contas ct ON c.conta_id = ct.id
            WHERE ct.username = ?
        """;
        return jdbcTemplate.query(sql, new CartaoRowMapper(), username);
    }

    
    public Optional<Cartao> findByIdAndUsername(Long id, String username) {
        String sql = "SELECT c.* " +
                     "FROM cartoes c " +
                     "JOIN contas co ON c.conta_id = co.id " +
                     "JOIN clientes cl ON co.cliente_id = cl.id " +
                     "JOIN users u ON cl.user = u.id " +
                     "WHERE c.id = ? AND u.username = ?";

        try {
            Cartao cartao = jdbcTemplate.queryForObject(
                sql,
                new CartaoRowMapper(),
                id,
                username
            );
            return Optional.ofNullable(cartao);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
