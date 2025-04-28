package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.util.List;

import br.com.marcielli.bancom.annotation.VersionControl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
@ToString
//@ToString(exclude = {"contas", "user", "endereco"})
@EqualsAndHashCode
public class Cliente implements Serializable {

	private Long id;

	//private Long version;

	private String nome;
	private Long cpf;
	private boolean clienteAtivo = true;
	private Endereco endereco;
	private List<Conta> contas;
    private User user;

}
