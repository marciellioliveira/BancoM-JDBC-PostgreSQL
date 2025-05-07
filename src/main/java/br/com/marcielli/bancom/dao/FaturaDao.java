package br.com.marcielli.bancom.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Fatura;

@Component
public class FaturaDao {
	
	 private final JdbcTemplate jdbcTemplate;

	    public FaturaDao(JdbcTemplate jdbcTemplate) {
	        this.jdbcTemplate = jdbcTemplate;
	    }
	    
	    public Long save(Fatura fatura) {
	        String sql = """
	            INSERT INTO faturas (cartao_id, data_vencimento)
	            VALUES (?, ?)
	            RETURNING id
	            """;
	        return jdbcTemplate.queryForObject(sql, Long.class, 
	            fatura.getCartao().getId(), 
	            fatura.getDataVencimento());
	    }

	    public void update(Fatura fatura) {
	        String sql = """
	            UPDATE faturas 
	            SET valor_total = ?,
	                data_vencimento = ?
	            WHERE id = ?
	            """;
	        jdbcTemplate.update(sql, 
	            fatura.getValorTotal(),
	            fatura.getDataVencimento(),
	            fatura.getId());
	    }
	    
	    public void atualizarTotalFatura(Long faturaId, BigDecimal valor) {
	        String sql = """
	            UPDATE faturas
	            SET valor_total = COALESCE(valor_total, 0) + ?
	            WHERE id = ?
	            """;
	        jdbcTemplate.update(sql, valor, faturaId);
	    }

	    public Optional<Fatura> findByCartaoId(Long cartaoId) {
	        String sql = """
	            SELECT f.id, f.cartao_id, f.valor_total, f.data_vencimento
	            FROM faturas f
	            WHERE f.cartao_id = ?
	            """;

	        List<Fatura> faturas = jdbcTemplate.query(sql, new Object[]{cartaoId}, (rs, rowNum) -> {
	            Fatura fatura = new Fatura();
	            fatura.setId(rs.getLong("id"));
	            fatura.setValorTotal(rs.getBigDecimal("valor_total"));
	            fatura.setDataVencimento(rs.getTimestamp("data_vencimento").toLocalDateTime());

	            // SE QUISER POSSO CARREGAR CARTÃO AQUI TIPO:
	            // fatura.setCartao(new CartaoCredito()); ou CartaoDebito

	            return fatura;
	        });

	        if (faturas.isEmpty()) {
	            return Optional.empty();
	        } else {
	            return Optional.of(faturas.get(0));
	        }
	    }

}
