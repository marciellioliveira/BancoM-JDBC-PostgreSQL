package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Profile;

@Profile("cliente")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Cliente implements Serializable {

	private Long id;
	@JsonIgnore
	private String nome;
	private Long cpf;
	private boolean clienteAtivo = true;

	@JsonManagedReference
	private Endereco endereco;

//	@Transient //Retirar depois (Ta aqui apenas para ver se o user está funcionando)
	@JsonManagedReference
	private List<Conta> contas;

//	@Transient //Retirar depois (Ta aqui apenas para ver se o user está funcionando)
	@JsonIgnore
    private User user;
}
