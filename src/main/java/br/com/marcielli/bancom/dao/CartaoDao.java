package br.com.marcielli.bancom.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
