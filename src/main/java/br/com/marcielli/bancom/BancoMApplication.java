package br.com.marcielli.bancom;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRetry
@SpringBootApplication(scanBasePackages = "br.com.marcielli")
public class BancoMApplication {

	public static void main(String[] args) {
		
		// Cria a pasta logs se não existir
	   // Paths.get("./logs").toFile().mkdirs();
		
		SpringApplication.run(BancoMApplication.class, args);
	
	}

}
