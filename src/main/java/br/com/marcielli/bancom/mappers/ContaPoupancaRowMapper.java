package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;

@Component
public class ContaPoupancaRowMapper implements RowMapper<ContaPoupanca> {

	@Override
    public ContaPoupanca mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContaPoupanca cp = new ContaPoupanca();

        cp.setId(rs.getLong("id"));
        cp.setTipoConta(TipoConta.valueOf(rs.getString("tipo_conta")));
        cp.setCategoriaConta(CategoriaConta.valueOf(rs.getString("categoria_conta")));
        cp.setSaldoConta(rs.getBigDecimal("saldo_conta"));
        cp.setNumeroConta(rs.getString("numero_conta"));
        cp.setPixAleatorio(rs.getString("pix_aleatorio"));
        cp.setStatus(rs.getBoolean("status"));

        // Campos específicos da ContaPoupanca
        cp.setTaxaAcrescRend(rs.getBigDecimal("taxa_acresc_rend"));
        cp.setTaxaMensal(rs.getBigDecimal("taxa_mensal"));

        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("cliente_id"));
        cliente.setNome(rs.getString("cliente_nome"));
        cp.setCliente(cliente);

        return cp;
    }

}
