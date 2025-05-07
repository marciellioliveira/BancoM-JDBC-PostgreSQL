package br.com.marcielli.bancom.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.bancom.entity.Fatura;

@Component
public class FaturaDao {
	
	 private final JdbcTemplate jdbcTemplate;
	 private static final Logger logger = LoggerFactory.getLogger(CartaoDao.class);

	    public FaturaDao(JdbcTemplate jdbcTemplate) {
	        this.jdbcTemplate = jdbcTemplate;
	    }
	    
	
	    public Long save(Fatura fatura) {
	        String sql = """
	            INSERT INTO faturas (cartao_id, data_vencimento)
	            VALUES (?, ?)
	            RETURNING id
	        """;

	        try {
	            Long faturaId = jdbcTemplate.queryForObject(sql, Long.class, 
	                fatura.getCartao().getId(), 
	                fatura.getDataVencimento());
	            logger.error("Erro ao salvar fatura -Fatura ID: " + faturaId);
	            logger.error("Erro ao salvar fatura - Cartao ID: " + fatura.getCartao().getId());
	            logger.error("Erro ao salvar fatura - Data Vencimento: " + fatura.getDataVencimento());
	            if (faturaId == null) {
	                throw new IllegalStateException("Falha ao obter ID gerado para fatura");
	            }

	            logger.info("Fatura salva com ID: " + faturaId);

	            // Atualiza o objeto Fatura com o ID (se quiser)
	            fatura.setId(faturaId);

	            return faturaId;
	        } catch (Exception e) {
	            logger.error("Erro ao salvar fatura: " + e.getMessage(), e);
	            throw e; // Deixa a exceção subir para o Spring fazer rollback
	        }
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
