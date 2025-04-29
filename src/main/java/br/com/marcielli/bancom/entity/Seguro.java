package br.com.marcielli.bancom.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import br.com.marcielli.bancom.enuns.TipoSeguro;
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
public class Seguro {

	private Long id;

    private TipoSeguro tipo;

    private BigDecimal valorMensal;

    private BigDecimal valorApolice;

    private Boolean ativo = true;

	@JsonBackReference
    private Cartao cartao;

}
