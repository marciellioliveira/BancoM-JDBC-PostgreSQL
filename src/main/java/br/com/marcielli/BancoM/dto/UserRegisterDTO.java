package br.com.marcielli.BancoM.dto;

import br.com.marcielli.BancoM.enuns.Role;
import br.com.marcielli.BancoM.validation.ValidarClienteSeForUser;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@ValidarClienteSeForUser //Criei essa anotação para validar os campos e aceitar somente alguns dados caso o cadastro seja de ADMIN
public class UserRegisterDTO {
	
	private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Role role; // Role pode ser 'USER' ou 'ADMIN'

    // Informações do cliente, se necessário (somente para USER)
	private String nome;	
	private Long cpf;	
	private String cep;	
	private String cidade;	
	private String estado;	
	private String rua;	
	private String numero;	
	private String bairro;	
	private String complemento;

}
