package br.com.marcielli.bancom.dao;

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
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.mappers.CartaoRowMapper;

@Component
public class CartaoDao {

	private final JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(CartaoDao.class);

	public CartaoDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Cartao saveWithRelations(Cartao cartao) {
		// Inserir cartão na tabela "cartoes"
		String sqlCartao = """
				    INSERT INTO cartoes
				    (tipo_conta, categoria_conta, tipo_cartao, numero_cartao, status, senha, conta_id,
				    limite_credito_pre_aprovado, limite_diario_transacao)
				    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				    RETURNING id
				""";

		Long cartaoId = jdbcTemplate.queryForObject(sqlCartao, Long.class, cartao.getTipoConta().toString(),
				cartao.getCategoriaConta().toString(), cartao.getTipoCartao().toString(), cartao.getNumeroCartao(),
				cartao.isStatus(), cartao.getSenha(), cartao.getConta().getId(),
				cartao instanceof CartaoCredito ? ((CartaoCredito) cartao).getLimiteCreditoPreAprovado() : null,
				cartao instanceof CartaoDebito ? ((CartaoDebito) cartao).getLimiteDiarioTransacao() : null);
		logger.info("cartaoDao  {}", cartao.getConta().getId());
		cartao.setId(cartaoId);

		// Inserir Fatura, se existir
		if (cartao.getFatura() != null) {
			insertFatura(cartaoId, cartao.getFatura());
		}

		// Inserir Seguros, se existirem
		if (cartao.getSeguros() != null && !cartao.getSeguros().isEmpty()) {
			insertSeguros(cartaoId, cartao.getSeguros());
		}

		return cartao;
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
	    String sql = """
	        SELECT
	            /* Campos da tabela cartoes */
	            c.id,
	            c.tipo_conta,
	            c.categoria_conta,
	            c.tipo_cartao,
	            c.numero_cartao,
	            c.status,
	            c.senha,
	            c.limite_credito_pre_aprovado,
	            c.taxa_utilizacao,
	            c.taxa_seguro_viagem,
	            c.total_gasto_mes_credito,
	            c.limite_diario_transacao,
	            c.total_gasto_mes,
	            c.conta_id,
	            c.fatura_id,
	            
	            /* Campos da tabela contas */
	            ct.id AS conta_id,
	            ct.cliente_id,
	            ct.tipo_conta AS conta_tipo_conta,
	            ct.categoria_conta AS conta_categoria_conta,
	            ct.saldo_conta AS conta_saldo,
	            ct.numero_conta,
	            ct.pix_aleatorio,
	            ct.status AS conta_status,
	            ct.taxa_manutencao_mensal,
	            ct.taxa_acresc_rend,
	            ct.taxa_mensal,
	            
	            /* Campos da tabela clientes */
	            cli.id AS cliente_id,
	            cli.nome AS cliente_nome,
	            cli.cpf AS cliente_cpf,
	            cli.cliente_ativo AS cliente_ativo,
	            cli.user_id AS cliente_user_id
	        FROM cartoes c
	        JOIN contas ct ON c.conta_id = ct.id
	        JOIN clientes cli ON ct.cliente_id = cli.id
	        WHERE c.id = ?
	    """;
	    try {
	        Cartao cartao = jdbcTemplate.queryForObject(sql, new CartaoRowMapper(), id);
	        return Optional.of(cartao);
	    } catch (EmptyResultDataAccessException e) {
	        return Optional.empty();
	    }
	}


	public void deleteCartao(Long id) {
		String sql = "DELETE FROM cartoes WHERE id = ?";
		jdbcTemplate.update(sql, id);
	}

	public List<Cartao> findAll() {
		String sql = "SELECT * FROM cartoes";
		return jdbcTemplate.query(sql, new CartaoRowMapper());
	}

	public List<Cartao> findByUsername(String username) {
		String sql = """
				    SELECT c.*
				    FROM cartoes c
				    INNER JOIN contas ct ON c.conta_id = ct.id
				    WHERE ct.username = ?
				""";
		return jdbcTemplate.query(sql, new CartaoRowMapper(), username);
	}

