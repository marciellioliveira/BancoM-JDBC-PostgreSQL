package br.com.marcielli.BancoM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "br.com.marcielli")
@EnableJpaRepositories(basePackages = "br.com.marcielli.BancoM.repository")
@EntityScan(basePackages = "br.com.marcielli.BancoM.entity")
public class BancoMApplication {

	public static void main(String[] args) {
		SpringApplication.run(BancoMApplication.class, args);
	}

}
