package br.com.marcielli.bancom.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Seguro;

@Component
public class SeguroDao {
	
	private final JdbcTemplate jdbcTemplate;

	public SeguroDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Seguro save(Seguro seguro) {
        String sql = "INSERT INTO seguros (tipo, valor_mensal, valor_apolice, ativo, cartao_id) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";

        Long id = jdbcTemplate.queryForObject(
            sql,
            new SingleColumnRowMapper<>(Long.class),
            seguro.getTipo().name(),
            seguro.getValorMensal(),
            seguro.getValorApolice(),
            seguro.getAtivo(),
            seguro.getCartao().getId()
        );

        seguro.setId(id);
        return seguro;
    }
	
    
}
