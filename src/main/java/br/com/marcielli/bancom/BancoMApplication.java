package br.com.marcielli.bancom;

import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.marcielli")
public class BancoMApplication {

	public static void main(String[] args) {
		
		// Cria a pasta logs se não existir
	   // Paths.get("./logs").toFile().mkdirs();
		
		SpringApplication.run(BancoMApplication.class, args);
	
	}

}
