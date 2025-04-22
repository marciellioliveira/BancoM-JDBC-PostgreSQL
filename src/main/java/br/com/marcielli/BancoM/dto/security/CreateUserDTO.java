package br.com.marcielli.BancoM.dto.security;

public record CreateUserDTO(String username, String password, String nome, Long cpf, String cep, String cidade, String estado, String rua, String numero, String bairro, String complemento) {
}

