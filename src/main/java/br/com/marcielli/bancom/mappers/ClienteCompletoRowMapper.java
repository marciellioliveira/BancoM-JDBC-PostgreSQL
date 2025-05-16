package br.com.marcielli.bancom.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import br.com.marcielli.bancom.dao.ClienteDao;
import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.enuns.TipoTransferencia;

public class ClienteCompletoRowMapper implements ResultSetExtractor<Void> {

	private final Map<Long, Cliente> clienteMap = new HashMap<Long, Cliente>();
    private final Map<Long, Conta> contaMap = new HashMap<Long, Conta>();
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteCompletoRowMapper.class);
    
    private Cliente clienteFinal;
	
	@Override
	public Void extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		while (rs.next()) { //Todo o conteúdo que antes estava no ClienteDao, agora fica aqui no while.
			
			Long clienteIdRs = rs.getLong("cliente_id");
			
			//Aqui devo criar ou pegar um cliente que já existe
            Cliente cliente = clienteMap.computeIfAbsent(clienteIdRs, id -> mapCliente(rs, id));
   
            Long contaId = rs.getLong("conta_id");
            
            if (!rs.wasNull()) {
                // Aqui devo criar ou pegar uma conta existente
                Conta conta = contaMap.computeIfAbsent(contaId, id -> mapConta(rs, id));

                // Se ainda não adicionou a conta ao cliente, deve adicionar agora
                if (cliente.getContas().stream().noneMatch(c -> c.getId().equals(contaId))) {
                    cliente.getContas().add(conta);
                }

                // Faz o processamento do cartão de débito/crédito
                processarCartao(rs, conta);

                // Faz o processamento da transferência
                mapTransferencia(rs, conta, clienteIdRs);
            }

            //Salva tudo no clienteFinal
            clienteFinal = cliente;
            
		}
		return null;
	}

	
	private Cliente mapCliente(ResultSet rs, Long id) { //metodo privado que encapsula a logica do mapeamento de cliente
        Cliente c = new Cliente();
        c.setId(id);
        
       
        try {
            c.setNome(rs.getString("nome"));
            String cpfStr = rs.getString("cpf");
            logger.debug("[DEBUG] Mapeando cpf do cliente: " + cpfStr);
            if (cpfStr != null) {
                c.setCpf(Long.valueOf(cpfStr));
            }
            c.setClienteAtivo(rs.getBoolean("cliente_ativo"));
            c.setContas(new ArrayList<Conta>());
            

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao mapear cliente: " + e.getMessage(), e);
        }
        return c;
    }
	
	private Conta mapConta(ResultSet rs, Long id) {//metodo privado que encapsula a logica do mapeamento de conta
        Conta conta = new Conta();
        conta.setId(id);
        conta.setCartoes(new ArrayList<>());
        conta.setTransferencias(new ArrayList<>());

        try {
            conta.setNumeroConta(rs.getString("numero_conta"));
            conta.setPixAleatorio(rs.getString("pix_aleatorio"));
            conta.setStatus(rs.getBoolean("status_conta"));
            conta.setSaldoConta(rs.getBigDecimal("saldo_conta"));

            String tipoContaStr = rs.getString("tipo_conta");
            try {
                conta.setTipoConta(tipoContaStr != null ? TipoConta.valueOf(tipoContaStr) : null);
            } catch (IllegalArgumentException e) {
                conta.setTipoConta(null);
            }

            String categoriaContaStr = rs.getString("categoria_conta");
            try {
                conta.setCategoriaConta(categoriaContaStr != null ? CategoriaConta.valueOf(categoriaContaStr) : null);
            } catch (IllegalArgumentException e) {
                conta.setCategoriaConta(null);
            }

            if (conta instanceof ContaCorrente) {
                ((ContaCorrente) conta).setTaxaManutencaoMensal(rs.getBigDecimal("taxa_manutencao_mensal"));
            }
            if (conta instanceof ContaPoupanca) {
                ((ContaPoupanca) conta).setTaxaAcrescRend(rs.getBigDecimal("taxa_acresc_rend"));
                ((ContaPoupanca) conta).setTaxaMensal(rs.getBigDecimal("taxa_mensal"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao mapear conta: " + e.getMessage(), e);
        }

        return conta;
    }
	
	
	private void processarCartao(ResultSet rs, Conta conta) throws SQLException {
	    Long cartaoId = rs.getLong("cartao_id");
	    if (!rs.wasNull()) {
	        boolean cartaoJaExiste = conta.getCartoes().stream()
	            .anyMatch(c -> c.getId().equals(cartaoId));

	        if (!cartaoJaExiste) {
	            logger.info("Iniciando processamento do cartão: Cartão ID: {}", cartaoId);

	            String tipoCartaoStr = rs.getString("cartao_tipo");
	            logger.debug("Tipo do cartão lido do banco: {}", tipoCartaoStr);

	            TipoCartao tipoCartao = null;
	            try {
	                tipoCartao = tipoCartaoStr != null ? TipoCartao.valueOf(tipoCartaoStr) : null;
	            } catch (IllegalArgumentException e) {
	                logger.warn("TipoCartao inválido: {}. Definindo como null.", tipoCartaoStr);
	            }

	            Cartao cartao = null;
	            boolean cartaoStatus = rs.getBoolean("status_cartao");

	            String categoriaContaStr = rs.getString("categoria_conta");

	            if (tipoCartao == TipoCartao.CREDITO) {
	                CartaoCredito cartaoCredito = new CartaoCredito();
	                cartaoCredito.setId(cartaoId);
	                cartaoCredito.setNumeroCartao(rs.getString("numero_cartao"));
	                cartaoCredito.setTipoCartao(tipoCartao);
	                cartaoCredito.setCategoriaConta(parseCategoriaConta(categoriaContaStr));
	                cartaoCredito.setStatus(cartaoStatus);
	                cartaoCredito.setLimiteCreditoPreAprovado(rs.getBigDecimal("limite_credito_pre_aprovado"));
	                cartaoCredito.setTotalGastoMesCredito(rs.getBigDecimal("total_gasto_mes_credito"));
	                cartaoCredito.setSenha(rs.getString("senha"));
	                cartaoCredito.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));
	                cartaoCredito.setContaId(rs.getLong("conta_id"));
	                cartaoCredito.setFaturaId(rs.getLong("id_fatura"));
	                cartao = cartaoCredito;

	            } else if (tipoCartao == TipoCartao.DEBITO) {
	                CartaoDebito cartaoDebito = new CartaoDebito();
	                cartaoDebito.setId(cartaoId);
	                cartaoDebito.setNumeroCartao(rs.getString("numero_cartao"));
	                cartaoDebito.setTipoCartao(tipoCartao);
	                cartaoDebito.setCategoriaConta(parseCategoriaConta(categoriaContaStr));
	                cartaoDebito.setLimiteDiarioTransacao(rs.getBigDecimal("limite_diario_transacao"));
	                cartaoDebito.setStatus(cartaoStatus);
	                cartaoDebito.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));
	                cartaoDebito.setSenha(rs.getString("senha"));
	                cartaoDebito.setContaId(rs.getLong("conta_id"));
	                cartaoDebito.setFaturaId(rs.getLong("id_fatura"));
	                cartao = cartaoDebito;

	            } else {	              
	                Cartao basico = new Cartao();
	                basico.setId(cartaoId);
	                basico.setNumeroCartao(rs.getString("numero_cartao"));
	                basico.setTipoCartao(null);
	                basico.setCategoriaConta(parseCategoriaConta(categoriaContaStr));
	                basico.setStatus(cartaoStatus);
	                basico.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));
	                basico.setSenha(rs.getString("senha"));
	                basico.setContaId(rs.getLong("conta_id"));
	                basico.setFaturaId(rs.getLong("id_fatura"));
	                cartao = basico;
	                logger.warn("Tipo de cartão desconhecido ou nulo: {}. Criando cartão básico.", tipoCartaoStr);
	            }

	            if (cartao != null) {
	                conta.getCartoes().add(cartao);
	                logger.debug("Cartão adicionado à conta {}: Cartão ID {}, Status: {}", 
	                        conta.getId(), cartao.getId(), cartaoStatus);
	            }
	        } else {
	            logger.debug("Cartão ID {} já existe na conta {}. Pulando duplicação.", cartaoId, conta.getId());
	        }
	    }
	}

	private CategoriaConta parseCategoriaConta(String categoriaStr) {
	    try {
	        return categoriaStr != null ? CategoriaConta.valueOf(categoriaStr) : null;
	    } catch (IllegalArgumentException e) {
	        logger.warn("CategoriaConta inválida: {}. Definindo como null.", categoriaStr);
	        return null;
	    }
	}

	
	private void mapTransferencia(ResultSet rs, Conta conta, Long clienteId) throws SQLException {
	    Long transferenciaId = rs.getLong("transferencia_id");
	    if (rs.wasNull()) return;

	    Long idClienteOrigem = rs.getLong("id_cliente_origem");

	    if (!clienteId.equals(idClienteOrigem)) return;

	    boolean transferenciaJaExiste = conta.getTransferencias().stream()
	        .anyMatch(t -> t.getId().equals(transferenciaId));

	    if (transferenciaJaExiste) return;

	    Transferencia transferencia = new Transferencia();
	    transferencia.setId(transferenciaId);
	    transferencia.setValor(rs.getBigDecimal("valor"));
	    
	    Timestamp timestamp = rs.getTimestamp("data");
	    if (timestamp != null) {
	        transferencia.setData(timestamp.toLocalDateTime());
	    }

	    transferencia.setIdClienteOrigem(idClienteOrigem);
	    transferencia.setIdClienteDestino(rs.getLong("id_cliente_destino"));
	    transferencia.setIdContaOrigem(rs.getLong("id_conta_origem"));
	    transferencia.setIdContaDestino(rs.getLong("id_conta_destino"));
	    
	    Long idCartaoTransferencia = rs.getLong("id_cartao");
	    transferencia.setIdCartao(idCartaoTransferencia);
	    transferencia.setFaturaId(rs.getLong("fatura_id"));
	    transferencia.setCodigoOperacao(rs.getString("codigo_operacao"));

	    // Tipo Transferência
	    String tipoTransferenciaStr = rs.getString("tipo_transferencia");
	    try {
	        transferencia.setTipoTransferencia(tipoTransferenciaStr != null ? 
	            TipoTransferencia.valueOf(tipoTransferenciaStr) : null);
	    } catch (IllegalArgumentException e) {
	        logger.warn("TipoTransferencia inválido: {}. Definindo como null.", tipoTransferenciaStr);
	        transferencia.setTipoTransferencia(null);
	    }

	    // Tipo Cartão
	    String tipoCartaoStr = rs.getString("tipo_cartao");
	    try {
	        transferencia.setTipoCartao(tipoCartaoStr != null ? 
	            TipoCartao.valueOf(tipoCartaoStr) : null);
	    } catch (IllegalArgumentException e) {
	        logger.warn("TipoCartao inválido: {}. Definindo como null.", tipoCartaoStr);
	        transferencia.setTipoCartao(null);
	    }

	    conta.getTransferencias().add(transferencia);
	    logger.debug("Transferência adicionada à conta {}: ID {}", conta.getId(), transferencia.getId());

	    // Se for transferência de crédito, associar ao cartão de crédito
	    if (idCartaoTransferencia != null && idCartaoTransferencia > 0 &&
	        "CARTAO_CREDITO".equals(tipoTransferenciaStr)) {

	        for (Cartao cartao : conta.getCartoes()) {
	            if (cartao.getId().equals(idCartaoTransferencia) && cartao instanceof CartaoCredito) {
	                CartaoCredito cartaoCredito = (CartaoCredito) cartao;

	                boolean creditoJaExiste = cartaoCredito.getTransferenciasCredito()
	                    .stream().anyMatch(t -> t.getId().equals(transferenciaId));

	                if (!creditoJaExiste) {
	                    cartaoCredito.getTransferenciasCredito().add(transferencia);
	                    logger.debug("Transferência de crédito adicionada ao cartão {}: ID {}",
	                        cartao.getId(), transferencia.getId());
	                }
	                break;
	            }
	        }
	    }
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Cliente getCliente() {
        return clienteFinal;
    }

}

