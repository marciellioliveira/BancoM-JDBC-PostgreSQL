package br.com.marcielli.BancoM.entity;

import java.math.BigDecimal;
import br.com.marcielli.BancoM.enuns.TipoSeguro;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "seguros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Seguro {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
	 @Enumerated(EnumType.STRING)
	    private TipoSeguro tipo;

	    private BigDecimal valorMensal;

	    private BigDecimal valorApolice;

	    private Boolean ativo = true;

	    @ManyToOne
	    private Cartao cartao;

}
