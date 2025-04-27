

package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import br.com.marcielli.BancoM.dto.security.UserContaResponseDTO;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.repository.ContaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AgendarAplicacaoTaxasContasService {
    
    private static final Logger log = LoggerFactory.getLogger(AgendarAplicacaoTaxasContasService.class);

    @Autowired
    private ContaRepository contaRepository;

    // Agendamento para contas corrente (teste: a cada 3 minutos)
    @Async
    @Scheduled(fixedRate = 180000)
    public void aplicarTaxaManutencaoMensal() {
        log.info("Iniciando aplicação de taxas mensais de manutenção...");
        List<ContaCorrente> contas = buscarTodasContasCorrentesAtivas();
        log.info("Processando {} contas corrente", contas.size());
        
        contas.forEach(conta -> {
            try {
                manutencaoTaxaCC(conta.getId());
            } catch (Exception e) {
                log.error("Erro na conta {}: {}", conta.getId(), e.getMessage());
            }
        });
    }

    // Agendamento para contas poupança (teste: a cada 3 minutos)
    @Async
    @Scheduled(fixedRate = 180000)
    public void aplicarRendimentosPoupancaTodoDia() {
        log.info("Iniciando aplicação diária de rendimentos...");
        List<ContaPoupanca> contas = buscarTodasContasPoupancaAtivas();
        log.info("Processando {} contas poupança", contas.size());
        
        contas.forEach(conta -> {
            try {
                rendimentoTaxaCP(conta.getId());
            } catch (Exception e) {
                log.error("Erro na conta {}: {}", conta.getId(), e.getMessage());
            }
        });
    }

    // Métodos auxiliares
    public List<ContaCorrente> buscarTodasContasCorrentesAtivas() {
        return contaRepository.findAll().stream()
                .filter(conta -> conta instanceof ContaCorrente && conta.getStatus())
                .map(conta -> (ContaCorrente) conta)
                .collect(Collectors.toList());
    }

    public List<ContaPoupanca> buscarTodasContasPoupancaAtivas() {
        return contaRepository.findAll().stream()
                .filter(conta -> conta instanceof ContaPoupanca && conta.getStatus())
                .map(conta -> (ContaPoupanca) conta)
                .collect(Collectors.toList());
    }

    public Conta rendimentoTaxaCP(Long idConta) {
        ContaPoupanca conta = (ContaPoupanca) contaRepository.findById(idConta)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada"));
        
        if (!conta.getStatus()) {
            log.warn("Conta poupança inativa - ID: {}", idConta);
            return conta;
        }

        BigDecimal rendimento = conta.getSaldoConta().multiply(conta.getTaxaAcrescRend());
        conta.creditar(rendimento);
        return contaRepository.save(conta);
    }

    public Conta manutencaoTaxaCC(Long idConta) {
        ContaCorrente conta = (ContaCorrente) contaRepository.findById(idConta)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Conta não encontrada"));
        
        if (!conta.getStatus()) {
            log.warn("Conta corrente inativa - ID: {}", idConta);
            return conta;
        }

        conta.debitar(conta.getTaxaManutencaoMensal());
        return contaRepository.save(conta);
    }

    // Métodos para controle externo
    public ResponseEntity<?> processarOperacaoConta(Long idConta, Function<Long, Conta> operacao) {
        try {
            Conta contaAtualizada = operacao.apply(idConta);
            return ResponseEntity.ok(converterParaDTO(contaAtualizada));
        } catch (ClienteNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    public UserContaResponseDTO converterParaDTO(Conta conta) {
        UserContaResponseDTO response = new UserContaResponseDTO();
        response.setId(conta.getId());
        response.setTipoConta(conta.getTipoConta());
        response.setCategoriaConta(conta.getCategoriaConta());
        response.setSaldoConta(conta.getSaldoConta());
        response.setNumeroConta(conta.getNumeroConta());
        response.setPixAleatorio(conta.getPixAleatorio());
        response.setStatus(conta.getStatus());

        if (conta instanceof ContaCorrente) {
            response.setTaxaManutencaoMensal(((ContaCorrente) conta).getTaxaManutencaoMensal());
        } else if (conta instanceof ContaPoupanca) {
            response.setTaxaAcrescRend(((ContaPoupanca) conta).getTaxaAcrescRend());
        }

        return response;
    }
}








//package br.com.marcielli.BancoM.service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import br.com.marcielli.BancoM.entity.ContaCorrente;
//import br.com.marcielli.BancoM.entity.ContaPoupanca;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Service
//public class AgendarAplicacaoTaxasContasService {
//	
//	//Para testar: @Scheduled(cron = "0 */5 * * * ?") // A cada 5 minutos
//
//	@Autowired
//    private UserContaService contaService;
//	
//	
//	//O Logger é da biblioteca de logging dp Spring. SLF4J (Simple Logging Facade for Java)
//	private static final Logger log = LoggerFactory.getLogger(AgendarAplicacaoTaxasContasService.class);
//	
//	//Todo mês no dia 1 as 2 da manhã esse método foi agendado para ser executado.
//	//@Scheduled(cron = "0 0 2 1 * ?")
//	@Async
//	@Scheduled(fixedRate = 180000) //Testando a cada 3 min
//	public void aplicarTaxaManutencaoMensal() {
//		
//		log.info("Iniciando aplicação de taxas mensais de manutenção...");
//        List<ContaCorrente> contasCorrente = contaService.buscarTodasContasCorrentesAtivas();
//        log.info("{} contas corrente serão processadas", contasCorrente.size());
//        
//        contasCorrente.forEach(conta -> {
//        	
//        	try {
//            contaService.manutencaoTaxaCC(conta.getId());
//        	} catch (Exception e) {
//               log.error("Falha ao processar conta {}: {}", conta.getId(), e.getMessage());
//            }
//        });
//    }
//	
//	//Todo dia esse método é executado as 23 horas
//	//@Scheduled(cron = "0 0 23 * * ?")
//	@Async
//	@Scheduled(fixedRate = 180000) //Testando a cada 3 min
//	public void aplicarRendimentosPoupancaTodoDia() {
//		
//		log.info("Iniciando aplicação diária de rendimentos...");
//		List<ContaPoupanca> contasPoupanca = contaService.buscarTodasContasPoupancaAtivas();
//		log.info("{} contas poupança serão processadas", contasPoupanca.size());
//		
//			contasPoupanca.forEach(conta -> { //Para cada conta, faça.
//				
//				try {
//				contaService.rendimentoTaxaCP(conta.getId());
//				} catch (Exception e) {
//			      log.error("Falha ao processar conta {}: {}", conta.getId(), e.getMessage());
//			    }
//			});
//		}
//	
//	
//	
//	
//	
//	
//	// Agendamento de Taxas de Conta Corrente utilizando o @Scheduled(cron = "0 0 2
//		// 1 * ?") do Spring (ROTAS DE AGENDAMENTO AUTOMATICO- ARRUMAR)
//		public List<ContaCorrente> buscarTodasContasCorrentesAtivas() {
//			return contaRepository.findAll().stream().filter(conta -> conta instanceof ContaCorrente && conta.getStatus())
//					.map(conta -> (ContaCorrente) conta).collect(Collectors.toList());
//	
//		}
//	
//		// Agendamento de Taxas de Conta Poupança utilizando o @Scheduled(cron = "0 0 23
//		// * *") do Spring
//		public List<ContaPoupanca> buscarTodasContasPoupancaAtivas() {
//			return contaRepository.findAll().stream().filter(conta -> conta instanceof ContaPoupanca && conta.getStatus())
//					.map(conta -> (ContaPoupanca) conta).collect(Collectors.toList());
//	
//		}
//	
//		public Conta rendimentoTaxaCP(Long idConta) {
//			log.debug("Iniciando aplicação de rendimento para conta {}", idConta);
//	
//			ContaPoupanca conta = (ContaPoupanca) contaRepository.findById(idConta).orElseThrow(() -> {
//				log.error("Conta poupança {} não encontrada", idConta);
//				return new ClienteNaoEncontradoException("Conta não encontrada");
//			});
//	
//			if (!conta.getStatus()) {
//				log.warn("Tentativa de aplicar rendimento em conta poupança inativa - ID: {}", idConta);
//	
//			}
//	
//			BigDecimal rendimento = calcularRendimentoCP(conta);
//			conta.creditar(rendimento);
//	
//			log.info("Rendimento de {} aplicado na conta poupança {}", rendimento, idConta);
//			return contaRepository.save(conta);
//		}
//	
//		private BigDecimal calcularRendimentoCP(ContaPoupanca conta) {
//	
//			log.trace("Calculando rendimento para conta {}", conta.getId());
//			return conta.getSaldoConta().multiply(conta.getTaxaAcrescRend());
//		}
//	
//		public Conta manutencaoTaxaCC(Long idConta) {
//			log.debug("Iniciando cobrança de manutenção para conta {}", idConta);
//	
//			ContaCorrente conta = (ContaCorrente) contaRepository.findById(idConta).orElseThrow(() -> {
//				log.error("Conta corrente {} não encontrada", idConta);
//				return new ClienteNaoEncontradoException("Conta não encontrada");
//			});
//	
//			if (!conta.getStatus()) {
//				log.warn("Tentativa de cobrar taxa em conta corrente inativa - ID: {}", idConta);
//	
//			}
//	
//			BigDecimal taxa = calcularAcrescimoTaxaCC(conta);
//			conta.debitar(taxa);
//	
//			log.info("Taxa de manutenção de {} debitada da conta {}", taxa, idConta);
//			return contaRepository.save(conta);
//		}
//	
//		private BigDecimal calcularAcrescimoTaxaCC(ContaCorrente conta) {
//			// Lógica de cálculo da taxa
//			log.trace("Calculando taxa para conta {}", conta.getId());
//			return conta.getTaxaManutencaoMensal();
//		}
//	
//		public ResponseEntity<?> processarOperacaoConta(Long idConta, Function<Long, Conta> operacao) {
//			try {
//				Conta contaAtualizada = operacao.apply(idConta);
//				return ResponseEntity.ok(converterParaDTO(contaAtualizada));
//			} catch (ClienteNaoEncontradoException e) {
//				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//			}
//		}
//	
//		public UserContaResponseDTO converterParaDTO(Conta conta) {
//			UserContaResponseDTO response = new UserContaResponseDTO();
//			response.setId(conta.getId());
//			response.setTipoConta(conta.getTipoConta());
//			response.setCategoriaConta(conta.getCategoriaConta());
//			if (conta instanceof ContaCorrente contaCorrente) {
//				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
//			}
//	
//			if (conta instanceof ContaPoupanca contaPoupanca) {
//				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
//				response.setTaxaMensal(contaPoupanca.getTaxaMensal());
//			}
//	
//			response.setSaldoConta(conta.getSaldoConta());
//			response.setNumeroConta(conta.getNumeroConta());
//			response.setPixAleatorio(conta.getPixAleatorio());
//			response.setStatus(conta.getStatus());
//			return response;
//		}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	}
//	