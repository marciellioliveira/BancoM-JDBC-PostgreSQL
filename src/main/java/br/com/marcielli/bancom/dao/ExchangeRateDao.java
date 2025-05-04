package br.com.marcielli.bancom.dao;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.exception.TaxaDeCambioException;

@Component
public class ExchangeRateDao {

	private final JdbcTemplate jdbcTemplate;

	public ExchangeRateDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public BigDecimal getTaxaCambio(String moedaOrigem, String moedaDestino) {
        String sql = """
                SELECT taxa FROM taxas_cambio
                WHERE moeda_origem = ? AND moeda_destino = ?
                ORDER BY data_atualizacao DESC
                LIMIT 1
            """;

        try {
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, moedaOrigem, moedaDestino);
        } catch (EmptyResultDataAccessException e) {
            throw new TaxaDeCambioException("Taxa não encontrada para: " + moedaOrigem + "->" + moedaDestino);
        }
    }

	public void salvarTaxas(String moedaOrigem, Map<String, Double> taxas) {
	    Objects.requireNonNull(moedaOrigem, "Moeda origem não pode ser nula");
	    Objects.requireNonNull(taxas, "Taxas não podem ser nulas");

	    jdbcTemplate.update("DELETE FROM taxas_cambio WHERE moeda_origem = ?", moedaOrigem);

	    if (!taxas.isEmpty()) {
	        String sql = "INSERT INTO taxas_cambio (moeda_origem, moeda_destino, taxa) VALUES (?, ?, ?)";
	        jdbcTemplate.batchUpdate(sql, taxas.entrySet().stream()
	            .map(entry -> new Object[]{moedaOrigem, entry.getKey(), entry.getValue()})
	            .collect(Collectors.toList()));
	    }
	}

}
