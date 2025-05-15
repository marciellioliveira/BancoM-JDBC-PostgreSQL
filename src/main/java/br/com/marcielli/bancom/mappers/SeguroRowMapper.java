package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;

import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Seguro;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoSeguro;


public class SeguroRowMapper implements RowMapper<Seguro> {

    @Override
    public Seguro mapRow(ResultSet rs, int rowNum) throws SQLException {
        Seguro seguro = new Seguro();
        seguro.setId(rs.getLong("id"));
        seguro.setTipo(TipoSeguro.valueOf(rs.getString("tipo")));
        seguro.setValorMensal(rs.getBigDecimal("valor_mensal"));
        seguro.setValorApolice(rs.getBigDecimal("valor_apolice"));
        seguro.setAtivo(rs.getBoolean("ativo"));

        Cartao cartao = new Cartao();
        cartao.setId(rs.getLong("cartao_id"));
        cartao.setNumeroCartao(rs.getString("numero_cartao"));

        Conta conta = new Conta();
        Cliente cliente = new Cliente();
        cliente.setNome(rs.getString("cliente_nome"));
        conta.setCliente(cliente);
        cartao.setConta(conta);

        seguro.setCartao(cartao);

        return seguro;
    }
}



