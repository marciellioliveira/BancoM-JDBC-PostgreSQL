package br.com.marcielli.BancoM.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
public class Taxas {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Version
	private Long version;
	
	@Enumerated(EnumType.STRING)
	private CategoriaConta categoria; //Comum, super ou premium
	
	@Enumerated(EnumType.STRING)
	private TipoConta tipoConta; //Corrente ou Poupança
	
	
	//Taxas da Poupança
	private float taxaAcrescRend;	
	private float taxaMensal;

	
	//Taxas da Corrente
	private float taxaManutencaoMensal;
	
	@ManyToOne
	@JoinColumn(name = "conta") 
	@JsonInclude 
	@Transient //Retirar do banco com @Transient mas incluir no Json para serializar com @JsonInclude 
	private Conta contas;
	
	public Taxas() {}
	
	//Testando
	public Taxas(float saldoConta, TipoConta tipoConta) {
		
//		this.tipoConta = tipoConta;
//		CategoriaConta categoria = null;
//		float taxaAcres = 0;
//		float taxaMes = 0;
		//Dependendo do saldo, você tem as taxas
		
		 
//		 if(saldoConta <= 1000f) {
//			 
//			 categoria = CategoriaConta.COMUM;
//			 taxaAcres = 0.005f;
//			 taxaMes = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//			 
//		}
//			
//		if(saldoConta > 1000f && saldoConta <= 5000f) {
//			
//			categoria = CategoriaConta.SUPER;
//			taxaAcres = 0.007f;		
//			taxaMes = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//			
//		}
//		
//		if(saldoConta > 5000f) {
//			
//			categoria = CategoriaConta.PREMIUM;
//			taxaAcres = 0.009f;
//			taxaMes = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//			
//		}
	
//		setCategoria(categoria);
		
	
			
		if(tipoConta == TipoConta.CORRENTE) {
			
			 this.tipoConta = TipoConta.CORRENTE;
			 
			 if(saldoConta <= 1000f) {
				 
				
				 this.categoria = CategoriaConta.COMUM;
				 this.taxaManutencaoMensal = 12.00f;		
				 
			}
				
			if(saldoConta > 1000f && saldoConta <= 5000f) {
				
				 this.categoria = CategoriaConta.SUPER;
				 this.taxaManutencaoMensal = 8.00f;	
				
			}
			
			if(saldoConta > 5000f) {
				
				 this.categoria = CategoriaConta.PREMIUM;
				 this.taxaManutencaoMensal = 0f;	
				
			}
			
			
//			 this.categoria = getCategoria();
//			 setTaxaManutencaoMensal(saldoConta);
//			 this.taxaManutencaoMensal = getTaxaManutencaoMensal();			
		} 
		
		if(tipoConta == TipoConta.POUPANCA) {
			
			 this.tipoConta = TipoConta.POUPANCA;
			
			if(saldoConta <= 1000f) {
				 
				 this.categoria = CategoriaConta.COMUM;
				 this.taxaAcrescRend = 0.005f;
				 this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
					
				 
			}
				
			if(saldoConta > 1000f && saldoConta <= 5000f) {
				
				 this.categoria = CategoriaConta.SUPER;
				 this.taxaAcrescRend = 0.007f;
				 this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
				
				
			}
			
			if(saldoConta > 5000f) {
				
				 this.categoria = CategoriaConta.PREMIUM;
				 this.taxaAcrescRend = 0.009f;
				 this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
				
			}
			
			
			
			
			
			
//			 this.categoria = getCategoria();
//			 setTaxaAcrescRend(taxaAcres);
//			 setTaxaMensal(taxaMes);
//			 this.taxaAcrescRend = getTaxaAcrescRend();			 
//			 this.taxaMensal = getTaxaMensal();
		}
		
		}
	
	
	
	//Usava
//	public Taxas(float saldoConta, TipoConta tipoConta) {
//		
//	//Dependendo do saldo, você tem as taxas
//	 setTaxaManutencaoMensal(saldoConta);
//	 
//	 this.taxaManutencaoMensal = getTaxaManutencaoMensal();
//	 
// 	if(saldoConta <= 1000f) {
// 		this.categoria = CategoriaConta.COMUM;
// 		this.taxaAcrescRend = 0.005f;			
//		this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//	}
//	
//	if(saldoConta > 1000f && saldoConta <= 5000f) {
//		this.categoria = CategoriaConta.SUPER;
//		this.taxaAcrescRend = 0.007f;				
//		this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//	}
//	
//	if(saldoConta > 5000f) {
//		this.categoria = CategoriaConta.PREMIUM;
//		this.taxaAcrescRend = 0.009f;
//		this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//	}
//	
//	this.tipoConta = tipoConta;
//	}

	public Taxas(CategoriaConta categoria, TipoConta tipoConta, float taxaAcrescRend,
			float taxaMensal, float taxaManutencaoMensal) {
		super();		
		this.categoria = categoria;
		this.tipoConta = tipoConta;
		this.taxaAcrescRend = taxaAcrescRend;
		this.taxaMensal = taxaMensal;
		this.taxaManutencaoMensal = taxaManutencaoMensal;		
	}
	

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public CategoriaConta getCategoria() {
		return categoria;
	}


	public void setCategoria(CategoriaConta categoria) {
		this.categoria = categoria;
	}


	public TipoConta getTipoConta() {
		return tipoConta;
	}


	public void setTipoConta(TipoConta tipoConta) {
		this.tipoConta = tipoConta;
	}


	public float getTaxaAcrescRend() {
		return taxaAcrescRend;
	}


	public void setTaxaAcrescRend(float taxaAcrescRend) {
		this.taxaAcrescRend = taxaAcrescRend;
	}


	public float getTaxaMensal() {
		return taxaMensal;
	}


	public void setTaxaMensal(float taxaMensal) {
		this.taxaMensal = taxaMensal;
	}


	public float getTaxaManutencaoMensal() {
		return taxaManutencaoMensal;
	}


	public void setTaxaManutencaoMensal(float saldo) {
		
		if(saldo <= 1000f) {
			this.taxaManutencaoMensal = 12.00f;		
		}
		
		if(saldo > 1000f && saldo <= 5000f) {
			this.taxaManutencaoMensal = 8.00f;	
		}
		
		if(saldo > 5000f) {
			this.taxaManutencaoMensal = 0f;	
		}
	}
}
