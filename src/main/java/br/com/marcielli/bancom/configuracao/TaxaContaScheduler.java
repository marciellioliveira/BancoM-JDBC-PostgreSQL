package br.com.marcielli.bancom.configuracao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.dao.ContaDao;

@Component
public class TaxaContaScheduler {
	
	private final ContaDao contaDao;
    private static final Logger log = LoggerFactory.getLogger(TaxaContaScheduler.class);
    private static final int BATCH_SIZE = 100; //Quantidade de contas a serem processadas por vez para evitar sobrecarga

    public TaxaContaScheduler(ContaDao contaDao) {
		this.contaDao = contaDao;
	}

	// Taxas de manutenção - Dia 1 de cada mês às 2h
    @Scheduled(cron = "0 0 2 1 * *", zone = "America/Sao_Paulo")
    //@Scheduled(cron = "*/10 * * * * *", zone = "America/Sao_Paulo")  // se quiser testar a cada 10 segundos
    public void aplicarTaxasMensais() {
    	log.info("Aplicando taxas mensais em lotes de {} contas...", BATCH_SIZE);
        contaDao.aplicarTaxaManutencaoEmLotes(BATCH_SIZE);
    }

    // Rendimentos - Diário às 23h50
    //@Scheduled(cron = "0 50 23 * * *", zone = "America/Sao_Paulo")
     @Scheduled(cron = "*/10 * * * * *", zone = "America/Sao_Paulo")  // se quiser testar a cada 10 segundos
    public void aplicarRendimentosDiarios() {
    	log.info("Aplicando rendimentos diários em lotes de {} contas...", BATCH_SIZE);
        contaDao.aplicarRendimentoEmLotes(BATCH_SIZE);
    }

}
