package br.com.marcielli.bancom.dao;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
import br.com.marcielli.bancom.mappers.TransferenciaRowMapper;

@Component
public class TransferenciaDao {
    
    private final JdbcTemplate jdbcTemplate;

    public TransferenciaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long save(Transferencia transferencia) {
        String sql = """
            INSERT INTO transferencias 
            (id_cliente_origem, id_cliente_destino, id_conta_origem,
             id_conta_destino, tipo_transferencia, valor, data,
             codigo_operacao, tipo_cartao, id_cartao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id""";
        
        return jdbcTemplate.queryForObject(sql, Long.class,
            transferencia.getIdClienteOrigem(),
            transferencia.getIdClienteDestino(),
            transferencia.getIdContaOrigem(),
            transferencia.getIdContaDestino(),
            transferencia.getTipoTransferencia().name(),
            transferencia.getValor(),
            Timestamp.valueOf(transferencia.getData()),
            transferencia.getCodigoOperacao(),
            transferencia.getTipoCartao() != null ? transferencia.getTipoCartao().name() : null,
            transferencia.getIdCartao());
    }
    
    public List<Transferencia> findByCartaoId(Long cartaoId) {
        String sql = "SELECT * FROM transferencias WHERE id_cartao = ? " +
                   "AND tipo_transferencia = 'CARTAO_CREDITO' " +
                   "ORDER BY data DESC";
        return jdbcTemplate.query(sql, new TransferenciaRowMapper(), cartaoId);
    }
    
    public void associarTransferenciaAFatura(Long faturaId, Long transferenciaId) {
        String sql = "INSERT INTO fatura_transferencias (fatura_id, transferencia_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, faturaId, transferenciaId);
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
    
    public List<Transferencia> findTransferenciasEnviadasByContaId(Long contaId) {
        String sql = "SELECT * FROM transferencias WHERE id_conta_origem = ? ORDER BY data DESC";
        return jdbcTemplate.query(sql, new TransferenciaRowMapper(), contaId);
    }
}