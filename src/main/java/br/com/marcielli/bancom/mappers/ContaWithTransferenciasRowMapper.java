package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import br.com.marcielli.bancom.dao.ClienteDao;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Transferencia;

public class ContaWithTransferenciasRowMapper implements RowMapper<Conta>{

	private final ContasRowMapper contaRowMapper;
    private final ClienteDao clientedao;
    private static final Logger logger = LoggerFactory.getLogger(Conta.class);

    public ContaWithTransferenciasRowMapper(ContasRowMapper contaRowMapper, ClienteDao clientedao) {
        this.contaRowMapper = contaRowMapper;
        this.clientedao = clientedao;
    }

    @Override
    public Conta mapRow(ResultSet rs, int rowNum) throws SQLException {
      
    	Conta conta = contaRowMapper.mapRow(rs, rowNum);
        Long contaId = rs.getLong("id");
        logger.debug("[DEBUG] Mapeando conta ID: "+ contaId);
        
        List<Transferencia> transferencias = clientedao.findByContaId(contaId);
        logger.debug("[DEBUG]  Número de transferências encontradas: " + transferencias.size());
        
        conta.setTransferencias(transferencias);
        logger.debug("[DEBUG]   Transferências na conta após set: " + conta.getTransferencias().size());
        
        return conta;
    }
		
	
}
