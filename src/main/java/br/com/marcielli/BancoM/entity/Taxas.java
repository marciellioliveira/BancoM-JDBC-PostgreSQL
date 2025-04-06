package br.com.marcielli.BancoM.entity;


public class Taxas {

//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//	
//	@Version
//	private Long version;
//	
//	@Enumerated(EnumType.STRING)
//	private CategoriaConta categoria; //Comum, super ou premium
//	
//	@Enumerated(EnumType.STRING)
//	private TipoConta tipoConta; //Corrente ou Poupança
//	
//	
//	//Taxas da Poupança
//	private float taxaAcrescRend;	
//	private float taxaMensal;
//
//	
//	//Taxas da Corrente
//	private float taxaManutencaoMensal;
//	
//	@ManyToOne
//	private Conta contas;
//	
//	public Taxas() {}
//	
//	public Taxas(float saldoConta) {
//		
//	//Dependendo do saldo, você tem as taxas
//	 setTaxaManutencaoMensal(saldoConta);
//	 
//	 this.taxaManutencaoMensal = getTaxaManutencaoMensal();
//	 
//	 	if(saldoConta <= 1000f) {
//	 		this.categoria = CategoriaConta.COMUM;
//	 		this.taxaAcrescRend = 0.005f;			
//			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//		}
//		
//		if(saldoConta > 1000f && saldoConta <= 5000f) {
//			this.categoria = CategoriaConta.SUPER;
//			this.taxaAcrescRend = 0.007f;				
//			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//		}
//		
//		if(saldoConta > 5000f) {
//			this.categoria = CategoriaConta.PREMIUM;
//			this.taxaAcrescRend = 0.009f;
//			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//		}
//	}
//
//	public Taxas(CategoriaConta categoria, TipoConta tipoConta, float taxaAcrescRend,
//			float taxaMensal, float taxaManutencaoMensal) {
//		super();		
//		this.categoria = categoria;
//		this.tipoConta = tipoConta;
//		this.taxaAcrescRend = taxaAcrescRend;
//		this.taxaMensal = taxaMensal;
//		this.taxaManutencaoMensal = taxaManutencaoMensal;		
//	}
//	
//
//	public Long getId() {
//		return id;
//	}
//
//
//	public void setId(Long id) {
//		this.id = id;
//	}
//
//
//	public CategoriaConta getCategoria() {
//		return categoria;
//	}
//
//
//	public void setCategoria(CategoriaConta categoria) {
//		this.categoria = categoria;
//	}
//
//
//	public TipoConta getTipoConta() {
//		return tipoConta;
//	}
//
//
//	public void setTipoConta(TipoConta tipoConta) {
//		this.tipoConta = tipoConta;
//	}
//
//
//	public float getTaxaAcrescRend() {
//		return taxaAcrescRend;
//	}
//
//
//	public void setTaxaAcrescRend(float taxaAcrescRend) {
//		this.taxaAcrescRend = taxaAcrescRend;
//	}
//
//
//	public float getTaxaMensal() {
//		return taxaMensal;
//	}
//
//
//	public void setTaxaMensal(float taxaMensal) {
//		this.taxaMensal = taxaMensal;
//	}
//
//
//	public float getTaxaManutencaoMensal() {
//		return taxaManutencaoMensal;
//	}
//
//
//	public void setTaxaManutencaoMensal(float saldo) {
//		
//		if(saldo <= 1000f) {
//			this.taxaManutencaoMensal = 12.00f;		
//		}
//		
//		if(saldo > 1000f && saldo <= 5000f) {
//			this.taxaManutencaoMensal = 8.00f;	
//		}
//		
//		if(saldo > 5000f) {
//			this.taxaManutencaoMensal = 0f;	
//		}
//	}
}
