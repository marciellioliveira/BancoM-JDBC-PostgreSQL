package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

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
        
		Cartao cartao = (rs.getString("tipo_cartao").equals("CREDITO")) 
            ? new CartaoCredito() 
            : new CartaoDebito();
        
        cartao.setId(rs.getLong("id"));
        cartao.setNumeroCartao(rs.getString("numero_cartao"));
        cartao.setSenha(rs.getString("senha"));
        cartao.setStatus(rs.getBoolean("status"));
        cartao.setCategoriaConta(CategoriaConta.valueOf(rs.getString("categoria_conta")));
        cartao.setTipoConta(TipoConta.valueOf(rs.getString("tipo_conta")));
        cartao.setTipoCartao(TipoCartao.valueOf(rs.getString("tipo_cartao")));
        
        Conta conta = new Conta();
        conta.setId(rs.getLong("conta_id"));
        cartao.setConta(conta);
        
        if(cartao instanceof CartaoCredito cc) {
            cc.setLimiteCreditoPreAprovado(rs.getBigDecimal("limite_credito"));
        } 
        
        else if(cartao instanceof CartaoDebito cd) {
            cd.setLimiteDiarioTransacao(rs.getBigDecimal("limite_diario"));
        }
        
        return cartao;
    }
}
