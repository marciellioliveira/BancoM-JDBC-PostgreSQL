package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;

@Component
public class ContaCorrenteRowMapper implements RowMapper<ContaCorrente> {

	@Override
	public ContaCorrente mapRow(ResultSet rs, int rowNum) throws SQLException {

		ContaCorrente cc = new ContaCorrente();

		cc.setId(rs.getLong("id"));
		cc.setTipoConta(TipoConta.valueOf(rs.getString("tipo_conta")));
		cc.setCategoriaConta(CategoriaConta.valueOf(rs.getString("categoria_conta")));
		cc.setSaldoConta(rs.getBigDecimal("saldo_conta"));
		cc.setNumeroConta(rs.getString("numero_conta"));
		cc.setPixAleatorio(rs.getString("pix_aleatorio"));
		cc.setStatus(rs.getBoolean("status"));

		cc.setTaxaManutencaoMensal(rs.getBigDecimal("taxa_manutencao_mensal"));

		Cliente cliente = new Cliente();
		cliente.setId(rs.getLong("cliente_id"));
		cliente.setNome(rs.getString("cliente_nome"));
		cc.setCliente(cliente);

		return cc;
	}

}
