//package br.com.marcielli.bancom;
//
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//public class TempPasswordCheck {
//	public static void main(String[] args) {
//        String storedHash = "$2a$10$4P1e/egbRpMUaYTuZ5arQeebrX20PRgws7XNI0DJfsM1PU1800M2q";
//        String rawPassword = "minhasenhasuperhipermegapowersecreta11";
//        
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        boolean matches = encoder.matches(rawPassword, storedHash);
//        
//        System.out.println("Resultado: " + (matches ? "SENHA CORRETA" : "SENHA INCORRETA"));
//    }
//}
