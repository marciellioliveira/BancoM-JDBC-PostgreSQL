package br.com.marcielli.bancom.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.mappers.TransferenciaRowMapper;

@Component
public class TransferenciaDao {
	
	 private final JdbcTemplate jdbcTemplate;

	    public TransferenciaDao(JdbcTemplate jdbcTemplate) {
	        this.jdbcTemplate = jdbcTemplate;
	    }

	    public void save(Transferencia transferencia) {
	        String sql = "INSERT INTO transferencias " +
	                    "(id_cliente_origem, id_cliente_destino, id_conta_origem, id_conta_destino, " +
	                    "tipo_transferencia, valor, data, codigo_operacao, tipo_cartao) " +
	                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	        
	        jdbcTemplate.update(sql,
	            transferencia.getIdClienteOrigem(),
	            transferencia.getIdClienteDestino(),
	            transferencia.getIdContaOrigem(),
	            transferencia.getIdContaDestino(),
	            transferencia.getTipoTransferencia().name(),
	            transferencia.getValor(),
	            transferencia.getData(),
	            transferencia.getCodigoOperacao(),
	            transferencia.getTipoCartao() != null ? transferencia.getTipoCartao().name() : null
	        );
	    }
	    
	    // Buscar todas as transferências
	    public List<Transferencia> findAll() {
	        String sql = "SELECT * FROM transferencias";
	        return jdbcTemplate.query(sql, new TransferenciaRowMapper());
	    }

	    // Buscar uma transferência por ID
	    public Transferencia findById(Long id) {
	        String sql = "SELECT * FROM transferencias WHERE id = ?";
	        return jdbcTemplate.queryForObject(sql, new TransferenciaRowMapper(), id);
	    }
}
