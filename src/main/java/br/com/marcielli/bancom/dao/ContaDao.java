package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.exception.ChavePixNaoEncontradaException;
import br.com.marcielli.bancom.mappers.ContaCorrenteRowMapper;
import br.com.marcielli.bancom.mappers.ContaPoupancaRowMapper;
import br.com.marcielli.bancom.mappers.ContasRowMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;
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

	public boolean existeConta(Long idConta) {
		String sql = "SELECT existe_conta_v1(?)";
		Boolean existe = jdbcTemplate.queryForObject(sql, Boolean.class, idConta);
		return existe != null && existe;
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

	public boolean atualizarPixAleatorio(Long idConta, String novoPix) {
		String sql = "SELECT atualizar_pix_aleatorio_v1(?, ?)";
		Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, idConta, novoPix);
		return Boolean.TRUE.equals(result);
	}

	public boolean updateSaldo(Conta conta) {

		String sql = "SELECT atualizar_saldo_conta_v1(?, ?, ?, ?, ?, ?)";

		Object[] params;

		if (conta instanceof ContaCorrente cc) {

			params = new Object[] { conta.getId(), // p_id
					conta.getSaldoConta(), // p_saldo_conta
					conta.getCategoriaConta().name(), // p_categoria_conta
					cc.getTaxaManutencaoMensal(), // p_taxa_manutencao_mensal
					null, // p_taxa_acresc_rend - Não tem em Conta Corrente
					null // p_taxa_mensal - Não tem em Conta Corrente
			};

		} else {

			ContaPoupanca cp = (ContaPoupanca) conta;

			params = new Object[] { conta.getId(), conta.getSaldoConta(), conta.getCategoriaConta().name(), null, // p_taxa_manutencao_mensal																													
					cp.getTaxaAcrescRend(), cp.getTaxaMensal() };
		}

		Boolean resultado = jdbcTemplate.query(sql, ps -> {
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
		}, rs -> {
			if (rs.next()) {
				return rs.getBoolean(1);
			}
			return false;
		});

		return resultado != null ? resultado : false;
	}

	public Conta findByChavePix(String chave) {

		String sql = "SELECT * FROM buscar_conta_por_chave_pix_v1(?)";

		Conta conta = jdbcTemplate.query(sql, new ContasRowMapper(), chave).stream().findFirst()
				.orElseThrow(() -> new ChavePixNaoEncontradaException("Chave PIX não encontrada: " + chave));

		return conta;

	}

	public Optional<ContaCorrente> findContaCorrenteById(Long id) {
		String sql = "SELECT * FROM buscar_conta_corrente_por_id_v1(?)";

		List<ContaCorrente> contas = jdbcTemplate.query(sql, new ContaCorrenteRowMapper(), id);
		if (contas.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(contas.get(0));
	}

	public boolean updateContaCorrente(ContaCorrente cc) {
		String sql = "SELECT atualizar_conta_corrente_v1(?, ?, ?, ?)";

		return jdbcTemplate.queryForObject(sql, Boolean.class, cc.getId(), cc.getSaldoConta(),
				cc.getCategoriaConta().name(), cc.getTaxaManutencaoMensal());
	}


	public Optional<ContaPoupanca> findContaPoupancaById(Long id) {
		String sql = "SELECT * FROM buscar_conta_poupanca_por_id_v1(?)";

		List<ContaPoupanca> contas = jdbcTemplate.query(sql, new ContaPoupancaRowMapper(), id);

		if (contas.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(contas.get(0));
	}

	// Deixei o em lote por ser melhor por desempenho. O outro conta por conta pode
	// travar se tiver muitas contas.
	public void aplicarRendimentoEmLotes(int batchSize) {
	    String sql = "SELECT aplicar_rendimento_em_lotes_v1(?)";
	    jdbcTemplate.query(sql, ps -> ps.setInt(1, batchSize), rs -> {});
	}
	
	public void aplicarTaxaManutencaoEmLotes(int batchSize) {
	    String sql = "SELECT aplicar_taxa_manutencao_em_lotes_v1(?)";
	    jdbcTemplate.query(sql, ps -> ps.setInt(1, batchSize), rs -> {});
	}

//	public void update(Conta conta) {
//		String sql = """
//				    UPDATE contas SET
//				        saldo_conta = ?,
//				        categoria_conta = ?,
//				        taxa_manutencao_mensal = ?,
//				        taxa_acresc_rend = ?,
//				        taxa_mensal = ?
//				    WHERE id = ?
//				""";
//
//		Object[] params;
//
//		if (conta instanceof ContaCorrente cc) {
//			params = new Object[] { conta.getSaldoConta(), conta.getCategoriaConta().name(),
//					cc.getTaxaManutencaoMensal(), null, null, conta.getId() };
//		} else if (conta instanceof ContaPoupanca cp) {
//			params = new Object[] { conta.getSaldoConta(), conta.getCategoriaConta().name(), null,
//					cp.getTaxaAcrescRend(), cp.getTaxaMensal(), conta.getId() };
//		} else {
//			params = new Object[] { conta.getSaldoConta(), conta.getCategoriaConta().name(), null, null, null,
//					conta.getId() };
//		}
//
//		jdbcTemplate.update(sql, params);
//	}
	
	public void update(Conta conta) {
	    String sql = "SELECT public.update_conta_v1(?, ?, ?, ?, ?, ?)";

	    Object[] params;

	    if (conta instanceof ContaCorrente cc) {
	        params = new Object[] {
	            conta.getId(),
	            conta.getSaldoConta(),
	            conta.getCategoriaConta().name(),
	            cc.getTaxaManutencaoMensal(),
	            null,
	            null
	        };
	    } else if (conta instanceof ContaPoupanca cp) {
	        params = new Object[] {
	            conta.getId(),
	            conta.getSaldoConta(),
	            conta.getCategoriaConta().name(),
	            null,
	            cp.getTaxaAcrescRend(),
	            cp.getTaxaMensal()
	        };
	    } else {
	        params = new Object[] {
	            conta.getId(),
	            conta.getSaldoConta(),
	            conta.getCategoriaConta().name(),
	            null,
	            null,
	            null
	        };
	    }

	    Integer rowsAffected = jdbcTemplate.queryForObject(sql, Integer.class, params);

	    if (rowsAffected == null || rowsAffected == 0) {
	        logger.error("Falha ao atualizar conta {}. Verifique se a conta existe.", conta.getId());
	    } else {
	        logger.info("Conta {} atualizada com sucesso", conta.getId());
	    }
	}


//	public void atualizarTransferenciasEnviadas(Long contaId, Transferencia transferencia) {
//		Conta conta = findById(contaId).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
//		if (conta.getTransferencias() == null) {
//			conta.setTransferencias(new ArrayList<>());
//		}
//		conta.getTransferencias().add(transferencia);
//		update(conta);
//	}

//	public void atualizarSaldo(Long contaId, BigDecimal novoSaldo) {
//		Conta conta = findById(contaId).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
//		conta.setSaldoConta(novoSaldo);
//		update(conta);
//	}

}
