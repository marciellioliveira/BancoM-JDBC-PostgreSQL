package br.com.marcielli.bancom.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Fatura;
import br.com.marcielli.bancom.entity.Seguro;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.exception.CartaoNaoEncontradoException;
import br.com.marcielli.bancom.mappers.CartaoRowMapper;
import br.com.marcielli.bancom.mappers.FaturaWithTransferenciasRowMapper;

@Component
public class CartaoDao {

	private final JdbcTemplate jdbcTemplate;
	private final FaturaWithTransferenciasRowMapper faturaWithTransferenciaRowMapper;
	private static final Logger logger = LoggerFactory.getLogger(CartaoDao.class);

	public CartaoDao(JdbcTemplate jdbcTemplate, FaturaWithTransferenciasRowMapper faturaWithTransferenciaRowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.faturaWithTransferenciaRowMapper = faturaWithTransferenciaRowMapper;
	}

	public Cartao saveWithRelations(Cartao cartao) { //professor fez

		String sqlCartao = "SELECT * FROM public.inserir_cartao_com_relacoes_v1(?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Long cartaoId = jdbcTemplate.queryForObject(sqlCartao, Long.class, cartao.getTipoConta().toString(),
				cartao.getCategoriaConta().toString(), cartao.getTipoCartao().toString(), cartao.getNumeroCartao(),
				cartao.isStatus(), cartao.getSenha(), cartao.getConta().getId(),
				cartao instanceof CartaoCredito ? ((CartaoCredito) cartao).getLimiteCreditoPreAprovado() : null,
				cartao instanceof CartaoDebito ? ((CartaoDebito) cartao).getLimiteDiarioTransacao() : null);
		logger.info("cartaoDao  {}", cartao.getConta().getId());
		cartao.setId(cartaoId);

		if (cartao.getFatura() != null) {
			insertFatura(cartaoId, cartao.getFatura());
		}

		if (cartao.getSeguros() != null && !cartao.getSeguros().isEmpty()) {
			insertSeguros(cartaoId, cartao.getSeguros());
		}

		if (cartao.getConta() != null) {
			cartao.setContaId(cartao.getConta().getId());
		}
		if (cartao.getFatura() != null) {
			cartao.setFaturaId(cartao.getFatura().getId());
		}

		return cartao;
	}

//	public boolean existeCartao(Long idCartao) {
//		String sql = "SELECT COUNT(1) FROM cartoes WHERE id = ?";
//		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idCartao);
//		return count != null && count > 0;
//	}
	
	public boolean existeCartao(Long idCartao) {
	    String sql = "SELECT public.existe_cartao_v1(?)";
	    Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, idCartao);
	    return exists != null && exists;
	}


	private void insertFatura(Long cartaoId, Fatura fatura) {
		String sqlFatura = """
				    INSERT INTO faturas
				    (cartao_id, valor_total, data_vencimento)
				    VALUES (?, ?, ?)
				""";
		jdbcTemplate.update(sqlFatura, cartaoId,
				// fatura.getValorTotal(), // Descomente se necessário
				fatura.getDataVencimento());
	}

	private void insertSeguros(Long cartaoId, List<Seguro> seguros) {
		String sqlSeguro = """
				    INSERT INTO seguros
				    (cartao_id, tipo, valor_cobertura)
				    VALUES (?, ?, ?)
				""";
		List<Object[]> batchArgs = seguros.stream().map(s -> new Object[] { cartaoId, s.getTipo().toString(),
				// s.getValorCobertura()
		}).collect(Collectors.toList());
		jdbcTemplate.batchUpdate(sqlSeguro, batchArgs);
	}
	
	public Optional<Cartao> findById(Long id) {
	    logger.info("Iniciando busca pelo cartão com ID: {}", id);
	    String sql = "SELECT * FROM public.find_cartao_by_id_v1(?)";
	    try {
	        Cartao cartao = jdbcTemplate.queryForObject(sql, new CartaoRowMapper(), id);
	        logger.info("Cartão encontrado: {}", cartao);
	        return Optional.of(cartao);
	    } catch (EmptyResultDataAccessException e) {
	        logger.error("Nenhum cartão encontrado para o ID: {}", id, e);
	        throw new CartaoNaoEncontradoException("Cartão não encontrado");
	    } catch (Exception e) {
	        logger.error("Erro inesperado ao buscar o cartão com ID: {}", id, e);
	        throw e;
	    }
	}


