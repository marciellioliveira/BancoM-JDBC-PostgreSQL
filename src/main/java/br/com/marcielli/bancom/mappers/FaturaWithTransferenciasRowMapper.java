package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.dao.TransferenciaDao;
import br.com.marcielli.bancom.entity.Fatura;
import br.com.marcielli.bancom.entity.Transferencia;

@Component
public class FaturaWithTransferenciasRowMapper implements RowMapper<Fatura> {
    
    private final TransferenciaDao transferenciaDao;
    private final Logger logger = LoggerFactory.getLogger(FaturaWithTransferenciasRowMapper.class);

    public FaturaWithTransferenciasRowMapper(TransferenciaDao transferenciaDao) {
        this.transferenciaDao = transferenciaDao;
    }

    @Override
    public Fatura mapRow(ResultSet rs, int rowNum) throws SQLException {
        Fatura fatura = new Fatura();
        fatura.setId(rs.getLong("id"));
        fatura.setCartaoId(rs.getLong("cartao_id"));
        fatura.setValorTotal(rs.getBigDecimal("valor_total"));
        fatura.setDataVencimento(rs.getTimestamp("data_vencimento").toLocalDateTime());

        Long faturaId = fatura.getId();
        logger.info("Buscando transferências de crédito para fatura ID: {}", faturaId);

        List<Transferencia> transferencias = transferenciaDao.findCreditoByFaturaIdUsingJoin(faturaId);
        fatura.setTransferenciasCredito(transferencias);  

        logger.info("Transferências de crédito carregadas para fatura ID {}: {}", faturaId, transferencias.size());

        return fatura;
    }
}