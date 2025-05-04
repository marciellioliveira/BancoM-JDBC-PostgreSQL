package br.com.marcielli.bancom.configuracao;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import br.com.marcielli.bancom.service.ExchangeRateService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TaxaCambioScheduler {

	private final ExchangeRateService exchangeRateService;
	private static final Logger logger = LoggerFactory.getLogger(TaxaCambioScheduler.class);

	public TaxaCambioScheduler(ExchangeRateService exchangeRateService) {
		this.exchangeRateService = exchangeRateService;
	}

	// Agendado - 8h, 14h e 20h
	@Scheduled(cron = "0 0 8,14,20 * * ?", zone = "America/Sao_Paulo")
	public void verificarTaxas() {
		exchangeRateService.atualizarTaxasCache();
		logger.info("Taxas atualizadas em: " + LocalDateTime.now());
	}

}
