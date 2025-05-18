package br.com.marcielli.bancom.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Profile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "cliente")
@EqualsAndHashCode
public class Endereco {

	private Long id;
	private String cep;
	private String cidade;
	private String estado;
	private String rua;
	private String numero;
	private String bairro;
	private String complemento;

	@JsonBackReference
	private Cliente cliente;
}
