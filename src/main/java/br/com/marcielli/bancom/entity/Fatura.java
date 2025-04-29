package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Fatura  implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;

	
	//Apenas para teste
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime dataVencimento;
	
//	private BigDecimal limiteCredito; 
//	
//	private BigDecimal totalGastoNoMes;

	@JsonManagedReference
	private List<Transferencia> transferenciasCredito;
	
	public Fatura() {
		this.dataVencimento = LocalDateTime.now();
		this.transferenciasCredito = new ArrayList<Transferencia>();
	}

	@JsonBackReference
	private Cartao cartao;
	
	public boolean status = false; //Se tiver verdadeiro é porque a fatura foi paga

	
	public void adicionarTransfCredito(Transferencia transferindo) {
		if (transferenciasCredito == null) {
			transferenciasCredito = new ArrayList<>();
		}
		this.transferenciasCredito.add(transferindo);
		transferindo.setFatura(this);
		
	}
}
