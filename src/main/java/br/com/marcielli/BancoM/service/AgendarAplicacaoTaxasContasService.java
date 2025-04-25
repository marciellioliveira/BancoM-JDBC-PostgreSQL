package br.com.marcielli.BancoM.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendarAplicacaoTaxasContasService {
	
	//Para testar: @Scheduled(cron = "0 */5 * * * ?") // A cada 5 minutos

	@Autowired
    private UserContaService contaService;
	
	//O Logger é da biblioteca de logging dp Spring. SLF4J (Simple Logging Facade for Java)
	private static final Logger log = LoggerFactory.getLogger(AgendarAplicacaoTaxasContasService.class);
	
	//Todo mês no dia 1 as 2 da manhã esse método foi agendado para ser executado.
	//@Scheduled(cron = "0 0 2 1 * ?")
	@Scheduled(cron = "0 */5 * * * ?")
	public void aplicarTaxaManutencaoMensal() {
		
		log.info("Iniciando aplicação de taxas mensais de manutenção...");
        List<ContaCorrente> contasCorrente = contaService.buscarTodasContasCorrentesAtivas();
        log.info("{} contas corrente serão processadas", contasCorrente.size());
        
        contasCorrente.forEach(conta -> {
        	
        	try {
            contaService.manutencaoTaxaCC(conta.getId());
        	} catch (Exception e) {
               log.error("Falha ao processar conta {}: {}", conta.getId(), e.getMessage());
            }
        });
    }
	
	//Todo dia esse método é executado as 23 horas
	//@Scheduled(cron = "0 0 23 * * ?")
	@Scheduled(cron = "0 */5 * * * ?")
	public void aplicarRendimentosPoupancaTodoDia() {
		
		log.info("Iniciando aplicação diária de rendimentos...");
		List<ContaPoupanca> contasPoupanca = contaService.buscarTodasContasPoupancaAtivas();
		log.info("{} contas poupança serão processadas", contasPoupanca.size());
		
			contasPoupanca.forEach(conta -> { //Para cada conta, faça.
				
				try {
				contaService.rendimentoTaxaCP(conta.getId());
				} catch (Exception e) {
			      log.error("Falha ao processar conta {}: {}", conta.getId(), e.getMessage());
			    }
			});
		}
	
	}
	