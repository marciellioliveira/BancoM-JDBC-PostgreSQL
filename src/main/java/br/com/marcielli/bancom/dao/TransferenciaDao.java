package br.com.marcielli.bancom.dao;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Transferencia;

@Component
public class TransferenciaDao {
	
	 private final JdbcTemplate jdbcTemplate;

	    public TransferenciaDao(JdbcTemplate jdbcTemplate) {
	        this.jdbcTemplate = jdbcTemplate;
	    }

	    public void save(Transferencia transferencia) {
	        String sql = "INSERT INTO transferencias " +
	                     "(id_conta_origem, id_conta_destino, valor, tipo_transferencia, data) " +
	                     "VALUES (?, ?, ?, ?, ?)";
	        
	        jdbcTemplate.update(sql,
	            transferencia.getIdContaOrigem(),
	            transferencia.getIdContaDestino(),
	            transferencia.getValor(),
	            transferencia.getTipoTransferencia(),
	            LocalDateTime.now()
	        );
	    }
	    
}
