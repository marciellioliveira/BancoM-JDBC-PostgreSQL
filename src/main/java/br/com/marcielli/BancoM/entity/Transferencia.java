package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.BancoM.exception.TransferenciaNaoRealizadaException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
public class Transferencia implements TransferenciaContrato, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	private Long version; //Caso precise implementar devolução de transferência porque transferiu errado.
	
	private Long idContaOrigem;
	
	private Long idContaDestino;

	private float valor;

	private LocalDateTime data;

	private String codigoOperacao;
	
	@ManyToOne
	@JsonIgnore
	@Transient //Remover informação do BD
	private Conta conta;

	public Transferencia() {}
	
	public Transferencia(Long idContaOrigem, Long idContaDestino, float valor, LocalDateTime data,
			String codigoOperacao, Conta conta) {
		super();
		this.idContaOrigem = idContaOrigem;
		this.idContaDestino = idContaDestino;
		this.valor = valor;
		this.data = data;
		this.codigoOperacao = codigoOperacao;
		this.conta = conta;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public float getValor() {
		return valor;
	}

	public void setValor(float valor) {
		this.valor = valor;
	}
	
	public Long getIdContaOrigem() {
		return idContaOrigem;
	}

	public void setIdContaOrigem(Long idContaOrigem) {
		this.idContaOrigem = idContaOrigem;
	}

	public Long getIdContaDestino() {
		return idContaDestino;
	}

	public void setIdContaDestino(Long idContaDestino) {
		this.idContaDestino = idContaDestino;
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	public String getCodigoOperacao() {
		return codigoOperacao;
	}

	public void setCodigoOperacao(String codigoOperacao) {
		this.codigoOperacao = codigoOperacao;
	}

	public Conta getConta() {
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}

	@Override
	@Transactional
	public boolean transferirTed(Conta enviar, float valor, Conta receber) {
	
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		float valorTransferencia = valor;
		
		
		if(enviar.getSaldoConta() < valor) {
			
			throw new TransferenciaNaoRealizadaException("A transferência não foi realizada porque você tentou enviar R$ "+valor+" mas o seu saldo atual é de R$ "+enviar.getSaldoConta()+".");
		}
		
		float novoSaldoEnviar = enviar.getSaldoConta() - valor;		
		enviar.setSaldoConta(novoSaldoEnviar);		
		
		float novoSaldoReceber = receber.getSaldoConta() + valor;
		receber.setSaldoConta(novoSaldoReceber);
		
		this.setIdContaOrigem(enviar.getId());
		this.setIdContaDestino(receber.getId());
		this.setValor(valorTransferencia);
		this.setData(dataTransferencia);
		this.setCodigoOperacao(codTransferencia);
		
		return true;
	}

	@Override
	@Transactional
	public boolean transferirPix(Conta enviar, float valor, Conta receber) {
		

		
		
		return false;
	}

	@Override
	@Transactional
	public boolean depositar(float valor, Conta receber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Transactional
	public boolean sacar(float valor, Conta receber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Transactional
	public float exibirSaldo(Conta conta) {
		return conta.getSaldoConta();
	}

	@Override
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
	
	

	@Override
	public String toString() {
		return "Transferencia [id=" + id + ", version=" + version + ", idContaOrigem=" + idContaOrigem
				+ ", idContaDestino=" + idContaDestino + ", valor=" + valor + ", data=" + data + ", codigoOperacao="
				+ codigoOperacao + ", conta=" + conta + "]";
	}


	
}
