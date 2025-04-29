package br.com.marcielli.bancom;

import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

//@SpringBootApplication(scanBasePackages = "br.com.marcielli")
//@EnableScheduling //(Preciso terminar de configurar, então desativei para não atrapalhar a thread principal
//@EnableAsync //Precisei colocar pq o agendamento do pagamento de taxas estava bloqueando a thread principal
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan(basePackages = "br.com.marcielli.bancom")
public class BancoMApplication {

	public static void main(String[] args) {
		
		// Cria a pasta logs se não existir
	    Paths.get("./logs").toFile().mkdirs();
		
		SpringApplication.run(BancoMApplication.class, args);
	}

}
