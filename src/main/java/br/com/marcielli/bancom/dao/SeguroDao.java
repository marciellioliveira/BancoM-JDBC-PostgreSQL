package br.com.marcielli.bancom.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Seguro;
import br.com.marcielli.bancom.mappers.SeguroRowMapper;

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
	
	public List<Seguro> findAll() {
	    String sql = """
	        SELECT
	            s.id, s.tipo, s.valor_mensal, s.valor_apolice, s.ativo,
	            s.cartao_id,
	            c.numero_cartao, c.tipo_cartao,
	            ct.id AS conta_id, cl.id AS cliente_id, cl.nome AS cliente_nome, u.username
	        FROM seguros s
	        INNER JOIN cartoes c ON s.cartao_id = c.id
	        INNER JOIN contas ct ON c.conta_id = ct.id
	        INNER JOIN clientes cl ON ct.cliente_id = cl.id
	        INNER JOIN users u ON cl.user_id = u.id
	    """;
	    return jdbcTemplate.query(sql, new SeguroRowMapper());
	}



	public List<Seguro> findByUsername(String username) {
	    String sql = """
	       SELECT 
		    s.id, s.tipo, s.valor_mensal, s.valor_apolice, s.ativo,
		    s.cartao_id, 
		    c.id AS cartao_id, c.numero_cartao, c.tipo_cartao,
		    ct.id AS conta_id, cl.id AS cliente_id, cl.nome AS cliente_nome, u.username
		FROM seguros s
		INNER JOIN cartoes c ON s.cartao_id = c.id
		INNER JOIN contas ct ON c.conta_id = ct.id
		INNER JOIN clientes cl ON ct.cliente_id = cl.id
		INNER JOIN users u ON cl.user_id = u.id
		WHERE u.username = ?			    		
	    """;
	    return jdbcTemplate.query(sql, new SeguroRowMapper(), username);
	}

	
	public Optional<Seguro> findById(Long id) {
	    String sql = """
	        SELECT s.id, s.tipo, s.valor_mensal, s.valor_apolice, s.ativo,
	               c.id AS cartao_id, c.numero_cartao,
	               cl.nome AS cliente_nome
	        FROM seguros s
	        JOIN cartoes c ON s.cartao_id = c.id
	        JOIN contas co ON c.conta_id = co.id
	        JOIN clientes cl ON co.cliente_id = cl.id
	        WHERE s.id = ?
	    """;

	    try {
	        Seguro seguro = jdbcTemplate.queryForObject(sql, new SeguroRowMapper(), id);
	        return Optional.of(seguro);
	    } catch (EmptyResultDataAccessException e) {
	        return Optional.empty();
	    }
	}

	public Optional<Seguro> findByIdAndUsername(Long id, String username) {
	    String sql = """
	        SELECT s.id, s.tipo, s.valor_mensal, s.valor_apolice, s.ativo,
	               c.id AS cartao_id, c.numero_cartao,
	               cl.nome AS cliente_nome
	        FROM seguros s
	        JOIN cartoes c ON s.cartao_id = c.id
	        JOIN contas co ON c.conta_id = co.id
	        JOIN clientes cl ON co.cliente_id = cl.id
	        JOIN users u ON cl.user_id = u.id
	        WHERE s.id = ? AND u.username = ?
	    """;

	    try {
	        Seguro seguro = jdbcTemplate.queryForObject(sql, new SeguroRowMapper(), id, username);
	        return Optional.ofNullable(seguro);
	    } catch (EmptyResultDataAccessException e) {
	        return Optional.empty();
	    }
	}

	

	
    
}
