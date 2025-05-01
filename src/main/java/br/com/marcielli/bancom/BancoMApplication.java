package br.com.marcielli.bancom;

import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(scanBasePackages = "br.com.marcielli")
//@EnableScheduling //(Preciso terminar de configurar, então desativei para não atrapalhar a thread principal
//@EnableAsync //Precisei colocar pq o agendamento do pagamento de taxas estava bloqueando a thread principal
@SpringBootApplication(scanBasePackages = "br.com.marcielli")
public class BancoMApplication {

	public static void main(String[] args) {
		System.out.println("Funcionou!"); // Exemplo
		// Cria a pasta logs se não existir
	    Paths.get("./logs").toFile().mkdirs();
		
		SpringApplication.run(BancoMApplication.class, args);
		System.out.println("Funcionou!"); // Exemplo
	}

}
