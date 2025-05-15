package br.com.marcielli.bancom.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import br.com.marcielli.bancom.entity.Fatura;

@Component
public class FaturaDao {
	
	 private final JdbcTemplate jdbcTemplate;
	 private static final Logger logger = LoggerFactory.getLogger(FaturaDao.class);

	    public FaturaDao(JdbcTemplate jdbcTemplate) {
	        this.jdbcTemplate = jdbcTemplate;
	    }
	

	    public Long save(Fatura fatura) {
	        if (fatura.getCartao().getId() == null) {
	        	 logger.info("Fatura get cartao id : " + fatura.getCartao().getId());
	            throw new IllegalArgumentException("O cartão não pode ser nulo ao salvar a fatura.");
	        }

	        String sql = """
	            INSERT INTO faturas (cartao_id, data_vencimento, valor_total, status)
	            VALUES (?, ?, ?, ?)
	            RETURNING id
	        """;

	        try {
	            LocalDateTime localDateTime = fatura.getDataVencimento();
	            Timestamp dataVencimentoSql = Timestamp.valueOf(localDateTime); 

	            Long faturaId = jdbcTemplate.queryForObject(sql, Long.class, 
	                fatura.getCartao().getId(), 
	                dataVencimentoSql,  
	                fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO,
	                fatura.isStatus());
	            	

	            logger.info("Fatura salva com ID: " + faturaId);

	            if (faturaId == null) {
	                throw new IllegalStateException("Falha ao obter ID gerado para fatura");
	            }

	            fatura.setId(faturaId);
	            return faturaId;
	        } catch (Exception e) {
	            logger.error("Erro ao salvar fatura: " + e.getMessage(), e);
	            throw e;
	        }
	    }


	    
	    public void update(Fatura fatura) {
	        String sql = """
	            UPDATE faturas 
	            SET valor_total = ?,
	                data_vencimento = ?,
	                status = ?
	            WHERE id = ?
	            """;
	        jdbcTemplate.update(sql, 
	            fatura.getValorTotal(),
	            fatura.getDataVencimento(),
	            fatura.isStatus(),
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
	            SELECT f.id, f.cartao_id, f.valor_total, f.data_vencimento, f.status
	            FROM faturas f
	            WHERE f.cartao_id = ?
	            """;

	        List<Fatura> faturas = jdbcTemplate.query(sql, (rs, rowNum) -> {
	            Fatura fatura = new Fatura();
	            fatura.setId(rs.getLong("id"));
	            fatura.setValorTotal(rs.getBigDecimal("valor_total"));
	            fatura.setDataVencimento(rs.getTimestamp("data_vencimento").toLocalDateTime());
	            fatura.setStatus(rs.getBoolean("status"));
	            return fatura;
	        }, cartaoId); 


	        if (faturas.isEmpty()) {
	            return Optional.empty();
	        } else {
	            return Optional.of(faturas.get(0));
	        }
	    }
	    
	    public void removerVinculoFaturaTransferencia(Long faturaId, Long transferenciaId) {
	        String sql = """
	            DELETE FROM fatura_transferencias 
	            WHERE fatura_id = ? AND transferencia_id = ?
	        """;
	        jdbcTemplate.update(sql, faturaId, transferenciaId);
	    }


}
