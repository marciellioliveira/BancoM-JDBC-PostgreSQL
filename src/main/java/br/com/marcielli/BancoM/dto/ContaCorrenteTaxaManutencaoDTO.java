package br.com.marcielli.BancoM.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
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
public class ContaCorrenteTaxaManutencaoDTO {

	@NotBlank(message = "A nova taxa deve ser informada.")
	private BigDecimal taxaManutencaoMensal;
	
}
