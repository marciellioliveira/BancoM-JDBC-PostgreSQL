package br.com.marcielli.bancom;

import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "br.com.marcielli")
//@EnableJpaRepositories(basePackages = "br.com.marcielli.BancoM.repository")
//@EntityScan(basePackages = "br.com.marcielli.BancoM.entity")
@EnableScheduling //(Preciso terminar de configurar, então desativei para não atrapalhar a thread principal
@EnableAsync //Precisei colocar pq o agendamento do pagamento de taxas estava bloqueando a thread principal
public class BancoMApplication {

	public static void main(String[] args) {
		
		// Cria a pasta logs se não existir
	    Paths.get("./logs").toFile().mkdirs();
		
		SpringApplication.run(BancoMApplication.class, args);
	}

}
