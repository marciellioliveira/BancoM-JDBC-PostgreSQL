package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.ResultSetExtractor;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.Seguro;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.enuns.TipoSeguro;

public class ClienteContasCartoesSegurosExtractor implements ResultSetExtractor<Cliente> {

	@Override
    public Cliente extractData(ResultSet rs) throws SQLException {
        Cliente cliente = null;
        Map<Long, Conta> contaMap = new HashMap<Long, Conta>();
        Map<Long, Cartao> cartaoMap = new HashMap<Long, Cartao>();

        while (rs.next()) {
            if (cliente == null) {
                cliente = new Cliente();
                cliente.setId(rs.getLong("cliente_id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setCpf(rs.getLong("cpf"));  
                cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));
                cliente.setContas(new ArrayList<Conta>());
            }
            
            Long contaId = rs.getLong("conta_id");
            if (contaId != 0) {
                Conta conta = contaMap.get(contaId);
                if (conta == null) {
                    conta = new Conta();
                    conta.setId(contaId);
                    conta.setPixAleatorio(rs.getString("pix_aleatorio"));
                    conta.setStatus(rs.getBoolean("conta_status"));
                    conta.setSaldoConta(rs.getBigDecimal("saldo_conta"));
                    conta.setTipoConta(TipoConta.valueOf(rs.getString("tipo_conta")));
                    conta.setCategoriaConta(CategoriaConta.valueOf(rs.getString("conta_categoria_conta")));
                    conta.setNumeroConta(rs.getString("numero_conta"));
                    conta.setCartoes(new ArrayList<>());
                    contaMap.put(contaId, conta);
                    cliente.getContas().add(conta);
                }

                Long cartaoId = rs.getLong("cartao_id");
                if (cartaoId != 0) {
                    Cartao cartao = cartaoMap.get(cartaoId);
                    if (cartao == null) {
                        cartao = new Cartao();
                        cartao.setId(cartaoId);
                        cartao.setStatus(rs.getBoolean("cartao_status"));
                        cartao.setSeguros(new ArrayList<>());
                        cartaoMap.put(cartaoId, cartao);
                        conta.getCartoes().add(cartao);
                    }

                    Long seguroId = rs.getLong("seguro_id");
                    if (seguroId != 0) {
                        Seguro seguro = new Seguro();
                        seguro.setId(seguroId);
                        seguro.setTipo(TipoSeguro.valueOf(rs.getString("tipo_seguro")));
                        seguro.setAtivo(rs.getBoolean("seguro_ativo"));
                        seguro.setValorMensal(rs.getBigDecimal("valor_mensal"));
                        seguro.setValorApolice(rs.getBigDecimal("valor_apolice"));
                        cartao.getSeguros().add(seguro);
                    }
                }
            }
        }

        return cliente;
    }
    
	
}
