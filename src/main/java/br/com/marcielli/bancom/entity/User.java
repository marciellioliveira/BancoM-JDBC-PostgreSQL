package br.com.marcielli.bancom.entity;

import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.marcielli.bancom.dto.LoginRequestDTO;
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
@EqualsAndHashCode
public class User {

	private Integer id;
	private String username;
	private String password;
	private boolean userAtivo = true;
	private String role;
	//private Set<Role> roles;
	private Cliente cliente;
	
	public boolean isLoginCorrect(LoginRequestDTO loginRequest, PasswordEncoder passwordEncoder) {
		//Para saber se o login está correto, precisamos receber o loginRequest e o passwordEncoder
		//O método matches(password, password) compara a senha bruta (sem criptografia que vem no request) com a senha criptografada
		return passwordEncoder.matches(loginRequest.password(), this.password);
	}
}
