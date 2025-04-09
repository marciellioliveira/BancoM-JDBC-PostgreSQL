package br.com.marcielli.BancoM.entity;

import java.io.Serializable;


public class TaxasManutencao implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long ateValor1;
	
	private Long ateValor2;
	
	private Long ateValor3;
	
	//Conta Corrente - Taxa de Manutenção
	private float taxaManutencaoMensal;
	

	//Conta Poupança - Taxas de Rendimento	
	private float taxaAcrescRend;	
	private float taxaMensal;
	
	public TaxasManutencao(Long ateValor1, Long ateValor2, Long ateValor3, float taxaManutencaoMensal,
			float taxaAcrescRend, float taxaMensal) {
		super();
		this.ateValor1 = ateValor1;
		this.ateValor2 = ateValor2;
		this.ateValor3 = ateValor3;
		this.taxaManutencaoMensal = taxaManutencaoMensal;
		this.taxaAcrescRend = taxaAcrescRend;
		this.taxaMensal = taxaMensal;
	}

	public Long getAteValor1() {
		return ateValor1;
	}

	public void setAteValor1(Long ateValor1) {
		this.ateValor1 = ateValor1;
	}

	public Long getAteValor2() {
		return ateValor2;
	}

	public void setAteValor2(Long ateValor2) {
		this.ateValor2 = ateValor2;
	}

	public Long getAteValor3() {
		return ateValor3;
	}

	public void setAteValor3(Long ateValor3) {
		this.ateValor3 = ateValor3;
	}

	public float getTaxaManutencaoMensal() {
		return taxaManutencaoMensal;
	}

	public void setTaxaManutencaoMensal(float taxaManutencaoMensal) {
		this.taxaManutencaoMensal = taxaManutencaoMensal;
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
}
