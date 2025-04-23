package br.com.marcielli.BancoM.entity;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.marcielli.BancoM.dto.LoginRequestDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tb_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"tokens", "clientes"})
@EqualsAndHashCode
public class User{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;
	
	//Criando uma tabela intermediária para o Id do usuário como userId e o id da role como roleId
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) 
	@JoinTable(name = "tb_users_roles", joinColumns = @JoinColumn(referencedColumnName = "id"),inverseJoinColumns = @JoinColumn(referencedColumnName = "role_id"))
	private Set<Role> roles;
	
	@OneToOne(cascade = CascadeType.ALL) 
	@JoinColumn(name = "cliente_id")
	private Cliente cliente;
	
	public boolean isLoginCorrect(LoginRequestDTO loginRequest, PasswordEncoder passwordEncoder) {
		//Para saber se o login está correto, precisamos receber o loginRequest e o passwordEncoder
		//O método matches(password, password) compara a senha bruta (sem criptografia que vem no request) com a senha criptografada
		return passwordEncoder.matches(loginRequest.password(), this.password);
	}
}
