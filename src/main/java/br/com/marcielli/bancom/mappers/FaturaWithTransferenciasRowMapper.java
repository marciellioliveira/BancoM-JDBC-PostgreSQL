package br.com.marcielli.bancom.mappers;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.dao.TransferenciaDao;
import br.com.marcielli.bancom.entity.Fatura;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoTransferencia;

@Component
public class FaturaWithTransferenciasRowMapper implements RowMapper<Fatura> {

    private final Logger logger = LoggerFactory.getLogger(FaturaWithTransferenciasRowMapper.class);

    @Override
    public Fatura mapRow(ResultSet rs, int rowNum) throws SQLException {
        Fatura fatura = new Fatura();
        
        Long idFatura = rs.getLong("fatura_id");
        Long idCartao = rs.getLong("fatura_cartao_id");
        
        fatura.setId(idFatura);
        fatura.setCartaoId(idCartao);
        fatura.setValorTotal(rs.getBigDecimal("valor_total"));
        fatura.setDataVencimento(rs.getTimestamp("data_vencimento").toLocalDateTime());
        fatura.setStatus(rs.getBoolean("fatura_status"));

        List<Transferencia> transferencias = new ArrayList<Transferencia>();

        do {
            Long transferenciaId = rs.getLong("transferencia_id");
            if (transferenciaId != 0) {
                Transferencia transferencia = new Transferencia();
                transferencia.setId(transferenciaId);
                transferencia.setValor(rs.getBigDecimal("transferencia_valor"));
                Timestamp ts = rs.getTimestamp("transferencia_data");
                if (ts != null) {
                    transferencia.setData(ts.toLocalDateTime());
                }
                transferencia.setFaturaId(idFatura);
                transferencia.setIdCartao(idCartao);
                transferencia.setIdClienteOrigem(rs.getLong("transferencia_cliente_origem_id"));
                transferencia.setIdClienteDestino(rs.getLong("transferencia_cliente_destino_id"));
                transferencia.setIdContaOrigem(rs.getLong("transferencia_conta_origem_id"));
                transferencia.setIdContaDestino(rs.getLong("transferencia_conta_destino_id"));
                transferencia.setTipoTransferencia(TipoTransferencia.valueOf(rs.getString("transferencia_tipo_transferencia")));
                transferencia.setCodigoOperacao(rs.getString("transferencia_cod_operacao"));
                transferencia.setTipoCartao(TipoCartao.valueOf(rs.getString("transferencia_tipo_cartao")));
                transferencias.add(transferencia);
            }
        } while (rs.next());

        fatura.setTransferenciasCredito(transferencias);

        logger.info("Fatura ID {} mapeada com {} transferências", fatura.getId(), transferencias.size());

        return fatura;
    }
}

//@Component
//public class FaturaWithTransferenciasRowMapper implements RowMapper<Fatura> {
//    
//    private final TransferenciaDao transferenciaDao;
//    private final Logger logger = LoggerFactory.getLogger(FaturaWithTransferenciasRowMapper.class);
//
//    public FaturaWithTransferenciasRowMapper(TransferenciaDao transferenciaDao) {
//        this.transferenciaDao = transferenciaDao;
//    }
//
//    @Override
//    public Fatura mapRow(ResultSet rs, int rowNum) throws SQLException {
//        Fatura fatura = new Fatura();
//        fatura.setId(rs.getLong("id"));
//        fatura.setCartaoId(rs.getLong("cartao_id"));
//        fatura.setValorTotal(rs.getBigDecimal("valor_total"));
//        fatura.setDataVencimento(rs.getTimestamp("data_vencimento").toLocalDateTime());
//        
//        Long faturaId = fatura.getId();
//        logger.info("Buscando transferências de crédito para fatura ID: {}", faturaId);
//
//        List<Transferencia> transferencias = transferenciaDao.findCreditoByFaturaIdUsingJoin(faturaId);
//        fatura.setTransferenciasCredito(transferencias);  
//
//        logger.info("Transferências de crédito carregadas para fatura ID {}: {}", faturaId, transferencias.size());
//        
//        boolean status = (transferencias == null || transferencias.isEmpty()) &&
//                (fatura.getValorTotal() == null || fatura.getValorTotal().compareTo(BigDecimal.ZERO) <= 0);
//        fatura.setStatus(status);
//
//        return fatura;
//    }
//}