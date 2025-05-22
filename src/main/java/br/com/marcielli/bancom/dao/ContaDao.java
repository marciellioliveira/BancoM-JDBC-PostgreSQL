package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.exception.ChavePixNaoEncontradaException;
import br.com.marcielli.bancom.exception.TaxaDeCambioException;
import br.com.marcielli.bancom.mappers.ContaCorrenteRowMapper;
import br.com.marcielli.bancom.mappers.ContaPoupancaRowMapper;
import br.com.marcielli.bancom.mappers.ContasRowMapper;
import br.com.marcielli.bancom.mappers.TaxaCambioRowMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.CallableStatementCallback;

@Component
public class ContaDao {

	private final JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(ContaDao.class);

	public ContaDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Conta save(Conta conta) {
		logger.info("Iniciando inserção da conta via função insert_conta_completa");

		if (conta.getCliente() == null || conta.getCliente().getId() == null) {
			logger.error("Cliente não pode ser nulo ou sem ID.");
			throw new IllegalArgumentException("Cliente não pode ser nulo ou sem ID.");
		}

		CallableStatementCreator creator = new CallableStatementCreator() {
			@Override
			public CallableStatement createCallableStatement(Connection con) throws SQLException {
				logger.debug("Montando CallableStatement para função insert_conta_completa");
				CallableStatement cs = con
						.prepareCall("{ ? = call insert_conta_completa(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }");

				cs.registerOutParameter(1, Types.BIGINT);

				cs.setLong(2, conta.getCliente().getId());
				cs.setString(3, conta.getTipoConta().name());
				cs.setString(4, conta.getCategoriaConta().name());
				cs.setBigDecimal(5, conta.getSaldoConta());
				cs.setString(6, conta.getNumeroConta());
				cs.setString(7, conta.getPixAleatorio());
				cs.setBoolean(8, conta.getStatus());

				switch (conta) {
				case ContaCorrente cc -> {
					cs.setBigDecimal(9, cc.getTaxaManutencaoMensal());
					cs.setNull(10, Types.NUMERIC);
					cs.setNull(11, Types.NUMERIC);
				}
				case ContaPoupanca cp -> {
					cs.setNull(9, Types.NUMERIC);
					cs.setBigDecimal(10, cp.getTaxaAcrescRend());
					cs.setBigDecimal(11, cp.getTaxaMensal());
				}
				default -> {
					cs.setNull(9, Types.NUMERIC);
					cs.setNull(10, Types.NUMERIC);
					cs.setNull(11, Types.NUMERIC);
				}
				}

				return cs;
			}
		};

		CallableStatementCallback<Conta> callback = new CallableStatementCallback<>() {
			@Override
			public Conta doInCallableStatement(CallableStatement cs) throws SQLException {
				logger.info("Executando função insert_conta_completa");
				cs.execute();
				long generatedId = cs.getLong(1);
				conta.setId(generatedId);
				logger.info("Conta inserida com ID: {}", generatedId);
				return conta;
			}
		};

		try {
			return jdbcTemplate.execute(creator, callback);
		} catch (Exception e) {
			logger.error("Erro ao executar a função insert_conta_completa", e);
			throw new RuntimeException("Erro ao inserir conta via procedure", e);
		}
	}

	public Cliente saveCliente(Cliente cliente) {
		// Inserindo o cliente na tabela.
		String sql = """
				    INSERT INTO clientes (nome, user_id)
				    VALUES (?, ?)
				""";

		jdbcTemplate.update(sql, cliente.getNome(), cliente.getId());

		String sqlSelect = "SELECT LAST_INSERT_ID()";
		Long clienteId = jdbcTemplate.queryForObject(sqlSelect, Long.class);

		cliente.setId(clienteId);

		return cliente;
	}

	public boolean existeConta(Long idConta) {
		String sql = "SELECT COUNT(1) FROM contas WHERE id = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idConta);
		return count != null && count > 0;
	}

	public boolean desativarConta(Long idConta) {
		String sql = "SELECT desativar_conta_v1(?)";
		Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, idConta);
		return Boolean.TRUE.equals(result);
	}

	public boolean ativarConta(Long idConta) {
		String sql = "SELECT ativar_conta_v1(?)";
		Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, idConta);
		return Boolean.TRUE.equals(result);
	}

	public List<Conta> findAll() {
		String sql = "SELECT * FROM public.find_all_contas_v1()";
		return jdbcTemplate.query(sql, new ContasRowMapper());
	}

	public List<Conta> findByUsername(String username) {
		String sql = "SELECT * FROM public.find_contas_by_username_v1(?)";
		return jdbcTemplate.query(sql, new ContasRowMapper(), username);
	}

	public Optional<Conta> findByIdAndUsername(Long id, String username) {
		String sql = "SELECT * FROM find_conta_by_id_and_username_v1(?, ?)";
		List<Conta> contas = jdbcTemplate.query(sql, new ContasRowMapper(), id, username);
		return contas.isEmpty() ? Optional.empty() : Optional.of(contas.get(0));
	}

	public Optional<Conta> findById(Long id) {
		String sql = "SELECT * FROM find_conta_by_id_v1(?)";
		List<Conta> contas = jdbcTemplate.query(sql, new ContasRowMapper(), id);
		return contas.isEmpty() ? Optional.empty() : Optional.of(contas.get(0));
	}

