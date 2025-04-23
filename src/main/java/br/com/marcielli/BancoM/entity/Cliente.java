package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"contas", "user", "endereco"}) 
@EqualsAndHashCode
@Entity
public class Cliente implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
	@JsonIgnore
	private String nome;
	
	private Long cpf;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "enderecoId", referencedColumnName = "id")
	@JsonManagedReference
	private Endereco endereco;
	
	@OneToMany(mappedBy = "cliente", cascade = {CascadeType.ALL}, orphanRemoval = true )
	@JsonManagedReference	
	private List<Conta> contas;
	
	@OneToOne(mappedBy = "cliente")
	@JsonIgnore
    private User user;

}
