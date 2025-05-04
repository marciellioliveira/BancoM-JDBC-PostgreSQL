package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.mappers.ContasRowMapper;

import org.springframework.data.mapping.AccessOptions.SetOptions.Propagation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
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
	            conta.getCategoriaConta().name(), // Convertendo enum para String
	            cc.getTaxaManutencaoMensal(),
	            null, 
	            null,
	            conta.getId()
	        };
	    } else {
	        ContaPoupanca cp = (ContaPoupanca) conta;
	        params = new Object[]{
	            conta.getSaldoConta(),
	            conta.getCategoriaConta().name(), // Convertendo enum para String
	            null,
	            cp.getTaxaAcrescRend(),
	            cp.getTaxaMensal(),
	            conta.getId()
	        };
	    }
	    
	    jdbcTemplate.update(sql, params);
	}
	
	
	
	
	

}