//	public Optional<Cartao> findById(Long id) {
//		logger.info("Iniciando busca pelo cartão com ID: {}", id);
//		String sql = """
//					SELECT
//				    c.id,
//				    c.tipo_conta,
//				    c.categoria_conta,
//				    c.tipo_cartao,
//				    c.numero_cartao,
//				    c.status,
//				    c.senha,
//				    c.limite_credito_pre_aprovado,
//				    c.taxa_utilizacao,
//				    c.taxa_seguro_viagem,
//				    c.total_gasto_mes_credito,
//				    c.limite_diario_transacao,
//				    c.total_gasto_mes,
//				    c.conta_id AS cartao_conta_id,
//				    c.fatura_id AS cartao_fatura_id,
//				    ct.id AS conta_id,
//				    ct.cliente_id,
//				    ct.tipo_conta AS conta_tipo_conta,
//				    ct.categoria_conta AS conta_categoria_conta,
//				    ct.saldo_conta,
//				    ct.numero_conta,
//				    ct.pix_aleatorio,
//				    ct.status AS conta_status,
//				    ct.taxa_manutencao_mensal,
//				    ct.taxa_acresc_rend,
//				    ct.taxa_mensal,
//				    cli.id AS cliente_id,
//				    cli.nome AS cliente_nome,
//				    cli.cpf AS cliente_cpf,
//				    cli.cliente_ativo AS cliente_ativo,
//				    cli.user_id AS cliente_user_id,
//				    f.id AS fatura_id,
//				    f.cartao_id AS fatura_cartao_id,
//				    f.valor_total AS fatura_valor_total,
//				    f.data_vencimento AS fatura_data_vencimento
//				FROM cartoes c
//				JOIN contas ct ON c.conta_id = ct.id
//				JOIN clientes cli ON ct.cliente_id = cli.id
//				LEFT JOIN faturas f ON c.fatura_id = f.id
//				WHERE c.id = ?
//						    """;
//		try {
//			Cartao cartao = jdbcTemplate.queryForObject(sql, new CartaoRowMapper(), id);
//			logger.info("Cartão encontrado: {}", cartao);
//			return Optional.of(cartao);
//		} catch (EmptyResultDataAccessException e) {
//			logger.error("Nenhum cartão encontrado para o ID: {}", id, e);
//			throw new CartaoNaoEncontradoException("Cartão não encontrado");
//		} catch (Exception e) {
//			logger.error("Erro inesperado ao buscar o cartão com ID: {}", id, e);
//			throw e;
//		}
//	}

	public List<Cartao> findAll() {
		String sql = "SELECT * FROM public.find_all_cartoes_v1()";
		return jdbcTemplate.query(sql, new CartaoRowMapper());
	}

	public List<Cartao> findByUsername(String username) {
	    String sql = "SELECT * FROM public.find_cartoes_by_username_v1(?)";
	    return jdbcTemplate.query(sql, new CartaoRowMapper(), username);
	}

	public Optional<Cartao> findByIdAndUsername(Long id, String username) {
	    String sql = "SELECT * FROM public.find_cartao_by_id_and_username_v1(?, ?)";
	    try {
	        Cartao cartao = jdbcTemplate.queryForObject(sql, new CartaoRowMapper(), id, username);
	        return Optional.ofNullable(cartao);
	    } catch (EmptyResultDataAccessException e) {
	        return Optional.empty();
	    }
	}
	
	public Cartao alterarSenhaCartao(Cartao cartao) {
	    String sql = "SELECT public.alterar_senha_cartao_v1(?, ?)";
	    String result = jdbcTemplate.queryForObject(sql, String.class, cartao.getId(), cartao.getSenha());
	    if (!"OK".equals(result)) {
	    	logger.info("Resultado da função alterar_senha_cartao_v1: {}", result);
	        throw new CartaoNaoEncontradoException("Erro ao alterar senha do cartão no banco!");
	    }
	    return cartao;
	}


	// Método para desativar
	public boolean desativarCartao(Long id) {
	    String sql = "SELECT public.desativar_cartao_v1(?)";
	    Integer rowsAffected = jdbcTemplate.queryForObject(sql, Integer.class, id);

	    return rowsAffected != null && rowsAffected > 0;
	}

//	public boolean desativarCartao(Long id) {
//		String sql = "UPDATE cartoes SET status = false WHERE id = ?";
//		int rowsAffected = jdbcTemplate.update(sql, id);
//		return rowsAffected > 0;
//	}

