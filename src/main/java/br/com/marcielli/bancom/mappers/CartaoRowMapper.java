package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Fatura;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;

@Component
public class CartaoRowMapper implements RowMapper<Cartao> {

    private static final Logger logger = LoggerFactory.getLogger(CartaoRowMapper.class);    


	@Override
    public Cartao mapRow(ResultSet rs, int rowNum) throws SQLException {
        String tipoCartao = rs.getString("tipo_cartao");

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
        cartao.setFaturaId(rs.getLong("fatura_id"));
        cartao.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));

        
        Long faturaId = rs.getObject("fatura_id", Long.class);
        if (faturaId != null && faturaId != 0) {
            Fatura fatura = new Fatura();
            fatura.setId(faturaId);
            fatura.setCartaoId(faturaId);
            fatura.setValorTotal(rs.getBigDecimal("fatura_valor_total"));
            fatura.setDataVencimento(rs.getObject("fatura_data_vencimento", LocalDateTime.class));
            cartao.setFatura(fatura);
            logger.debug("[DEBUG] Fatura mapeada para cartão ID: " + cartao.getId() + ", fatura ID: " + faturaId);
        }

        
        Long contaId = rs.getObject("conta_id", Long.class);
        if (contaId != null) {
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
        conta.setTipoConta(parseEnum(TipoConta.class, rs.getString("tipo_conta")));
        conta.setCategoriaConta(parseEnum(CategoriaConta.class, rs.getString("categoria_conta")));
        conta.setStatus(rs.getBoolean("status"));      
        conta.setSaldoConta(rs.getBigDecimal("saldo_conta"));
        
        Cliente cliente = new Cliente();
        Long clienteId = rs.getLong("cliente_id");
        cliente.setId(clienteId);
        cliente.setNome(rs.getString("cliente_nome"));
        cliente.setCpf(rs.getLong("cliente_cpf"));
        cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));
        //cliente.setUserId(rs.getLong("cliente_user_id"));
        
        conta.setCliente(cliente);
        
        logger.debug("Mapeando conta ID: {} com cliente ID: {}", contaId, clienteId);
        logger.debug("Dados do cliente: Nome={}, CPF={}", cliente.getNome(), cliente.getCpf());
        
        return conta;
    }

}