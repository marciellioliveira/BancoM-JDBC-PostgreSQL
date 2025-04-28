package br.com.marcielli.BancoM;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.enuns.TipoConta;

@SpringBootTest
public class TaxaManutencaoTest {
	
	@Test
    public void testTaxaPoupancaPremium() {
        TaxaManutencao taxaPremium = new TaxaManutencao(new BigDecimal("6000"), TipoConta.POUPANCA);
        System.out.println("Premium - Taxa Anual: " + taxaPremium.getTaxaAcrescRend());
        System.out.println("Premium - Taxa Mensal: " + taxaPremium.getTaxaMensal());
        
        BigDecimal taxaMensalPercent = taxaPremium.getTaxaMensal().multiply(new BigDecimal("100"));
        System.out.println("Premium - Taxa Mensal (%): " + taxaMensalPercent);
    }

}