//	public boolean ativarCartao(Long idCartao) {
//		String sql = "UPDATE cartoes SET status = true WHERE id = ?";
//		int rowsAffected = jdbcTemplate.update(sql, idCartao);
//
//		if (rowsAffected == 0) {
//			throw new EmptyResultDataAccessException("Nenhuma conta encontrada com ID: " + idCartao, 1);
//		}
//
//		return true;
//	}
	
	public boolean ativarCartao(Long idCartao) {
	    String sql = "SELECT public.ativar_cartao_v1(?)";
	    Integer rowsAffected = jdbcTemplate.queryForObject(sql, Integer.class, idCartao);

	    if (rowsAffected == null || rowsAffected == 0) {
	        throw new EmptyResultDataAccessException("Nenhum cartão encontrado com ID: " + idCartao, 1);
	    }

	    return true;
	}


	// Método que busca todos os cartões associados a uma conta
	public List<Cartao> findByContaId(Long contaId) {
		String sql = "SELECT c.*, t.saldo_conta\r\n" + "FROM cartoes c\r\n" + "JOIN contas t ON c.conta_id = t.id\r\n"
				+ "WHERE c.conta_id = ?\r\n" + "";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			Cartao cartao = new Cartao();
			cartao.setId(rs.getLong("id"));
			cartao.setNumeroCartao(rs.getString("numero_cartao"));
			cartao.setStatus(rs.getBoolean("status"));
			cartao.setTipoConta(TipoConta.valueOf(rs.getString("tipo_conta")));
			cartao.setCategoriaConta(CategoriaConta.valueOf(rs.getString("categoria_conta")));
			cartao.setTipoCartao(TipoCartao.valueOf(rs.getString("tipo_cartao")));
			cartao.setSenha(rs.getString("senha"));
			return cartao;
		}, contaId);
	}

	public Optional<Cartao> findByIdAndClienteId(Long cartaoId, Long clienteId) {
		String sql = "SELECT c.*, co.* FROM cartoes c " + "JOIN contas co ON c.conta_id = co.id "
				+ "WHERE c.id = ? AND co.cliente_id = ?";

		try {
			Cartao cartao = jdbcTemplate.queryForObject(sql, new CartaoRowMapper(), cartaoId, clienteId);
			return Optional.ofNullable(cartao);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public void update(Cartao cartao) {
		String sql = """
				UPDATE cartoes
				SET limite_credito_pre_aprovado = ?, total_gasto_mes_credito = ?,
				    limite_diario_transacao = ?, total_gasto_mes = ?
				WHERE id = ?
				""";
		BigDecimal totalGastoMes = cartao instanceof CartaoDebito ? ((CartaoDebito) cartao).getTotalGastoMes()
				: (cartao instanceof CartaoCredito ? ((CartaoCredito) cartao).getTotalGastoMesCredito() : null);
		logger.info("Atualizando cartão: id={}, total_gasto_mes={}", cartao.getId(), totalGastoMes);
		int rowsAffected = jdbcTemplate.update(sql,
				cartao instanceof CartaoCredito ? ((CartaoCredito) cartao).getLimiteCreditoPreAprovado() : null,
				cartao instanceof CartaoCredito ? ((CartaoCredito) cartao).getTotalGastoMesCredito() : null,
				cartao instanceof CartaoDebito ? ((CartaoDebito) cartao).getLimiteDiarioTransacao() : null,
				totalGastoMes, cartao.getId());
		if (rowsAffected == 0) {
			logger.error("Falha ao atualizar cartão {}. Verifique se o cartão existe.", cartao.getId());
		}
	}

	public void atualizarLimitesCartaoCredito(Long cartaoId, BigDecimal novoLimite, BigDecimal novoTotalGasto) {
		String sql = """
				UPDATE cartoes
				SET limite_credito_pre_aprovado = ?, total_gasto_mes_credito = ?, total_gasto_mes = COALESCE(total_gasto_mes, 0) + ?
				WHERE id = ?
				""";
		logger.info("Atualizando limites cartão crédito: cartaoId={}, novoLimite={}, novoTotalGasto={}", cartaoId,
				novoLimite, novoTotalGasto);
		int rowsAffected = jdbcTemplate.update(sql, novoLimite, novoTotalGasto, novoTotalGasto, cartaoId);
		if (rowsAffected == 0) {
			logger.error("Falha ao atualizar limites do cartão {}. Verifique se o cartão existe.", cartaoId);
		}
	}

	public void associarFaturaAoCartao(Long cartaoId, Long faturaId) {
		String sql = "UPDATE cartoes SET fatura_id = ? WHERE id = ?";
		logger.info("Executando associarFaturaAoCartao: cartaoId={}, faturaId={}", cartaoId, faturaId);
		int rowsAffected = jdbcTemplate.update(sql, faturaId, cartaoId);
		if (rowsAffected == 0) {
			logger.error("Falha ao associar fatura {} ao cartão {}. Verifique se o cartão existe.", faturaId, cartaoId);
		} else {
			logger.info("Fatura {} associada ao cartão {} com sucesso", faturaId, cartaoId);
		}
	}

	public Optional<Fatura> findFaturaByCartaoId(Long cartaoId) {
		String sql = """
				SELECT f.id, f.data_vencimento, f.valor_total
				FROM faturas f
				WHERE f.cartao_id = ?
				""";
		try {
			Fatura fatura = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				Fatura f = new Fatura();
				f.setId(rs.getLong("id"));
				f.setDataVencimento(rs.getObject("data_vencimento", LocalDateTime.class));
				f.setValorTotal(rs.getBigDecimal("valor_total"));
				return f;
			}, cartaoId);
			return Optional.ofNullable(fatura);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public void atualizarTotalFatura(Long faturaId, BigDecimal valor) {
		String sql = "UPDATE faturas SET valor_total = COALESCE(valor_total, 0) + ? WHERE id = ?";
		jdbcTemplate.update(sql, valor, faturaId);
	}

	public void verificarEAssociarFatura(Long cartaoId, Long faturaId) {
		String checkFaturaSql = "SELECT fatura_id FROM cartoes WHERE id = ?";
		try {
			Long currentFaturaId = jdbcTemplate.queryForObject(checkFaturaSql, Long.class, cartaoId);
			if (currentFaturaId == null) {
				logger.info("fatura_id está NULL para cartaoId={}. Associando faturaId={}", cartaoId, faturaId);
				associarFaturaAoCartao(cartaoId, faturaId);
			} else if (!currentFaturaId.equals(faturaId)) {
				logger.warn("fatura_id={} já existe para cartaoId={}, mas tentando associar faturaId={}. Atualizando.",
						currentFaturaId, cartaoId, faturaId);
				associarFaturaAoCartao(cartaoId, faturaId);
			} else {
				logger.info("fatura_id={} já está associado ao cartaoId={}", faturaId, cartaoId);
			}
		} catch (EmptyResultDataAccessException e) {
			logger.error("Cartão com id={} não encontrado ao verificar fatura_id", cartaoId);
			throw new IllegalArgumentException("Cartão não encontrado: " + cartaoId);
		}
	}

	public void atualizarTransferenciasCredito(Long cartaoId, Transferencia transferencia) {
		String sql = """
				UPDATE transferencias
				SET cartao_id = ?
				WHERE id = ?
				""";
		jdbcTemplate.update(sql, cartaoId, transferencia.getId());
	}

	public void alterarLimiteCartaoCredito(Cartao cartao) {
		if (cartao instanceof CartaoCredito cartaoCredito) {
			String sql = "UPDATE cartoes SET limite_credito_pre_aprovado = ? WHERE id = ?";
			jdbcTemplate.update(sql, cartaoCredito.getLimiteCreditoPreAprovado(), cartaoCredito.getId());
			logger.info("Limite de crédito do cartão com ID {} atualizado para {}", cartaoCredito.getId(),
					cartaoCredito.getLimiteCreditoPreAprovado());
		} else {
			throw new IllegalArgumentException("O cartão informado não é do tipo Cartao de Credito.");
		}
	}

	public void alterarLimiteCartaoDebito(Cartao cartao) {
		if (cartao instanceof CartaoDebito cartaoDebito) {
			String sql = "UPDATE cartoes SET limite_diario_transacao = ? WHERE id = ?";
			jdbcTemplate.update(sql, cartaoDebito.getLimiteDiarioTransacao(), cartaoDebito.getId());
			logger.info("Limite de crédito do cartão com ID {} atualizado para {}", cartaoDebito.getId(),
					cartaoDebito.getLimiteDiarioTransacao());
		} else {
			throw new IllegalArgumentException("O cartão informado não é do tipo Cartao de Débito.");
		}
	}

	public Fatura buscarFaturaComTransferenciasPorCartaoId(Long cartaoId) {
		String sql = """
				    SELECT id, cartao_id, valor_total, data_vencimento, status
				    FROM faturas
				    WHERE cartao_id = ?
				""";

		List<Fatura> faturas = jdbcTemplate.query(sql, faturaWithTransferenciaRowMapper, cartaoId);

		return faturas.isEmpty() ? null : faturas.get(0);
	}

}
