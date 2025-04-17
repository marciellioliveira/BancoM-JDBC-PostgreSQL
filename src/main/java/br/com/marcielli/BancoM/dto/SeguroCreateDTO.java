package br.com.marcielli.BancoM.dto;

import java.math.BigDecimal;

import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.enuns.TipoSeguro;
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
