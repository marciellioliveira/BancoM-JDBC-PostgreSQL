package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
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
public class Transferencia implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;

	@JsonInclude
	private Long idClienteOrigem;
	private Long idClienteDestino;

	@JsonInclude
	private Long idContaOrigem;
	private Long idContaDestino;
	
	private Long idCartao;

	private TipoTransferencia tipoTransferencia;

	private BigDecimal valor;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime data;

	private String codigoOperacao;

	private TipoCartao tipoCartao;

	@JsonIgnore
	private Conta conta;

	@JsonBackReference
	@ToString.Exclude
	private Fatura fatura;


	// TED
	public Transferencia(Conta enviar, BigDecimal valor, Conta receber, TipoTransferencia tipoTransferencia) {
		
		if (enviar.getCliente() != null) {
	        this.idClienteOrigem = enviar.getCliente().getId();
	    } else {
	        throw new IllegalArgumentException("Cliente da conta de origem não pode ser nulo.");
	    }
	    if (receber.getCliente() != null) {
	        this.idClienteDestino = receber.getCliente().getId();
	    } else {
	        throw new IllegalArgumentException("Cliente da conta de destino não pode ser nulo.");
	    }
		
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
		this.tipoCartao = TipoCartao.SEM_CARTAO;
	}

	// PIX
	public Transferencia(Conta enviar, BigDecimal valor, Conta receber, TipoTransferencia tipoTransferencia,String chavePixUtilizada) {
		
		if (enviar.getCliente() != null) {
	        this.idClienteOrigem = enviar.getCliente().getId();
	    } else {
	        throw new IllegalArgumentException("Cliente da conta de origem não pode ser nulo.");
	    }
	    if (receber.getCliente() != null) {
	        this.idClienteDestino = receber.getCliente().getId();
	    } else {
	        throw new IllegalArgumentException("Cliente da conta de destino não pode ser nulo.");
	    }
		
		this.idClienteOrigem = enviar.getCliente().getId();
	    this.idClienteDestino = receber.getCliente().getId();
	    this.idContaOrigem = enviar.getId();
	    this.idContaDestino = receber.getId();
	    this.tipoTransferencia = tipoTransferencia;
	    this.valor = valor;

	    LocalDateTime dataTransferencia = LocalDateTime.now();
	    String codTransferencia = gerarCodigoTransferencia();

	    this.data = dataTransferencia;
	    this.codigoOperacao = "PIX_" + chavePixUtilizada + "_" + codTransferencia;
	    this.tipoCartao = TipoCartao.SEM_CARTAO;
		
//		this(enviar, valor, receber, tipoTransferencia);
//		this.codigoOperacao = "PIX_" + chavePixUtilizada + "_" + this.codigoOperacao;
	}

	// DEPOSITO - SAQUE
	public Transferencia(Conta conta, BigDecimal valor, TipoTransferencia tipoTransferencia) {
		
		if (conta.getCliente() != null) {
	        this.idClienteOrigem = conta.getCliente().getId();
	    } else {
	        throw new IllegalArgumentException("Cliente da conta não pode ser nulo.");
	    }

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
		this.tipoCartao = TipoCartao.SEM_CARTAO;
	}

	// CARTÃO CRÉDITO - DÉBITO
	public Transferencia(Conta enviar, BigDecimal valor, Conta receber, TipoTransferencia tipoTransferencia,
			TipoCartao tipoCartao) {

		if (enviar.getCliente() == null || receber.getCliente() == null) {
            throw new IllegalArgumentException("Contas devem ter clientes associados");
        }

		this.idClienteOrigem = enviar.getCliente().getId();
        this.idClienteDestino = receber.getCliente().getId();
        this.idContaOrigem = enviar.getId();
        this.idContaDestino = receber.getId();
        this.tipoTransferencia = tipoTransferencia;
        this.valor = valor;
        this.tipoCartao = tipoCartao;
        
        // Campos automáticos
        this.data = LocalDateTime.now();
        this.codigoOperacao = gerarCodigoTransferencia();
        
        this.idCartao = null;
	}
	
	
	

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
