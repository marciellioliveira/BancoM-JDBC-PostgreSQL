package br.com.marcielli.BancoM.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import br.com.marcielli.BancoM.exception.TransferenciaNaoRealizadaException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	
	@JsonInclude
	private Long idClienteOrigem;	
	
	private Long idClienteDestino;
	
	@JsonInclude
	private Long idContaOrigem;
	
	private Long idContaDestino;
	
	private String tipoTransferencia;
	
	@Enumerated(EnumType.STRING)
	private TipoCartao tipoCartao;

	@JsonInclude
	private float valor;
	
	@JsonInclude
	private String numeroCartao;

	private LocalDateTime data;

	private String codigoOperacao;
	
	@ManyToOne(cascade = {CascadeType.ALL})
	@JsonIgnore
	@Transient //Remover informação do BD
	private Conta conta;

	public Transferencia() {
		this.idClienteOrigem = 0L;
		this.idContaOrigem = 0L;
	}
	
	public Transferencia(Long idClienteOrigem, Long idClienteDestino) {
		super();
		this.idClienteOrigem = idClienteOrigem;		
		this.idClienteDestino = idClienteDestino;	
	}
	
	public Transferencia(Long idClienteOrigem, Long idContaOrigem, Long idClienteDestino, Long idContaDestino, float valor, LocalDateTime data,
			String codigoOperacao, Conta conta) {
		super();
		this.idClienteOrigem = idClienteOrigem;
		this.idContaOrigem = idContaOrigem;
		this.idClienteDestino = idClienteDestino;
		this.idContaDestino = idContaDestino;
		this.valor = valor;
		this.data = data;
		this.codigoOperacao = codigoOperacao;
		this.conta = conta;
	}
	
	

	public String getTipoTransferencia() {
		return tipoTransferencia;
	}

	public void setTipoTransferencia(String tipoTransferencia) {
		
		if(tipoTransferencia.equalsIgnoreCase("TED")) {
			this.tipoTransferencia = "TED";
		}
		
		if(tipoTransferencia.equalsIgnoreCase("PIX")) {
			this.tipoTransferencia = "PIX";
		}
		
		if(tipoTransferencia.equalsIgnoreCase("DEPOSITO")) {
			this.tipoTransferencia = "DEPOSITO";
		}
		
		if(tipoTransferencia.equalsIgnoreCase("SAQUE")) {
			this.tipoTransferencia = "SAQUE";
		}
		
		
	}

	
	public String getNumeroCartao() {
		return numeroCartao;
	}

	public void setNumeroCartao(String numeroCartao) {
		this.numeroCartao = numeroCartao;
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
	
	

	public Long getIdClienteOrigem() {
		return idClienteOrigem;
	}

	public void setIdClienteOrigem(Long idClienteOrigem) {
		this.idClienteOrigem = idClienteOrigem;
	}

	public Long getIdClienteDestino() {
		return idClienteDestino;
	}

	public void setIdClienteDestino(Long idClienteDestino) {
		this.idClienteDestino = idClienteDestino;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Conta> transferirTed(Conta enviar, float valorTransferencia, Conta receber) {
		
		List<Conta> contasTransferidas = new ArrayList<Conta>();	
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		
		
		float saldoContaEnviar = enviar.getSaldoConta();
		float saldoContaReceber = receber.getSaldoConta();

		
		if(saldoContaEnviar < valorTransferencia) {
			
			throw new TransferenciaNaoRealizadaException("A transferência não foi realizada porque você tentou enviar R$ "+valorTransferencia+" mas o seu saldo atual é de R$ "+saldoContaEnviar+".");
		}
		
		float novoSaldoEnviar = saldoContaEnviar - valorTransferencia;		
		enviar.setSaldoConta(novoSaldoEnviar);		
		
		float novoSaldoReceber = saldoContaReceber + valorTransferencia;
		receber.setSaldoConta(novoSaldoReceber);
		
		this.setIdContaOrigem(enviar.getId());		
		this.setIdContaDestino(receber.getId());
		
		this.setValor(valorTransferencia);
		this.setData(dataTransferencia);
		this.setCodigoOperacao(codTransferencia);
		this.setTipoTransferencia("TED");
		
		
		Conta contaAtualizada = null;
		Taxas novasTaxas = new Taxas();
		//Atualizar 
		
		//Pagou
		if(enviar.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente minhaContaCorrentePagou = (ContaCorrente)enviar;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrentePagou);
			
			if(contaAtualizada != null) {	
				
				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
				
				for(Taxas taxasObjPagouCorrente : contaAtualizada.getTaxas()) {
					
					taxasObjPagouCorrente.setCategoria(mcc.getCategoriaConta());
					taxasObjPagouCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
				}
				contasTransferidas.add(mcc);
			} 
		}
		
		if(enviar.getTipoConta() == TipoConta.POUPANCA) {
			
			ContaPoupanca minhaContaPoupancaPagou = (ContaPoupanca)enviar;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaPagou);
			
			if(contaAtualizada != null) {	
				
				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
				
				for(Taxas taxasObjPagouPoupanca : contaAtualizada.getTaxas()) {
					
					taxasObjPagouPoupanca.setCategoria(mpp.getCategoriaConta());
					taxasObjPagouPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
					taxasObjPagouPoupanca.setTaxaMensal(mpp.getTaxaMensal());
				}
				
				contasTransferidas.add(mpp);
			} 				
		}
		
		
		//Recebeu
		if(receber.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente minhaContaCorrenteRecebeu = (ContaCorrente)receber;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrenteRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
				
				for(Taxas taxasObjRecebeuCorrente : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuCorrente.setCategoria(mcc.getCategoriaConta());
					taxasObjRecebeuCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
				}
				contasTransferidas.add(mcc);
			} 
		}
		
		if(receber.getTipoConta() == TipoConta.POUPANCA) {
			
			ContaPoupanca minhaContaPoupancaRecebeu = (ContaPoupanca)receber;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
				
				for(Taxas taxasObjRecebeuPoupanca : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuPoupanca.setCategoria(mpp.getCategoriaConta());
					taxasObjRecebeuPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
					taxasObjRecebeuPoupanca.setTaxaMensal(mpp.getTaxaMensal());
				}
				
				contasTransferidas.add(mpp);
			} 				
		}
		return contasTransferidas;
		
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Conta> transferirPix(Conta enviar, float valorTransferencia, Conta receber) {

		List<Conta> contasTransferidas = new ArrayList<Conta>();	
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		
		
		float saldoContaEnviar = enviar.getSaldoConta();
		float saldoContaReceber = receber.getSaldoConta();
		
		if(saldoContaEnviar < valorTransferencia) {
			
			throw new TransferenciaNaoRealizadaException("O PIX não foi realizado porque você tentou enviar R$ "+valorTransferencia+" mas o seu saldo atual é de R$ "+saldoContaEnviar+".");
		}
		
		float novoSaldoEnviar = saldoContaEnviar - valorTransferencia;		
		enviar.setSaldoConta(novoSaldoEnviar);		
		
		float novoSaldoReceber = saldoContaReceber + valorTransferencia;
		receber.setSaldoConta(novoSaldoReceber);
		
		this.setIdContaOrigem(enviar.getId());		
		this.setIdContaDestino(receber.getId());
		
		this.setValor(valorTransferencia);
		this.setData(dataTransferencia);
		this.setCodigoOperacao(codTransferencia);
		this.setTipoTransferencia("PIX");
		
		Conta contaAtualizada = null;
		Taxas novasTaxas = new Taxas();
		//Atualizar 
		
		//Pagou
		if(enviar.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente minhaContaCorrentePagou = (ContaCorrente)enviar;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrentePagou);
			
			if(contaAtualizada != null) {	
				
				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
				
				for(Taxas taxasObjPagouCorrente : contaAtualizada.getTaxas()) {
					
					taxasObjPagouCorrente.setCategoria(mcc.getCategoriaConta());
					taxasObjPagouCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
				}
				contasTransferidas.add(mcc);
			} 
		}
		
		if(enviar.getTipoConta() == TipoConta.POUPANCA) {
			
			ContaPoupanca minhaContaPoupancaPagou = (ContaPoupanca)enviar;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaPagou);
			
			if(contaAtualizada != null) {	
				
				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
				
				for(Taxas taxasObjPagouPoupanca : contaAtualizada.getTaxas()) {
					
					taxasObjPagouPoupanca.setCategoria(mpp.getCategoriaConta());
					taxasObjPagouPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
					taxasObjPagouPoupanca.setTaxaMensal(mpp.getTaxaMensal());
				}
				
				contasTransferidas.add(mpp);
			} 				
		}
		
		
		//Recebeu
		if(receber.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente minhaContaCorrenteRecebeu = (ContaCorrente)receber;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrenteRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
				
				for(Taxas taxasObjRecebeuCorrente : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuCorrente.setCategoria(mcc.getCategoriaConta());
					taxasObjRecebeuCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
				}
				contasTransferidas.add(mcc);
			} 
		}
		
		if(receber.getTipoConta() == TipoConta.POUPANCA) {
			
			ContaPoupanca minhaContaPoupancaRecebeu = (ContaPoupanca)receber;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
				
				for(Taxas taxasObjRecebeuPoupanca : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuPoupanca.setCategoria(mpp.getCategoriaConta());
					taxasObjRecebeuPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
					taxasObjRecebeuPoupanca.setTaxaMensal(mpp.getTaxaMensal());
				}
				
				contasTransferidas.add(mpp);
			} 				
		}
		return contasTransferidas;
		
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Conta> depositar(float valorDeposito, Conta receber) {
		
		List<Conta> contasTransferidas = new ArrayList<Conta>();	
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		
		
		float saldoContaReceber = receber.getSaldoConta();

		float novoSaldoReceber = saldoContaReceber + valorDeposito;
		receber.setSaldoConta(novoSaldoReceber);
			
		this.setIdContaDestino(receber.getId());
		
		this.setValor(valorDeposito);
		this.setData(dataTransferencia);
		this.setCodigoOperacao(codTransferencia);
		this.setIdClienteDestino(receber.getId());
		this.setTipoTransferencia("DEPOSITO");
		
		Conta contaAtualizada = null;
		Taxas novasTaxas = new Taxas();
		//Atualizar 
		
		//Recebeu
		if(receber.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente minhaContaCorrenteRecebeu = (ContaCorrente)receber;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrenteRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
				
				for(Taxas taxasObjRecebeuCorrente : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuCorrente.setCategoria(mcc.getCategoriaConta());
					taxasObjRecebeuCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
				}
				contasTransferidas.add(mcc);
			} 
		}
		
		if(receber.getTipoConta() == TipoConta.POUPANCA) {
			
			ContaPoupanca minhaContaPoupancaRecebeu = (ContaPoupanca)receber;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
				
				for(Taxas taxasObjRecebeuPoupanca : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuPoupanca.setCategoria(mpp.getCategoriaConta());
					taxasObjRecebeuPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
					taxasObjRecebeuPoupanca.setTaxaMensal(mpp.getTaxaMensal());
				}
				
				contasTransferidas.add(mpp);
			} 				
		}
		return contasTransferidas;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Conta> pagarCartao(Conta enviar, String numCartao, float valorTransferencia, Conta receber) {
		
		List<Conta> contasTransferidas = new ArrayList<Conta>();	
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		
		
		float saldoContaEnviar = enviar.getSaldoConta();
		float saldoContaReceber = receber.getSaldoConta();

		
		if(saldoContaEnviar < valorTransferencia) {
			
			throw new TransferenciaNaoRealizadaException("O pagamento não foi realizado porque você tentou enviar R$ "+valorTransferencia+" mas o seu saldo atual é de R$ "+saldoContaEnviar+".");
		}
		
		float novoSaldoEnviar = saldoContaEnviar - valorTransferencia;		
		enviar.setSaldoConta(novoSaldoEnviar);		
		
		float novoSaldoReceber = saldoContaReceber + valorTransferencia;
		receber.setSaldoConta(novoSaldoReceber);
		
		this.setIdContaOrigem(enviar.getId());		
		this.setIdContaDestino(receber.getId());
		
		this.setValor(valorTransferencia);
		this.setData(dataTransferencia);
		this.setCodigoOperacao(codTransferencia);
		this.setTipoTransferencia("CARTAO");
		this.setNumeroCartao(numCartao);
		
		Conta contaAtualizada = null;
		Taxas novasTaxas = new Taxas();
		//Atualizar 
		
		//Pagou
		if(enviar.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente minhaContaCorrentePagou = (ContaCorrente)enviar;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrentePagou);
			
			if(contaAtualizada != null) {	
				
				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
				
				for(Taxas taxasObjPagouCorrente : contaAtualizada.getTaxas()) {
					
					taxasObjPagouCorrente.setCategoria(mcc.getCategoriaConta());
					taxasObjPagouCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
					
				}
				
				contasTransferidas.add(mcc);
			} 
		}
		
		if(enviar.getTipoConta() == TipoConta.POUPANCA) {
			
			ContaPoupanca minhaContaPoupancaPagou = (ContaPoupanca)enviar;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaPagou);
			
			if(contaAtualizada != null) {	
				
				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
				
				for(Taxas taxasObjPagouPoupanca : contaAtualizada.getTaxas()) {
					
					taxasObjPagouPoupanca.setCategoria(mpp.getCategoriaConta());
					taxasObjPagouPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
					taxasObjPagouPoupanca.setTaxaMensal(mpp.getTaxaMensal());					
				}
				
				contasTransferidas.add(mpp);
			} 				
		}
		
		
		//Recebeu
		if(receber.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente minhaContaCorrenteRecebeu = (ContaCorrente)receber;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrenteRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
				
				for(Taxas taxasObjRecebeuCorrente : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuCorrente.setCategoria(mcc.getCategoriaConta());
					taxasObjRecebeuCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
				}
				contasTransferidas.add(mcc);
			} 
		}
		
		if(receber.getTipoConta() == TipoConta.POUPANCA) {
			
			ContaPoupanca minhaContaPoupancaRecebeu = (ContaPoupanca)receber;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
				
				for(Taxas taxasObjRecebeuPoupanca : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuPoupanca.setCategoria(mpp.getCategoriaConta());
					taxasObjRecebeuPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
					taxasObjRecebeuPoupanca.setTaxaMensal(mpp.getTaxaMensal());
				}
				
				contasTransferidas.add(mpp);
			} 				
		}
		return contasTransferidas;
		
	}
	
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public List<Conta> pagarCartao(float valorDeposito, Conta receber) {
//		
//		List<Conta> contaUtilizada = new ArrayList<Conta>();	
//		
//		LocalDateTime dataTransferencia = LocalDateTime.now();
//		String codTransferencia = gerarCodigoTransferencia();		
//		
//		float saldoContaReceber = receber.getSaldoConta();
//
//		float novoSaldoReceber = saldoContaReceber + valorDeposito;
//		receber.setSaldoConta(novoSaldoReceber);
//			
//		this.setIdContaDestino(receber.getId());
//		
//		this.setValor(valorDeposito);
//		this.setData(dataTransferencia);
//		this.setCodigoOperacao(codTransferencia);
//		this.setIdClienteDestino(receber.getId());
//		this.setTipoTransferencia("PAGCARTAO");
//		
//		Conta contaAtualizada = null;
//
//		Taxas novasTaxas = new Taxas();
//		//Atualizar 
//		
//		//Recebeu
//		if(receber.getTipoConta() == TipoConta.CORRENTE) {
//			
//			ContaCorrente minhaContaCorrenteRecebeu = (ContaCorrente)receber;
//			
//			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrenteRecebeu);
//			
//			if(contaAtualizada != null) {	
//				
//				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
//								
//				for(Taxas taxasObjRecebeuCorrente : contaAtualizada.getTaxas()) {
//					
//					taxasObjRecebeuCorrente.setCategoria(mcc.getCategoriaConta());
//					taxasObjRecebeuCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
//				}
//				
//				contaUtilizada.add(mcc);
//			} 
//		}
//		
//		if(receber.getTipoConta() == TipoConta.POUPANCA) {
//			
//			ContaPoupanca minhaContaPoupancaRecebeu = (ContaPoupanca)receber;
//			
//			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaRecebeu);
//			
//			if(contaAtualizada != null) {	
//				
//				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
//				
//				for(Taxas taxasObjRecebeuPoupanca : contaAtualizada.getTaxas()) {
//					
//					taxasObjRecebeuPoupanca.setCategoria(mpp.getCategoriaConta());
//					taxasObjRecebeuPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
//					taxasObjRecebeuPoupanca.setTaxaMensal(mpp.getTaxaMensal());
//				}
//				
//				contaUtilizada.add(mpp);
//			} 				
//		}
//		return contaUtilizada;
//	}
//	
	
	

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Conta> sacar(float valorSaque, Conta sacar) {
		
		List<Conta> contasSaque = new ArrayList<Conta>();	
		
		LocalDateTime dataTransferencia = LocalDateTime.now();
		String codTransferencia = gerarCodigoTransferencia();		
		
		float saldoContaSacar = sacar.getSaldoConta();

		float novoSaldoSacar = saldoContaSacar - valorSaque;
		sacar.setSaldoConta(novoSaldoSacar);
			
		this.setIdContaDestino(sacar.getId());
		
		this.setValor(valorSaque);
		this.setData(dataTransferencia);
		this.setCodigoOperacao(codTransferencia);
		this.setIdClienteDestino(sacar.getId());
		this.setTipoTransferencia("SAQUE");
		
		Conta contaAtualizada = null;
		Taxas novasTaxas = new Taxas();
		//Atualizar 
		
		//Recebeu
		if(sacar.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente minhaContaCorrenteRecebeu = (ContaCorrente)sacar;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaCorrenteRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaCorrente mcc = (ContaCorrente)contaAtualizada;
				
				for(Taxas taxasObjRecebeuCorrente : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuCorrente.setCategoria(mcc.getCategoriaConta());
					taxasObjRecebeuCorrente.setTaxaManutencaoMensal(mcc.getTaxaManutencaoMensal());
				}
				contasSaque.add(mcc);
			} 
		}
		
		if(sacar.getTipoConta() == TipoConta.POUPANCA) {
			
			ContaPoupanca minhaContaPoupancaRecebeu = (ContaPoupanca)sacar;
			
			contaAtualizada = novasTaxas.atualizarTaxas(minhaContaPoupancaRecebeu);
			
			if(contaAtualizada != null) {	
				
				ContaPoupanca mpp = (ContaPoupanca)contaAtualizada;
				
				for(Taxas taxasObjRecebeuPoupanca : contaAtualizada.getTaxas()) {
					
					taxasObjRecebeuPoupanca.setCategoria(mpp.getCategoriaConta());
					taxasObjRecebeuPoupanca.setTaxaAcrescRend(mpp.getTaxaAcrescRend());
					taxasObjRecebeuPoupanca.setTaxaMensal(mpp.getTaxaMensal());
				}
				
				contasSaque.add(mpp);
			} 				
		}
		return contasSaque;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
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
	
	
	
	
	//Pagamento com Cartão
	
	
	

	@Override
	public String toString() {
		return "Transferencia [id=" + id + ", version=" + version + ", idContaOrigem=" + idContaOrigem
				+ ", idContaDestino=" + idContaDestino + ", valor=" + valor + ", data=" + data + ", codigoOperacao="
				+ codigoOperacao + ", conta=" + conta + "]";
	}


	
}
