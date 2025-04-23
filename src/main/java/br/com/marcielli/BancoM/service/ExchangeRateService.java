package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;

import java.math.RoundingMode;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.com.marcielli.BancoM.dto.security.ConversionResponseDTO;
import br.com.marcielli.BancoM.exception.TaxaDeCambioException;

@Service
public class ExchangeRateService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${exchangerate.api.key}")
	private String apiKey;

    public ConversionResponseDTO convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        try {
            String url = String.format("https://v6.exchangerate-api.com/v6/"+apiKey+"/latest/%s", fromCurrency);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            System.out.println("Resposta da API: " + body);

            if (body != null && "success".equals(body.get("result"))) {
                Map<String, Object> conversionData = (Map<String, Object>) body.get("conversion_rates");
                Double rate = (Double) conversionData.get(toCurrency);
                BigDecimal convertedAmount = amount.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
                return new ConversionResponseDTO(amount, fromCurrency, toCurrency, convertedAmount, rate);
            } else {
                throw new TaxaDeCambioException("Erro ao obter taxa de câmbio para " + fromCurrency + " -> " + toCurrency);
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter a taxa de câmbio: " + e.getMessage());
            throw new TaxaDeCambioException("Erro ao obter taxa de câmbio para " + fromCurrency + " -> " + toCurrency);
        }
    }



}
