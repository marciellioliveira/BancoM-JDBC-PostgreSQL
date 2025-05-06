package br.com.marcielli.bancom.dao;

import java.math.BigDecimal;

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
	            fatura.getValor_total(),
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

}
