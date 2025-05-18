package br.com.marcielli.bancom.dto.security;

public record UserCreateDTO(String username, String password, String nome, String cpf, String cep, String cidade, String estado, String rua, String numero, String bairro, String complemento) {
}

