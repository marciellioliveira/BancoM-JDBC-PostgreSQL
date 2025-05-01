package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class ContaDao {

    private final JdbcTemplate jdbcTemplate;

    public ContaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }



    public Conta save(Conta conta) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = """
            INSERT INTO contas (
                cliente_id, tipo_conta, categoria_conta, saldo_conta,
                numero_conta, pix_aleatorio, status,
                taxa_manutencao_mensal, taxa_acresc_rend, taxa_mensal
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(connection -> {


            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setLong(1, conta.getCliente().getId());
            ps.setString(2, conta.getTipoConta().name());
            ps.setString(3, conta.getCategoriaConta().name());
            ps.setBigDecimal(4, conta.getSaldoConta());
            ps.setString(5, conta.getNumeroConta());
            ps.setString(6, conta.getPixAleatorio());
            ps.setBoolean(7, conta.getStatus());

            switch (conta) {
                case ContaCorrente cc -> {
                    ps.setBigDecimal(8, cc.getTaxaManutencaoMensal());
                    ps.setNull(9, Types.NUMERIC);
                    ps.setNull(10, Types.NUMERIC);
                }
                case ContaPoupanca cp -> {
                    ps.setNull(8, Types.NUMERIC);
                    ps.setBigDecimal(9, cp.getTaxaAcrescRend());
                    ps.setBigDecimal(10, cp.getTaxaMensal());
                }
                default -> {
                    ps.setNull(8, Types.NUMERIC);
                    ps.setNull(9, Types.NUMERIC);
                    ps.setNull(10, Types.NUMERIC);
                }
            }

            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            conta.setId(generatedId.longValue());
        }

        return conta;
    }






















}
