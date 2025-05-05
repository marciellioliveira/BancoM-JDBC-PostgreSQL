package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;

public class CartaoRowMapper implements RowMapper<Cartao> {
	
	@Override
    public Cartao mapRow(ResultSet rs, int rowNum) throws SQLException {
        String tipoCartao = rs.getString("tipo_cartao");

        Cartao cartao;
        if ("CREDITO".equalsIgnoreCase(tipoCartao)) {
            cartao = new CartaoCredito();
            ((CartaoCredito) cartao).setLimiteCreditoPreAprovado(rs.getBigDecimal("limite_credito_pre_aprovado"));
        } else if ("DEBITO".equalsIgnoreCase(tipoCartao)) {
            cartao = new CartaoDebito();
            ((CartaoDebito) cartao).setLimiteDiarioTransacao(rs.getBigDecimal("limite_diario_transacao"));
        } else {
            cartao = new Cartao(); 
        }

        cartao.setId(rs.getLong("id"));
        cartao.setTipoConta(Enum.valueOf(TipoConta.class, rs.getString("tipo_conta")));
        cartao.setCategoriaConta(Enum.valueOf(CategoriaConta.class, rs.getString("categoria_conta")));
        cartao.setTipoCartao(Enum.valueOf(TipoCartao.class, rs.getString("tipo_cartao")));
        cartao.setNumeroCartao(rs.getString("numero_cartao"));
        cartao.setStatus(rs.getBoolean("status"));
        cartao.setSenha(rs.getString("senha"));

        Conta conta = new Conta();
        conta.setId(rs.getLong("conta_id"));
        cartao.setConta(conta);

        return cartao;
    }
}
