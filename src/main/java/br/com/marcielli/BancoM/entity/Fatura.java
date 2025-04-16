package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Fatura  implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
	//Apenas para teste
	private LocalDateTime dataVencimento = LocalDateTime.now();
	
	private BigDecimal limiteCredito; 
	
	private BigDecimal totalGastoNoMes;
	
	@OneToMany(mappedBy = "fatura", cascade = {CascadeType.ALL})
	@JsonManagedReference
	private List<Transferencia> transferenciasCredito;
	
	
	
//	@OneToOne(mappedBy = "faturaMensal")
//	private Cartao cartao;
	
//	@OneToMany(cascade = {CascadeType.ALL})
//	@JoinColumn(name = "transferenciaId")
//	private List<Transferencia> transferenciasCredito;
	
	@OneToOne(mappedBy = "fatura")
	@JsonBackReference
	private Conta conta;
	
	
	
	
	//Ver como colocar a lista de transferencias aqui quando for transferencia de Cartão de Crédito
	//pegar o ultimo limite de credito, ou seja o atualizado na rota da API sempre
	//pegar todos os valores das transferencias daquele usuario e somar.
	
	//Dps fazer: GET /cartoes/{id}/fatura** - Consultar fatura do cartão de crédito
	//Dps dazer? **POST /cartoes/{id}/fatura/pagamento** - Realizar pagamento da fatura do cartão de crédito
	//Dps voltar pra conta e fazer: - **PUT /contas/{id}/manutencao** - Aplicar taxa de manutenção mensal (para conta corrente)
	//Aionda tem conta: - **PUT /contas/{id}/rendimentos** - Aplicar rendimentos (para conta poupança)
	
	//O conta foi o que vi com a Aline das taxas de rendimento.
	
	//Dps arrumar as taxas dos cartões
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
