package br.com.marcielli.bancom.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import br.com.marcielli.bancom.exception.TaxaDeCambioException;

@Service
@Transactional
public class ExchangeRateService {

	private final RestTemplate restTemplate;
	private final String apiKey;
	private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

	public ExchangeRateService(RestTemplate restTemplate, @Value("${exchangerate.api.key}") String apiKey) {
		this.restTemplate = restTemplate;
		this.apiKey = apiKey;
	}

	@Cacheable(value = "taxas", key = "{#moedaOrigem, #moedaDestino}")
	public BigDecimal converterMoeda(BigDecimal valor, String moedaOrigem, String moedaDestino) {
		try {
			BigDecimal taxa = getTaxaCambio(moedaOrigem, moedaDestino);
			BigDecimal valorConvertido = valor.multiply(taxa).setScale(2, RoundingMode.HALF_UP);

			logger.debug("Conversão realizada: {} {} → {} {} = {}", valor, moedaOrigem, valorConvertido, moedaDestino,
					taxa);

			return valorConvertido;
		} catch (Exception e) {
			logger.warn("Falha na conversão, tentando usar cache...", e);
			return BigDecimal.ZERO;
		}
	}

	@CacheEvict(value = "taxas", allEntries = true)
	public void atualizarTaxasCache() {
		logger.info("Cache de taxas limpo em {}", LocalDateTime.now());
	}

	@Cacheable(value = "taxas", key = "{#moedaOrigem, #moedaDestino}")
	private BigDecimal getTaxaCambio(String moedaOrigem, String moedaDestino) {
		logger.info("Buscando taxa nova: {} → {}", moedaOrigem, moedaDestino);

		String url = String.format("https://v6.exchangerate-api.com/v6/%s/pair/%s/%s", apiKey, moedaOrigem,
				moedaDestino);

		Map<String, Object> response = restTemplate.getForObject(url, Map.class);

		if (response == null || !"success".equals(response.get("result"))) {
			throw new TaxaDeCambioException("Falha ao obter taxa de câmbio");
		}

		return BigDecimal.valueOf((Double) response.get("conversion_rate"));
	}

}