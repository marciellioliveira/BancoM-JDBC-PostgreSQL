package br.com.marcielli.BancoM;

import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "br.com.marcielli")
@EnableJpaRepositories(basePackages = "br.com.marcielli.BancoM.repository")
@EntityScan(basePackages = "br.com.marcielli.BancoM.entity")
@EnableScheduling
public class BancoMApplication {

	public static void main(String[] args) {
		
		// Cria a pasta logs se não existir
	    Paths.get("./logs").toFile().mkdirs();
		
		SpringApplication.run(BancoMApplication.class, args);
	}

}
