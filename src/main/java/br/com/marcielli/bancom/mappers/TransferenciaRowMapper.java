package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;

import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoTransferencia;

public class TransferenciaRowMapper implements RowMapper<Transferencia> {

	@Override
    public Transferencia mapRow(ResultSet rs, int rowNum) throws SQLException {
        Transferencia transferencia = new Transferencia();
        transferencia.setId(rs.getLong("id"));
        transferencia.setIdClienteOrigem(rs.getLong("id_cliente_origem"));
        transferencia.setIdClienteDestino(rs.getLong("id_cliente_destino"));
        transferencia.setIdContaOrigem(rs.getLong("id_conta_origem"));
        transferencia.setIdContaDestino(rs.getLong("id_conta_destino"));
        transferencia.setTipoTransferencia(TipoTransferencia.valueOf(rs.getString("tipo_transferencia")));
        transferencia.setValor(rs.getBigDecimal("valor"));
        
        // Convertendo Timestamp para LocalDateTime
        Timestamp timestamp = rs.getTimestamp("data");
        transferencia.setData(timestamp != null ? timestamp.toLocalDateTime() : null);
        
        transferencia.setCodigoOperacao(rs.getString("codigo_operacao"));
        
        String tipoCartaoStr = rs.getString("tipo_cartao");
        if (tipoCartaoStr != null) {
            transferencia.setTipoCartao(TipoCartao.valueOf(tipoCartaoStr));
        }
        
        return transferencia;
    }

}
