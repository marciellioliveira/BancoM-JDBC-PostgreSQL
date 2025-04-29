package br.com.marcielli.bancom.validation;

import org.springframework.context.annotation.Profile;

@Profile("cliente")
public class ValidadorCPF {

	public static boolean validar(String cpf) {
        
        cpf = cpf.replaceAll("[^0-9]", "");

        
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
           
            int digito1 = calcularDigito(cpf.substring(0, 9));
            
            
            int digito2 = calcularDigito(cpf.substring(0, 9) + digito1);
            
           
            return cpf.equals(cpf.substring(0, 9) + digito1 + digito2);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int calcularDigito(String parte) {
        int soma = 0;
        int peso = parte.length() + 1;
        
        for (int i = 0; i < parte.length(); i++) {
            soma += Integer.parseInt(parte.substring(i, i + 1)) * peso--;
        }
        
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
    
    public static boolean validarFormato(String cpf) {
        return cpf.matches("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$");
    }


//    
//    public static void main(String[] args) {
//        System.out.println(validar("529.982.247-25")); // true (CPF válido)
//        System.out.println(validar("111.111.111-11")); // false (inválido)
//        System.out.println(validar("123.456.789-09")); // false (inválido)
//    }
}
