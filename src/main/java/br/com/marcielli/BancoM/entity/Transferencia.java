package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoTransferencia;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
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
public class Transferencia implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	private Long version; //Caso precise implementar devolução de transferência porque transferiu errado.
	
	@JsonInclude
	private Long idClienteOrigem;	
	private Long idClienteDestino;
	
	@JsonInclude
	private Long idContaOrigem;
	private Long idContaDestino;
	
	private TipoTransferencia tipoTransferencia;
	
	private BigDecimal valor;

	private LocalDateTime data;

	private String codigoOperacao;
	
	private TipoCartao tipoCartao;
	
	@ManyToOne(cascade = {CascadeType.ALL})
	@JsonIgnore
	@Transient //Remover informação do BD
	private Conta conta;
	
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "faturaId")
	@JsonBackReference
	private Fatura fatura;
	
	
//	@ManyToOne(cascade = {CascadeType.ALL})
//	private Fatura fatura;

	//TED - PIX
	public Transferencia(Conta enviar, BigDecimal valor, Conta receber, TipoTransferencia tipoTransferencia) {
		super();
		this.idClienteOrigem = enviar.getCliente().getId();		
		this.idClienteDestino = receber.getCliente().getId();	
		this.idContaOrigem = enviar.getId();
		this.idContaDestino = receber.getId();
		this.tipoTransferencia = tipoTransferencia;
		this.valor = valor;
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		
		
		this.data = dataTransferencia;		
		this.codigoOperacao = codTransferencia;
	}
	
	//DEPOSITO - SAQUE
	public Transferencia(Conta conta, BigDecimal valor, TipoTransferencia tipoTransferencia) {
		super();
		this.idClienteOrigem = conta.getCliente().getId();		
		this.idClienteDestino = 0L;	
		this.idContaOrigem = conta.getId();
		this.idContaDestino = 0L;
		this.tipoTransferencia = tipoTransferencia;
		this.valor = valor;
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		
		
		this.data = dataTransferencia;		
		this.codigoOperacao = codTransferencia;
	}
	
	//CARTÃO CRÉDITO - DÉBITO
	public Transferencia(Conta enviar, BigDecimal valor, Conta receber, TipoTransferencia tipoTransferencia, TipoCartao tipoCartao) {
		super();
		this.idClienteOrigem = enviar.getCliente().getId();		
		this.idClienteDestino = receber.getCliente().getId();	
		this.idContaOrigem = enviar.getId();
		this.idContaDestino = receber.getId();
		this.tipoTransferencia = tipoTransferencia;
		this.valor = valor;
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		
		
		this.data = dataTransferencia;		
		this.codigoOperacao = codTransferencia;
		this.tipoCartao = tipoCartao;
	}
		
	
	//@Override
	public String gerarCodigoTransferencia() {
		int[] sequencia = new int[21];
		Random random = new Random();
		String codTransferencia = "";

		for (int i = 0; i < sequencia.length; i++) {
			sequencia[i] = 1 + random.nextInt(8);
		}

		for (int i = 0; i < sequencia.length; i++) {
			codTransferencia += Integer.toString(sequencia[i]);
		}

		return codTransferencia;		
	}

	
}
