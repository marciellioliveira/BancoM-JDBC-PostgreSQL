//package br.com.marcielli.bancom.configuracao;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//
//@Configuration
//@EnableWebMvc
//public class CorsConfig implements WebMvcConfigurer {
//
//	@Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")  // Permite todas as rotas
//		//.allowedOriginPatterns("*")
//       	.allowedOrigins("http://localhost:5173")  // Permite apenas conexões de localhost na porta 8080
//        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Permite esses métodos HTTP
//        .allowedHeaders("Authorization", "Content-Type", "Accept")
//        .exposedHeaders("Authorization");  // Expõe o cabeçalho 'Authorization' se for necessário
//        //.allowedHeaders("*")  // Permite qualquer cabeçalho
//        //.allowCredentials(true);  // Permite enviar cookies, se necessário
//    }
//}
