package br.com.marcielli.bancom.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.PagamentoFatura;

@Component
public class PagamentoFaturaDao {

private final JdbcTemplate jdbcTemplate;
    
    public PagamentoFaturaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public void salvar(PagamentoFatura pagamento) {
        String sql = """
            INSERT INTO pagamentos_fatura 
            (fatura_id, cartao_id, conta_id, valor_pago, data_pagamento) 
            VALUES (?, ?, ?, ?, ?)
            """;
            
        jdbcTemplate.update(sql,
            pagamento.getFaturaId(),
            pagamento.getCartaoId(),
            pagamento.getContaId(),
            pagamento.getValorPago(),
            pagamento.getDataPagamento());
    }

}
