package br.com.marcielli.BancoM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.marcielli")
public class BancoMApplication {

	public static void main(String[] args) {
		SpringApplication.run(BancoMApplication.class, args);
	}

}
