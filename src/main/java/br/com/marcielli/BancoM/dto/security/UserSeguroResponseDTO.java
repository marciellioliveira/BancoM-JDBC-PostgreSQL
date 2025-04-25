package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.BancoM.enuns.TipoSeguro;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class UserSeguroResponseDTO {
	
	private Long id;
    private TipoSeguro tipo;
    private boolean ativo;
    private BigDecimal valorMensal;
    private BigDecimal valorApolice;
    private Long idCartao;
    
	
//	private Long id;
//	
//	@Enumerated(EnumType.STRING)
//    private TipoSeguro tipo;
//    
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private BigDecimal valorMensal;
//    
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private BigDecimal valorApolice;
//    
//    private Boolean ativo;

}
