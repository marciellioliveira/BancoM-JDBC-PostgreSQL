package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.dao.ClienteDao;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Transferencia;

@Component
public class ContaWithTransferenciasRowMapper implements RowMapper<Conta>{

	private final ContasRowMapper contaRowMapper;
    private final ClienteDao clientedao;
    private static final Logger logger = LoggerFactory.getLogger(ContaWithTransferenciasRowMapper.class);

    public ContaWithTransferenciasRowMapper(ContasRowMapper contaRowMapper, ClienteDao clientedao) {
        this.contaRowMapper = contaRowMapper;
        this.clientedao = clientedao;
    }

    @Override
    public Conta mapRow(ResultSet rs, int rowNum) throws SQLException {
        Conta conta = contaRowMapper.mapRow(rs, rowNum);
        Long contaId = rs.getLong("id");
        logger.debug("[DEBUG] Mapeando conta ID: " + contaId);

        // Carregar transferências enviadas
        List<Transferencia> transferenciasEnviadas = clientedao.findByContaId(contaId);
        logger.debug("[DEBUG] Número de transferências enviadas encontradas: " + transferenciasEnviadas.size());
        conta.setTransferencias(transferenciasEnviadas);   

        // Carregar cartões para a conta
        List<Cartao> cartoes = clientedao.findCartoesByContaId(contaId);
        conta.setCartoes(cartoes);
        logger.debug("[DEBUG] Cartões carregados para a conta ID: " + contaId);

        // Carregar transferências de crédito
        List<Transferencia> transferenciasCredito = new ArrayList<Transferencia>();
        for (Cartao cartao : cartoes) {
            if ("CREDITO".equalsIgnoreCase(cartao.getTipoCartao().name())) {
                List<Transferencia> cartaoTransferencias = clientedao.findTransferenciasCreditoByCartaoId(cartao.getId());
                transferenciasCredito.addAll(cartaoTransferencias);
                logger.debug("[DEBUG] Transferências de crédito carregadas para cartão ID: " + cartao.getId() + ", total: " + cartaoTransferencias.size());
            }
        }
        conta.setTransferencias(transferenciasCredito);
        logger.debug("[DEBUG] Total de transferências de crédito para conta ID: " + contaId + ": " + transferenciasCredito.size());

        return conta;
    }
		
	
}