//	public void atualizarPixAleatorio(Long idConta, String novoPix) {
//		String sql = "UPDATE contas SET pix_aleatorio = ? WHERE id = ?";
//		jdbcTemplate.update(sql, novoPix, idConta);
//	}
	
	public boolean atualizarPixAleatorio(Long idConta, String novoPix) {
	    String sql = "SELECT atualizar_pix_aleatorio_v1(?, ?)";
	    Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, idConta, novoPix);
	    return Boolean.TRUE.equals(result);
	}


	public void updateSaldo(Conta conta) {
		String sql = "UPDATE contas SET saldo_conta = ?, categoria_conta = ?, "
				+ "taxa_manutencao_mensal = ?, taxa_acresc_rend = ?, taxa_mensal = ? " + "WHERE id = ?";

		Object[] params;

		if (conta instanceof ContaCorrente cc) {
			params = new Object[] { conta.getSaldoConta(), conta.getCategoriaConta().name(),
					cc.getTaxaManutencaoMensal(), null, null, conta.getId() };
		} else {
			ContaPoupanca cp = (ContaPoupanca) conta;
			params = new Object[] { conta.getSaldoConta(), conta.getCategoriaConta().name(), null,
					cp.getTaxaAcrescRend(), cp.getTaxaMensal(), conta.getId() };
		}

		jdbcTemplate.update(sql, params);
	}

	public BigDecimal getTaxaCambio(String moedaOrigem, String moedaDestino) {
		String sql = """
				    SELECT taxa FROM taxas_cambio
				    WHERE moeda_origem = ? AND moeda_destino = ?
				    ORDER BY data_atualizacao DESC
				    LIMIT 1
				""";

		try {
			return jdbcTemplate.queryForObject(sql, new TaxaCambioRowMapper(), moedaOrigem, moedaDestino);
		} catch (EmptyResultDataAccessException e) {
			throw new TaxaDeCambioException("Taxa de câmbio não encontrada para: " + moedaOrigem + "->" + moedaDestino);
		}
	}

	public Map<String, BigDecimal> getMultiplasTaxasCambio(String moedaOrigem, List<String> moedasDestino) {
		String sql = """
				    SELECT moeda_destino, taxa FROM (
				        SELECT moeda_destino, taxa,
				               ROW_NUMBER() OVER (PARTITION BY moeda_destino ORDER BY data_atualizacao DESC) as rn
				        FROM taxas_cambio
				        WHERE moeda_origem = ? AND moeda_destino IN (%s)
				    ) t WHERE rn = 1
				""";

		String inClause = String.join(",", Collections.nCopies(moedasDestino.size(), "?"));
		sql = String.format(sql, inClause);

		List<Object> params = new ArrayList<Object>();
		params.add(moedaOrigem);
		params.addAll(moedasDestino);

		return jdbcTemplate.query(sql, rs -> {
			Map<String, BigDecimal> result = new LinkedHashMap<>();
			while (rs.next()) {
				result.put(rs.getString("moeda_destino"), rs.getBigDecimal("taxa"));
			}
			return result;
		}, params.toArray());
	}

	public boolean existsByIdAndUsername(Long contaId, String username) {
		String sql = """
				    SELECT COUNT(c.id) > 0
				    FROM contas c
				    JOIN clientes cl ON c.cliente_id = cl.id
				    JOIN users u ON cl.user_id = u.id
				    WHERE c.id = ? AND u.username = ?
				""";

		return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, contaId, username));
	}

	public Conta findByChavePix(String chave) {
		String sql = """
				    SELECT c.*, cl.id AS cliente_id, cl.nome AS cliente_nome,
				           u.id AS user_id, u.username
				    FROM contas c
				    JOIN clientes cl ON c.cliente_id = cl.id
				    JOIN users u ON cl.user_id = u.id
				    WHERE c.pix_aleatorio = ?
				""";

		List<Conta> contas = jdbcTemplate.query(sql, new ContasRowMapper(), chave);
		if (!contas.isEmpty()) {
			return contas.get(0);
		}
		throw new ChavePixNaoEncontradaException("Chave PIX não encontrada: " + chave);
	}

	public Optional<ContaCorrente> findContaCorrenteById(Long id) {
		String sql = """
				    SELECT
				        c.*,
				        cl.id AS cliente_id,
				        cl.nome AS cliente_nome
				    FROM contas c
				    JOIN clientes cl ON c.cliente_id = cl.id
				    WHERE c.id = ? AND c.tipo_conta = 'CORRENTE'
				""";

		try {
			ContaCorrente cc = jdbcTemplate.queryForObject(sql, new ContaCorrenteRowMapper(), id);
			return Optional.ofNullable(cc);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public void updateContaCorrente(ContaCorrente cc) {
		String sql = """
				    UPDATE contas SET
				        saldo_conta = ?,
				        categoria_conta = ?,
				        taxa_manutencao_mensal = ?
				    WHERE id = ? AND tipo_conta = 'CORRENTE'
				""";

		jdbcTemplate.update(sql, cc.getSaldoConta(), cc.getCategoriaConta().name(), cc.getTaxaManutencaoMensal(),
				cc.getId());
	}

	public Optional<ContaPoupanca> findContaPoupancaById(Long id) {
		String sql = """
				    SELECT
				        c.*,
				        cl.id AS cliente_id,
				        cl.nome AS cliente_nome
				    FROM contas c
				    JOIN clientes cl ON c.cliente_id = cl.id
				    WHERE c.id = ? AND c.tipo_conta = 'POUPANCA'
				""";

		try {
			ContaPoupanca cp = jdbcTemplate.queryForObject(sql, new ContaPoupancaRowMapper(), id);
			return Optional.ofNullable(cp);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	// Deixei o em lote por ser melhor por desempenho. O outro conta por conta pode
	// travar se tiver muitas contas.
	public void aplicarRendimentoEmLotes(int batchSize) { // batchSize = qnt de contas a ser processada por vez
		String selectSql = """
				    SELECT id FROM contas
				    WHERE tipo_conta = 'POUPANCA'
				    AND status = true
				    AND taxa_acresc_rend IS NOT NULL
				    LIMIT ?
				""";

		String updateSql = """
				    UPDATE contas
				    SET saldo_conta = saldo_conta + (saldo_conta * taxa_acresc_rend),
				        categoria_conta = (
				            SELECT categoria FROM (
				                VALUES
				                (10000, 'PREMIUM'),
				                (5000, 'SUPER'),
				                (0, 'COMUM')
				            ) AS limites(valor, categoria)
				            WHERE saldo_conta >= limites.valor
				            ORDER BY limites.valor DESC
				            LIMIT 1
				        )
				    WHERE id = ?
				""";

		List<Long> ids = jdbcTemplate.queryForList(selectSql, Long.class, batchSize);

		jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setLong(1, ids.get(i));
			}

			@Override
			public int getBatchSize() {
				return ids.size();
			}
		});
	}

	public void aplicarTaxaManutencaoEmLotes(int batchSize) { // batchSize = qnt de contas a ser processada por vez
		String selectSql = "SELECT id FROM contas WHERE tipo_conta = 'CORRENTE' AND status = true AND taxa_manutencao_mensal IS NOT NULL LIMIT ?";
		List<Long> ids = jdbcTemplate.queryForList(selectSql, Long.class, batchSize);

		String updateSql = """
				    UPDATE contas
				    SET saldo_conta = saldo_conta - taxa_manutencao_mensal,
				        categoria_conta = CASE
				            WHEN (saldo_conta - taxa_manutencao_mensal) >= 10000 THEN 'PREMIUM'
				            WHEN (saldo_conta - taxa_manutencao_mensal) >= 5000 THEN 'SUPER'
				            ELSE 'COMUM'
				        END
				    WHERE id = ?
				""";

		jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setLong(1, ids.get(i));
			}

			@Override
			public int getBatchSize() {
				return ids.size();
			}
		});
	}

	public void update(Conta conta) {
		String sql = """
				    UPDATE contas SET
				        saldo_conta = ?,
				        categoria_conta = ?,
				        taxa_manutencao_mensal = ?,
				        taxa_acresc_rend = ?,
				        taxa_mensal = ?
				    WHERE id = ?
				""";

		Object[] params;

		if (conta instanceof ContaCorrente cc) {
			params = new Object[] { conta.getSaldoConta(), conta.getCategoriaConta().name(),
					cc.getTaxaManutencaoMensal(), null, null, conta.getId() };
		} else if (conta instanceof ContaPoupanca cp) {
			params = new Object[] { conta.getSaldoConta(), conta.getCategoriaConta().name(), null,
					cp.getTaxaAcrescRend(), cp.getTaxaMensal(), conta.getId() };
		} else {
			params = new Object[] { conta.getSaldoConta(), conta.getCategoriaConta().name(), null, null, null,
					conta.getId() };
		}

		jdbcTemplate.update(sql, params);
	}

	public void atualizarTransferenciasEnviadas(Long contaId, Transferencia transferencia) {
		Conta conta = findById(contaId).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
		if (conta.getTransferencias() == null) {
			conta.setTransferencias(new ArrayList<>());
		}
		conta.getTransferencias().add(transferencia);
		update(conta);
	}

	public void atualizarSaldo(Long contaId, BigDecimal novoSaldo) {
		Conta conta = findById(contaId).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
		conta.setSaldoConta(novoSaldo);
		update(conta);
	}

}
