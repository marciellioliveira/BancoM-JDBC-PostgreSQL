package br.com.marcielli.bancom.mappers;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ContasRowMapper implements RowMapper<Conta> {
	
	private static final Logger logger = LoggerFactory.getLogger(ContasRowMapper.class);
	
	 @Override
	    public Conta mapRow(ResultSet rs, int rowNum) throws SQLException {
	        Long id = rs.getLong("id");
	        Long clienteId = rs.getLong("cliente_id");
	        String clienteNome = rs.getString("cliente_nome");
	        TipoConta tipoConta = TipoConta.valueOf(rs.getString("tipo_conta"));
	        CategoriaConta categoriaConta = CategoriaConta.valueOf(rs.getString("categoria_conta"));
	        BigDecimal saldo = rs.getBigDecimal("saldo_conta");
	        String numeroConta = rs.getString("numero_conta");
	        String pixAleatorio = rs.getString("pix_aleatorio");
	        boolean status = rs.getBoolean("status");
	        logger.debug("Mapeando conta ID: {} - Status: {}", id, status);
	        User user = new User();
	        
	        try {
	            if (rs.getObject("user_id") != null) {
	               
	                user.setId(rs.getInt("user_id"));
	                user.setUsername(rs.getString("username"));  
	             //   cliente.setUser(user);
	            }
	        } catch (SQLException e) {
	            logger.warn("Coluna user_id não encontrada nos resultados");
	        }

	        Conta conta;
	        
	        if (tipoConta == TipoConta.CORRENTE) {
	            ContaCorrente cc = new ContaCorrente();
	            cc.setTaxaManutencaoMensal(rs.getBigDecimal("taxa_manutencao_mensal"));
	            conta = cc;
	        } else if (tipoConta == TipoConta.POUPANCA) {
	            ContaPoupanca cp = new ContaPoupanca();
	            cp.setTaxaAcrescRend(rs.getBigDecimal("taxa_acresc_rend"));
	            cp.setTaxaMensal(rs.getBigDecimal("taxa_mensal"));
	            conta = cp;
	        } else {
	            conta = new Conta();
	        }
	        
	        return setCommonFields(conta, id, clienteId, clienteNome,user, tipoConta, 
	                             categoriaConta, saldo, numeroConta, pixAleatorio, status);
	    }

	    private Conta setCommonFields(Conta conta, Long id, Long clienteId, String clienteNome, User user,
	                                TipoConta tipoConta, CategoriaConta categoriaConta, 
	                                BigDecimal saldo, String numeroConta, 
	                                String pixAleatorio, boolean status) {
	        conta.setId(id);
	        Cliente cliente = new Cliente();
	        cliente.setId(clienteId);
	        cliente.setNome(clienteNome);
	        cliente.setUser(user); 
	        conta.setCliente(cliente);
	        conta.setTipoConta(tipoConta);
	        conta.setCategoriaConta(categoriaConta);
	        conta.setSaldoConta(saldo);
	        conta.setNumeroConta(numeroConta);
	        conta.setPixAleatorio(pixAleatorio);
	        conta.setStatus(status);
	        conta.setClienteNome(clienteNome);
	        return conta;
	    }
}
