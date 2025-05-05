package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.exception.ChavePixNaoEncontradaException;
import br.com.marcielli.bancom.exception.ContaNaoEncontradaException;
import br.com.marcielli.bancom.exception.TaxaDeCambioException;
import br.com.marcielli.bancom.mappers.ContaCorrenteRowMapper;
import br.com.marcielli.bancom.mappers.ContasRowMapper;
import br.com.marcielli.bancom.mappers.TaxaCambioRowMapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mapping.AccessOptions.SetOptions.Propagation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ContaDao {

	private final JdbcTemplate jdbcTemplate;

	public ContaDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Conta save(Conta conta) {
		KeyHolder keyHolder = new GeneratedKeyHolder();

		String sql = """
				    INSERT INTO contas (
				        cliente_id, tipo_conta, categoria_conta, saldo_conta,
				        numero_conta, pix_aleatorio, status,
				        taxa_manutencao_mensal, taxa_acresc_rend, taxa_mensal
				    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";

		jdbcTemplate.update(connection -> {

			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			ps.setLong(1, conta.getCliente().getId());
			ps.setString(2, conta.getTipoConta().name());
			ps.setString(3, conta.getCategoriaConta().name());
			ps.setBigDecimal(4, conta.getSaldoConta());
			ps.setString(5, conta.getNumeroConta());
			ps.setString(6, conta.getPixAleatorio());
			ps.setBoolean(7, conta.getStatus());

			switch (conta) {
			case ContaCorrente cc -> {
				ps.setBigDecimal(8, cc.getTaxaManutencaoMensal());
				ps.setNull(9, Types.NUMERIC);
				ps.setNull(10, Types.NUMERIC);
			}
			case ContaPoupanca cp -> {
				ps.setNull(8, Types.NUMERIC);
				ps.setBigDecimal(9, cp.getTaxaAcrescRend());
				ps.setBigDecimal(10, cp.getTaxaMensal());
			}
			default -> {
				ps.setNull(8, Types.NUMERIC);
				ps.setNull(9, Types.NUMERIC);
				ps.setNull(10, Types.NUMERIC);
			}
			}

			return ps;
		}, keyHolder);

		// Utilizando getKeys() para acessar todas as chaves geradas
		Map<String, Object> keys = keyHolder.getKeys();
		if (keys != null && !keys.isEmpty()) {
			Number generatedId = (Number) keys.get("id");
			if (generatedId != null) {
				conta.setId(generatedId.longValue());
			}
		}

		return conta;
	}


	public List<Conta> findAll() {
	    String sql = """
	        SELECT
	            c.*,
	            cl.id AS cliente_id,
	            cl.nome AS cliente_nome
	        FROM contas c
	        JOIN clientes cl ON c.cliente_id = cl.id
	    """;
	    return jdbcTemplate.query(sql, new ContasRowMapper());
	}

	public List<Conta> findByUsername(String username) {
	    String sql = """
	        SELECT
	            c.*,
	            cl.id AS cliente_id,
	            cl.nome AS cliente_nome
	        FROM contas c
	        JOIN clientes cl ON c.cliente_id = cl.id
	        JOIN users u ON cl.user_id = u.id
	        WHERE u.username = ?
	    """;
	    return jdbcTemplate.query(sql, new ContasRowMapper(), username);
	}

	public Optional<Conta> findByIdAndUsername(Long id, String username) {
	    String sql = """
	        SELECT
	            c.*,
	            cl.id AS cliente_id,
	            cl.nome AS cliente_nome,
	            u.id AS user_id,
	            u.username
	        FROM contas c
	        JOIN clientes cl ON c.cliente_id = cl.id
	        JOIN users u ON cl.user_id = u.id
	        WHERE c.id = ? AND u.username = ?
	    """;

	    List<Conta> contas = jdbcTemplate.query(sql, new ContasRowMapper(), id, username);
	    return contas.isEmpty() ? Optional.empty() : Optional.of(contas.get(0));
	}

	public Optional<Conta> findById(Long id) {
	    String sql = """
	        SELECT
	            c.*,
	            cl.id AS cliente_id,
	            cl.nome AS cliente_nome,
	            u.id AS user_id,
	            u.username
	        FROM contas c
	        JOIN clientes cl ON c.cliente_id = cl.id
	        JOIN users u ON cl.user_id = u.id
	        WHERE c.id = ?
	    """;

	    List<Conta> contas = jdbcTemplate.query(sql, new ContasRowMapper(), id);
	    return contas.isEmpty() ? Optional.empty() : Optional.of(contas.get(0));
	}
	
	public void atualizarPixAleatorio(Long idConta, String novoPix) {
	    String sql = "UPDATE contas SET pix_aleatorio = ? WHERE id = ?";
	    jdbcTemplate.update(sql, novoPix, idConta);
	}
	

	public void updateSaldo(Conta conta) {
	    String sql = "UPDATE contas SET saldo_conta = ?, categoria_conta = ?, " +
	                 "taxa_manutencao_mensal = ?, taxa_acresc_rend = ?, taxa_mensal = ? " +
	                 "WHERE id = ?";
	    
	    Object[] params;
	    
	    if(conta instanceof ContaCorrente cc) {
	        params = new Object[]{
	            conta.getSaldoConta(),
	            conta.getCategoriaConta().name(),
	            cc.getTaxaManutencaoMensal(),
	            null, 
	            null,
	            conta.getId()
	        };
	    } else {
	        ContaPoupanca cp = (ContaPoupanca) conta;
	        params = new Object[]{
	            conta.getSaldoConta(),
	            conta.getCategoriaConta().name(),
	            null,
	            cp.getTaxaAcrescRend(),
	            cp.getTaxaMensal(),
	            conta.getId()
	        };
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
        
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
            sql, 
            Boolean.class, 
            contaId, 
            username
        ));
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
	    
	    jdbcTemplate.update(sql,
	        cc.getSaldoConta(),
	        cc.getCategoriaConta().name(),
	        cc.getTaxaManutencaoMensal(),
	        cc.getId());
	}
	
	

}
