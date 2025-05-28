package br.com.marcielli.bancom.dao;

import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
import br.com.marcielli.bancom.mappers.TransferenciaRowMapper;

@Component
public class TransferenciaDao {

	private final JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(TransferenciaDao.class);

	public TransferenciaDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Long save(Transferencia transferencia) {
		String sql = "SELECT salvar_transferencia_v1(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Long faturaId = null;
		if (transferencia.getFatura() != null) {
			faturaId = transferencia.getFatura().getId();
		}

		return jdbcTemplate.queryForObject(sql, Long.class, transferencia.getIdClienteOrigem(),
				transferencia.getIdClienteDestino(), transferencia.getIdContaOrigem(),
				transferencia.getIdContaDestino(), transferencia.getTipoTransferencia().name(),
				transferencia.getValor(), Timestamp.valueOf(transferencia.getData()), transferencia.getCodigoOperacao(),
				transferencia.getTipoCartao() != null ? transferencia.getTipoCartao().name() : null,
				transferencia.getIdCartao(), faturaId);
	}

	public List<Transferencia> findByCartaoId(Long cartaoId) {
		String sql = "SELECT * FROM transferencias WHERE id_cartao = ? " + "AND tipo_transferencia = 'CARTAO_CREDITO' "
				+ "ORDER BY data DESC";
		return jdbcTemplate.query(sql, new TransferenciaRowMapper(), cartaoId);
	}

	public void associarTransferenciaAFatura(Long faturaId, Long transferenciaId) {
		String sql = "INSERT INTO fatura_transferencias (fatura_id, transferencia_id) VALUES (?, ?)";
		jdbcTemplate.update(sql, faturaId, transferenciaId);
	}

	public List<Transferencia> findAll() {
		String sql = "SELECT * FROM transferencias";
		return jdbcTemplate.query(sql, new TransferenciaRowMapper());
	}

	public Transferencia findById(Long id) {
		String sql = "SELECT * FROM transferencias WHERE id = ?";
		return jdbcTemplate.queryForObject(sql, new TransferenciaRowMapper(), id);
	}

	public List<Transferencia> findTransferenciasEnviadasByContaId(Long contaId) {
		String sql = "SELECT * FROM transferencias WHERE id_conta_origem = ? ORDER BY data DESC";
		return jdbcTemplate.query(sql, new TransferenciaRowMapper(), contaId);
	}

	public List<Transferencia> findByFaturaId(Long faturaId) {
		String sql = """
				    SELECT id, valor, data, fatura_id
				    FROM transferencias
				    WHERE fatura_id = ?
				""";

		return jdbcTemplate.query(sql, new TransferenciaRowMapper(), faturaId);
	}

	public List<Transferencia> findCreditoByFaturaIdUsingJoin(Long faturaId) {
		String sql = """
				SELECT t.id, t.valor, t.data, t.fatura_id,
				       NULL AS id_cliente_origem, NULL AS id_cliente_destino,
				       NULL AS id_conta_origem, NULL AS id_conta_destino,
				       t.tipo_transferencia, t.codigo_operacao, t.tipo_cartao, t.id_cartao
				FROM transferencias t
				INNER JOIN fatura_transferencias ft ON t.id = ft.transferencia_id
				WHERE ft.fatura_id = ? AND t.tipo_transferencia = 'CARTAO_CREDITO'
				ORDER BY t.data DESC
				""";
		logger.info("Buscando transferências de crédito para fatura ID: {}", faturaId);
		List<Transferencia> transferencias = jdbcTemplate.query(sql, new TransferenciaRowMapper(), faturaId);
		logger.info("Transferências de crédito encontradas: {}", transferencias.size());
		return transferencias;
	}

	public void update(Transferencia transferencia) {
		String sql = """
				    UPDATE transferencias SET
				        id_cliente_origem = ?,
				        id_cliente_destino = ?,
				        id_conta_origem = ?,
				        id_conta_destino = ?,
				        tipo_transferencia = ?,
				        valor = ?,
				        data = ?,
				        codigo_operacao = ?,
				        tipo_cartao = ?,
				        id_cartao = ?,
				        fatura_id = ?
				    WHERE id = ?
				""";

		jdbcTemplate.update(sql, transferencia.getIdClienteOrigem(), transferencia.getIdClienteDestino(),
				transferencia.getIdContaOrigem(), transferencia.getIdContaDestino(),
				transferencia.getTipoTransferencia() != null ? transferencia.getTipoTransferencia().name() : null,
				transferencia.getValor(), Timestamp.valueOf(transferencia.getData()), transferencia.getCodigoOperacao(),
				transferencia.getTipoCartao() != null ? transferencia.getTipoCartao().name() : null,
				transferencia.getIdCartao(),
				transferencia.getFatura() != null ? transferencia.getFatura().getId() : null, transferencia.getId());
	}

}