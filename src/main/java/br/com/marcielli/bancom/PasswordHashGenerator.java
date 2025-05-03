//package br.com.marcielli.bancom;
//
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//public class PasswordHashGenerator {
//
//	public static void main(String[] args) {
//        String rawPassword = "minhasenhasuperhipermegapowersecreta11";
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String newHash = encoder.encode(rawPassword);
//        
//        System.out.println("=== NOVO HASH BCrypt ===");
//        System.out.println("Senha: " + rawPassword);
//        System.out.println("Hash: " + newHash);
//        System.out.println("Comando SQL para atualizar:");
//        System.out.println("UPDATE users SET password = '" + newHash + "' WHERE username = 'admin';");
//    }
//}
