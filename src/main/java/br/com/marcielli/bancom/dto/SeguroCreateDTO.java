package br.com.marcielli.bancom.dto;

import br.com.marcielli.bancom.enuns.TipoSeguro;
import jakarta.validation.constraints.NotNull;
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
public class SeguroCreateDTO {
	
	 @NotNull
	 private Long idCartao;

	 @NotNull
	 private TipoSeguro tipo;

}
