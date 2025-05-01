package br.com.marcielli.bancom.mappers;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContasRowMapper implements RowMapper<Conta> {


    @Override
    public Conta mapRow(ResultSet rs, int rowNum) throws SQLException {
        String tipoConta = rs.getString("tipo_conta");

        Conta conta;

        if ("CORRENTE".equalsIgnoreCase(tipoConta)) {
            ContaCorrente cc = new ContaCorrente();
            cc.setTaxaManutencaoMensal(rs.getBigDecimal("taxa_manutencao_mensal"));
            conta = cc;
        } else if ("POUPANCA".equalsIgnoreCase(tipoConta)) {
            ContaPoupanca cp = new ContaPoupanca();
            cp.setTaxaAcrescRend(rs.getBigDecimal("taxa_acresc_rend"));
            cp.setTaxaMensal(rs.getBigDecimal("taxa_mensal"));
            conta = cp;
        } else {
            conta = new Conta(); // caso genérico, se for um novo tipo no futuro
        }

        conta.setId(rs.getLong("id"));
        conta.setNumeroConta(rs.getString("numero_conta"));
        conta.setPixAleatorio(rs.getString("pix_aleatorio"));
        conta.setSaldoConta(rs.getBigDecimal("saldo_conta"));
        conta.setStatus(rs.getBoolean("status"));

        // Categoria e cliente (simples)
        conta.setCategoriaConta(CategoriaConta.valueOf(rs.getString("categoria_conta")));
        conta.setTipoConta(TipoConta.valueOf(tipoConta));

        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("cliente_id"));
        conta.setCliente(cliente);

        return conta;
    }
}
