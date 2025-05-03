//package br.com.marcielli.bancom;
//
//import javax.crypto.SecretKey;
//
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.io.Encoders;
//import io.jsonwebtoken.security.Keys;
//
//public class JwtConfig {
//	public static void main(String[] args) {
//        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//        System.out.println("Sua chave segura: " + Encoders.BASE64.encode(key.getEncoded()));
//    }
//}