	public Optional<Cartao> findByIdAndUsername(Long id, String username) {
		String sql = "SELECT c.* " + "FROM cartoes c " + "JOIN contas co ON c.conta_id = co.id "
				+ "JOIN clientes cl ON co.cliente_id = cl.id " + "JOIN users u ON cl.user = u.id "
				+ "WHERE c.id = ? AND u.username = ?";

		try {
			Cartao cartao = jdbcTemplate.queryForObject(sql, new CartaoRowMapper(), id, username);
			return Optional.ofNullable(cartao);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Cartao save(Cartao cartao) {
		String sql = "UPDATE cartoes SET senha = ? WHERE id = ?";
		jdbcTemplate.update(sql, cartao.getSenha(), cartao.getId());
		return cartao;
	}

	// Método para desativar
	public boolean desativarCartao(Long id) {
		String sql = "UPDATE cartoes SET status = false WHERE id = ?";
		int rowsAffected = jdbcTemplate.update(sql, id);
		return rowsAffected > 0;
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

	public void update(Cartao cartao) {
	    if (cartao instanceof CartaoCredito cartaoCredito) {
	        updateCartaoCredito(cartaoCredito);
	    } else if (cartao instanceof CartaoDebito cartaoDebito) {
	        updateCartaoDebito(cartaoDebito);
	    } else {
	        updateCartaoBase(cartao);
	    }
	}

	private void updateCartaoCredito(CartaoCredito cartao) {
	    String sql = """
	        UPDATE cartoes
	        SET 
	            tipo_conta = ?,
	            categoria_conta = ?,
	            tipo_cartao = ?,
	            numero_cartao = ?,
	            status = ?,
	            senha = ?,
	            conta_id = ?,
	            fatura_id = ?,
	            limite_credito_pre_aprovado = ?,
	            taxa_utilizacao = ?,
	            taxa_seguro_viagem = ?,
	            total_gasto_mes_credito = ?
	        WHERE id = ?
	    """;
	    
	    jdbcTemplate.update(sql,
	    	    cartao.getTipoConta() != null ? cartao.getTipoConta().name() : null,
	    	    cartao.getCategoriaConta() != null ? cartao.getCategoriaConta().name() : null,
	    	    cartao.getTipoCartao() != null ? cartao.getTipoCartao().name() : null,
	    	    cartao.getNumeroCartao(),
	    	    cartao.isStatus(),
	    	    cartao.getSenha(),
	    	    cartao.getConta() != null ? cartao.getConta().getId() : null,
	    	    cartao.getFatura() != null ? cartao.getFatura().getId() : null,
	    	    cartao.getLimiteCreditoPreAprovado(),
	    	    cartao.getTaxaUtilizacao(),
	    	    cartao.getTaxaSeguroViagem(),
	    	    cartao.getTotalGastoMesCredito(),
	    	    cartao.getId()
	    	);
	}

	private void updateCartaoDebito(CartaoDebito cartao) {
	    String sql = """
	        UPDATE cartoes
	        SET 
	            tipo_conta = ?,
	            categoria_conta = ?,
	            tipo_cartao = ?,
	            numero_cartao = ?,
	            status = ?,
	            senha = ?,
	            conta_id = ?,
	            fatura_id = ?,
	            limite_diario_transacao = ?,
	            total_gasto_mes = ?
	        WHERE id = ?
	    """;
	    
	    jdbcTemplate.update(sql,
	        cartao.getTipoConta() != null ? cartao.getTipoConta().name() : null,
	        cartao.getCategoriaConta() != null ? cartao.getCategoriaConta().name() : null,
	        cartao.getTipoCartao() != null ? cartao.getTipoCartao().name() : null,
	        cartao.getNumeroCartao(),
	        cartao.isStatus(),
	        cartao.getSenha(),
	        cartao.getConta() != null ? cartao.getConta().getId() : null,
	        cartao.getFatura() != null ? cartao.getFatura().getId() : null,
	        cartao.getLimiteDiarioTransacao(),
	        cartao.getTotalGastoMes(),
	        cartao.getId()
	    );
	}

	private void updateCartaoBase(Cartao cartao) {
	    String sql = """
	        UPDATE cartoes
	        SET 
	            tipo_conta = ?,
	            categoria_conta = ?,
	            tipo_cartao = ?,
	            numero_cartao = ?,
	            status = ?,
	            senha = ?,
	            conta_id = ?,
	            fatura_id = ?
	        WHERE id = ?
	    """;
	    
	    jdbcTemplate.update(sql,
	        cartao.getTipoConta() != null ? cartao.getTipoConta().name() : null,  // convertendo o enum para String porque o banco não estava aceitando
	        cartao.getCategoriaConta() != null ? cartao.getCategoriaConta().name() : null,   // convertendo o enum para String
	        cartao.getTipoCartao() != null ? cartao.getTipoCartao().name() : null,   // convertendo o enum para String
	        cartao.getNumeroCartao(),
	        cartao.isStatus(),
	        cartao.getSenha(),
	        cartao.getConta() != null ? cartao.getConta().getId() : null,
	        cartao.getFatura() != null ? cartao.getFatura().getId() : null,
	        cartao.getId()
	    );
	}


}
