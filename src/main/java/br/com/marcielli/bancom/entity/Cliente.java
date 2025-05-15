package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
@EqualsAndHashCode
public class Cliente implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String nome;
	private Long cpf;
	private boolean clienteAtivo;

	@JsonManagedReference
	private Endereco endereco;

	@JsonManagedReference
	private List<Conta> contas;

	@JsonIgnore
    private User user;
	
	public List<Conta> getContas() {
        if (contas == null) {
            contas = new ArrayList<>();
        }
        return contas;
    }
}
