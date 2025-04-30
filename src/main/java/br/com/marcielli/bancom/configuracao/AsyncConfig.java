//package br.com.marcielli.bancom.configuracao;
//
//import java.util.concurrent.Executor;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//public class AsyncConfig {
//
//	//Para evitar sobrecarga precisei criar um "pool de threads dedicado" pelo que entendi, isso ajuda muito a não travar e as vezes até parar o software.
//
//	@Bean(name = "taskExecutor")
//    public Executor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);  // Número mínimo de threads
//        executor.setMaxPoolSize(10);  // Máximo de threads
//        executor.setQueueCapacity(100);  // Tamanho da fila de espera
//        executor.setThreadNamePrefix("AsyncTaxa-");  // Prefixo para identificação
//        executor.initialize();
//        return executor;
//    }
//
//} //(Preciso terminar de configurar, então desativei para não atrapalhar a thread principal
