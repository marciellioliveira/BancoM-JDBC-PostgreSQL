package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.service.UserClienteService;

public class CartaoRowMapper implements RowMapper<Cartao> {

    private static final Logger logger = LoggerFactory.getLogger(UserClienteService.class);

    @Override
    public Cartao mapRow(ResultSet rs, int rowNum) throws SQLException {
        String tipoCartao = rs.getString("tipo_cartao");

        // Criação do objeto Cartao dependendo do tipo
        Cartao cartao;
        if ("CREDITO".equalsIgnoreCase(tipoCartao)) {
            cartao = new CartaoCredito();
            mapCartaoCredito((CartaoCredito) cartao, rs);
        } else if ("DEBITO".equalsIgnoreCase(tipoCartao)) {
            cartao = new CartaoDebito();
            mapCartaoDebito((CartaoDebito) cartao, rs);
        } else {
            cartao = new Cartao();
        }

        cartao.setId(rs.getLong("id"));
        cartao.setTipoConta(parseEnum(TipoConta.class, rs.getString("tipo_conta")));
        cartao.setCategoriaConta(parseEnum(CategoriaConta.class, rs.getString("categoria_conta")));
        cartao.setTipoCartao(parseEnum(TipoCartao.class, rs.getString("tipo_cartao")));
        cartao.setNumeroCartao(rs.getString("numero_cartao"));
        cartao.setStatus(rs.getBoolean("status"));
        cartao.setSenha(rs.getString("senha"));

        long contaId = rs.getLong("conta_id");
        if (!rs.wasNull()) {
            Conta conta = mapConta(rs, contaId);
            cartao.setConta(conta);
        } else {
            logger.error("Cartão ID={} não tem conta_id associado!", rs.getLong("id"));
            throw new IllegalStateException("Cartão não possui conta vinculada.");
        }

        return cartao;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        try {
            return value != null ? Enum.valueOf(enumClass, value) : null;
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao mapear enum {} com valor '{}'", enumClass.getSimpleName(), value);
            return null;
        }
    }

    private void mapCartaoCredito(CartaoCredito cartao, ResultSet rs) throws SQLException {
        cartao.setLimiteCreditoPreAprovado(rs.getBigDecimal("limite_credito_pre_aprovado"));
        cartao.setTaxaUtilizacao(rs.getBigDecimal("taxa_utilizacao"));
        cartao.setTaxaSeguroViagem(rs.getBigDecimal("taxa_seguro_viagem"));
        cartao.setTotalGastoMesCredito(rs.getBigDecimal("total_gasto_mes_credito"));
    }

    private void mapCartaoDebito(CartaoDebito cartao, ResultSet rs) throws SQLException {
        cartao.setLimiteDiarioTransacao(rs.getBigDecimal("limite_diario_transacao"));
        cartao.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));
    }

    private Conta mapConta(ResultSet rs, long contaId) throws SQLException {
        Conta conta = new Conta();
        conta.setId(contaId);
        conta.setTipoConta(parseEnum(TipoConta.class, rs.getString("conta_tipo_conta")));
        conta.setCategoriaConta(parseEnum(CategoriaConta.class, rs.getString("conta_categoria_conta")));
        conta.setStatus(rs.getBoolean("conta_status"));
        conta.setSaldoConta(rs.getBigDecimal("conta_saldo"));
        return conta;
    }
}