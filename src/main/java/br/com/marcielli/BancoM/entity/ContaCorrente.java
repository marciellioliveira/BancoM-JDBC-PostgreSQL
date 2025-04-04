package br.com.marcielli.BancoM.entity;


import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "idConta")
public class ContaCorrente extends Conta {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private float taxaManutencaoMensal;
	
	public ContaCorrente() {}
	
	public ContaCorrente(float saldo) {		
		setTaxaManutencaoMensal(saldo);
		super.setCategoriaConta(saldo);			
	}

	public ContaCorrente(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta,
			String numeroConta, float taxaManutencaoMensal) {
		super(cliente, tipoConta, categoriaConta, saldoConta, numeroConta);
		setTaxaManutencaoMensal(saldoConta);
		super.setCategoriaConta(saldoConta);		
	}
	
	
	
	public float getTaxaManutencaoMensal() {		
		return taxaManutencaoMensal;
	}

	public void setTaxaManutencaoMensal(float saldo) {		
		
		if(saldo <= 1000f) {
			this.taxaManutencaoMensal = 1200f;		
		}
		
		if(saldo > 1000f && saldo <= 5000f) {
			this.taxaManutencaoMensal = 800f;	
		}
		
		if(saldo > 5000f)
		this.taxaManutencaoMensal = 0f;		
	}
	
	
	
	

}
