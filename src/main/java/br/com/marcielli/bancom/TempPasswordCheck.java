//package br.com.marcielli.bancom;
//
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//public class TempPasswordCheck {
//	public static void main(String[] args) {
//        String storedHash = "$2a$10$sKpNdB2d8A0ZXczWbjtc.OgsHRlB2Os7/F7aGBwN4WomJOL5Y6si.";
//        String rawPassword = "minhasenhasuperhipermegapowersecreta11";
//        
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        boolean matches = encoder.matches(rawPassword, storedHash);
//        
//        System.out.println("Resultado: " + (matches ? "SENHA CORRETA" : "SENHA INCORRETA"));
//    }
//}
