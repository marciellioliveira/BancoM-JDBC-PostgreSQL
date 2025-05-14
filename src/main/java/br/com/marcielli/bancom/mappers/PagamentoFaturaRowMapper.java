package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import br.com.marcielli.bancom.entity.PagamentoFatura;

public class PagamentoFaturaRowMapper implements RowMapper<PagamentoFatura> {

	@Override
	public PagamentoFatura mapRow(ResultSet rs, int rowNum) throws SQLException {
		PagamentoFatura pagamento = new PagamentoFatura();
		pagamento.setId(rs.getLong("id"));
		pagamento.setFaturaId(rs.getLong("fatura_id"));
		pagamento.setCartaoId(rs.getLong("cartao_id"));
		pagamento.setContaId(rs.getLong("conta_id"));
		pagamento.setValorPago(rs.getBigDecimal("valor_pago"));
		pagamento.setDataPagamento(rs.getObject("data_pagamento", LocalDateTime.class));
		return pagamento;

	}
}
