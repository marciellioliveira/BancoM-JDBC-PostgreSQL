package br.com.marcielli.bancom.mappers;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContasRowMapper implements RowMapper<Conta> {
	
	@Override
    public Conta mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        Long clienteId = rs.getLong("cliente_id");
        String clienteNome = rs.getString("cliente_nome");
        System.out.println("cliente_nome from ResultSet for conta id " + id + ": " + clienteNome); 
        if (clienteNome == null) {
            clienteNome = "Desconhecido";
        }
        TipoConta tipoConta = TipoConta.valueOf(rs.getString("tipo_conta"));
        CategoriaConta categoriaConta = CategoriaConta.valueOf(rs.getString("categoria_conta"));
        BigDecimal saldo = rs.getBigDecimal("saldo_conta");
        String numeroConta = rs.getString("numero_conta");
        String pixAleatorio = rs.getString("pix_aleatorio");
        boolean status = rs.getBoolean("status");

        if (tipoConta == TipoConta.CORRENTE) {
            BigDecimal taxaManutencao = rs.getBigDecimal("taxa_manutencao_mensal");
            ContaCorrente cc = new ContaCorrente();
            cc.setTaxaManutencaoMensal(taxaManutencao);
            return setCommonFields(cc, id, clienteId, clienteNome, tipoConta, categoriaConta, saldo, numeroConta, pixAleatorio, status);
        } else if (tipoConta == TipoConta.POUPANCA) {
            BigDecimal taxaAcresc = rs.getBigDecimal("taxa_acresc_rend");
            BigDecimal taxaMensal = rs.getBigDecimal("taxa_mensal");
            ContaPoupanca cp = new ContaPoupanca();
            cp.setTaxaAcrescRend(taxaAcresc);
            cp.setTaxaMensal(taxaMensal);
            return setCommonFields(cp, id, clienteId, clienteNome, tipoConta, categoriaConta, saldo, numeroConta, pixAleatorio, status);
        } else {
            return setCommonFields(new Conta(), id, clienteId, clienteNome, tipoConta, categoriaConta, saldo, numeroConta, pixAleatorio, status);
        }
    }

    private Conta setCommonFields(Conta conta, Long id, Long clienteId, String clienteNome, TipoConta tipoConta,
            CategoriaConta categoriaConta, BigDecimal saldo, String numeroConta, String pixAleatorio, boolean status) {
        conta.setId(id);
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        conta.setCliente(cliente);
        conta.setTipoConta(tipoConta);
        conta.setCategoriaConta(categoriaConta);
        conta.setSaldoConta(saldo);
        conta.setNumeroConta(numeroConta);
        conta.setPixAleatorio(pixAleatorio);
        conta.setStatus(status);
        conta.setClienteNome(clienteNome); 
        System.out.println("Setting clienteNome for conta id " + id + ": " + clienteNome); 
        return conta;
    }
}
