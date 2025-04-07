package br.com.marcielli.BancoM.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	@JsonIgnore
	private CategoriaConta categoria; // Comum, super ou premium

	@Enumerated(EnumType.STRING)
	@JsonIgnore
	private TipoConta tipoConta; // Corrente ou Poupança

	// Taxas da Poupança
	private float taxaAcrescRend;
	private float taxaMensal;

	// Taxas da Corrente
	private float taxaManutencaoMensal;

	@ManyToOne
	@JoinColumn(name = "conta")
	@JsonInclude
	@Transient // Retirar do banco com @Transient mas incluir no Json para serializar com
				// @JsonInclude
	private Conta contas;

	public Taxas() {
	}

	// Testando
	public Taxas(float saldoConta, TipoConta tipoConta) {

		if (tipoConta == TipoConta.CORRENTE) {

			this.tipoConta = TipoConta.CORRENTE;

			if (saldoConta <= 1000f) {

				this.categoria = CategoriaConta.COMUM;
				this.taxaManutencaoMensal = 12.00f;

			}

			if (saldoConta > 1000f && saldoConta <= 5000f) {

				this.categoria = CategoriaConta.SUPER;
				this.taxaManutencaoMensal = 8.00f;

			}

			if (saldoConta > 5000f) {

				this.categoria = CategoriaConta.PREMIUM;
				this.taxaManutencaoMensal = 0f;

			}
		}

		if (tipoConta == TipoConta.POUPANCA) {

			this.tipoConta = TipoConta.POUPANCA;

			if (saldoConta <= 1000f) {

				this.categoria = CategoriaConta.COMUM;
				this.taxaAcrescRend = 0.005f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);

			}

			if (saldoConta > 1000f && saldoConta <= 5000f) {

				this.categoria = CategoriaConta.SUPER;
				this.taxaAcrescRend = 0.007f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);

			}

			if (saldoConta > 5000f) {

				this.categoria = CategoriaConta.PREMIUM;
				this.taxaAcrescRend = 0.009f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);

			}

		}

	}

	public Taxas(CategoriaConta categoria, TipoConta tipoConta, float taxaAcrescRend, float taxaMensal,
			float taxaManutencaoMensal) {
		super();
		this.categoria = categoria;
		this.tipoConta = tipoConta;
		this.taxaAcrescRend = taxaAcrescRend;
		this.taxaMensal = taxaMensal;
		this.taxaManutencaoMensal = taxaManutencaoMensal;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conta atualizarTaxas(Taxas taxasAtualizadas, Conta dadosParaAtualizar, Conta contaParaSerAtualizada) {
		
		contaParaSerAtualizada.setSaldoConta(dadosParaAtualizar.getSaldoConta());
		
		//contaParaSerAtualizada.getTaxas().removeFirst();

		
	
		if (dadosParaAtualizar.getTipoConta() == TipoConta.CORRENTE) {

			this.tipoConta = TipoConta.CORRENTE;

			if (dadosParaAtualizar.getSaldoConta() <= 1000f) {

				this.categoria = CategoriaConta.COMUM;
				this.taxaManutencaoMensal = 12.00f;
			
			}

			if (dadosParaAtualizar.getSaldoConta() > 1000f && dadosParaAtualizar.getSaldoConta() <= 5000f) {

				this.categoria = CategoriaConta.SUPER;
				this.taxaManutencaoMensal = 8.00f;
				
				

			}

			if (dadosParaAtualizar.getSaldoConta() > 5000f) {

				this.categoria = CategoriaConta.PREMIUM;
				this.taxaManutencaoMensal = 0f;

			}
			

			//flagAtualizou = true;
		}

		if (dadosParaAtualizar.getTipoConta()  == TipoConta.POUPANCA) {

			this.tipoConta = TipoConta.POUPANCA;

			if (dadosParaAtualizar.getSaldoConta() <= 1000f) {

				this.categoria = CategoriaConta.COMUM;
				this.taxaAcrescRend = 0.005f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);

			}

			if (dadosParaAtualizar.getSaldoConta() > 1000f && dadosParaAtualizar.getSaldoConta() <= 5000f) {

				this.categoria = CategoriaConta.SUPER;
				this.taxaAcrescRend = 0.007f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);

			}

			if (dadosParaAtualizar.getSaldoConta() > 5000f) {

				this.categoria = CategoriaConta.PREMIUM;
				this.taxaAcrescRend = 0.009f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);

			}
						
			taxasAtualizadas.setTaxaManutencaoMensal(taxaManutencaoMensal);
			taxasAtualizadas.setCategoria(categoria);
			taxasAtualizadas.setTaxaAcrescRend(taxaAcrescRend);
			taxasAtualizadas.setTaxaMensal(taxaMensal);
			
			List<Taxas> novasTaxas = new ArrayList<Taxas>();
			novasTaxas.add(taxasAtualizadas);
		
			//contaParaSerAtualizada.getTaxas().removeFirst();
			contaParaSerAtualizada.setTaxas(novasTaxas);
		}
		return contaParaSerAtualizada;
				
	}
	
	public Conta atualizarTaxas(Conta contaParaSerAtualizada) {
		
			//Teria que receber aqui as duas contas para atualizar? mas pçq a poupança da certo?
		if (contaParaSerAtualizada.getTipoConta() == TipoConta.CORRENTE) {
			
			ContaCorrente cc = (ContaCorrente)contaParaSerAtualizada;		

			this.tipoConta = TipoConta.CORRENTE;

			if (contaParaSerAtualizada.getSaldoConta() <= 1000f) {

				this.categoria = CategoriaConta.COMUM;
				this.taxaManutencaoMensal = 12.00f;		
			}

			if (contaParaSerAtualizada.getSaldoConta() > 1000f && contaParaSerAtualizada.getSaldoConta() <= 5000f) {

				this.categoria = CategoriaConta.SUPER;
				this.taxaManutencaoMensal = 8.00f;		
			}

			if (contaParaSerAtualizada.getSaldoConta() > 5000f) {

				this.categoria = CategoriaConta.PREMIUM;
				this.taxaManutencaoMensal = 0f;
		
			}
			
			cc.setCategoriaConta(categoria);
			cc.setTaxaManutencaoMensal(taxaManutencaoMensal);
			
			
			return cc;
			
		}

		if (contaParaSerAtualizada.getTipoConta()  == TipoConta.POUPANCA) {
			
			ContaPoupanca pp = (ContaPoupanca)contaParaSerAtualizada;	

			this.tipoConta = TipoConta.POUPANCA;

			if (contaParaSerAtualizada.getSaldoConta() <= 1000f) {

				this.categoria = CategoriaConta.COMUM;
				this.taxaAcrescRend = 0.005f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);
				

			}

			if (contaParaSerAtualizada.getSaldoConta() > 1000f && contaParaSerAtualizada.getSaldoConta() <= 5000f) {

				this.categoria = CategoriaConta.SUPER;
				this.taxaAcrescRend = 0.007f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);
				

			}

			if (contaParaSerAtualizada.getSaldoConta() > 5000f) {

				this.categoria = CategoriaConta.PREMIUM;
				this.taxaAcrescRend = 0.009f;
				this.taxaMensal = (float) (Math.pow(1 + taxaAcrescRend, 1.0 / 12) - 1);
				
				

			}
						
			pp.setCategoriaConta(categoria);
			pp.setTaxaAcrescRend(taxaAcrescRend);
			pp.setTaxaMensal(taxaMensal);
			
			return pp;
		}
		
		return null;
				
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

	public void setTaxaManutencaoMensal(float taxaManutencaoMensal) {

		this.taxaManutencaoMensal = taxaManutencaoMensal;
//		if (saldo <= 1000f) {
//			this.taxaManutencaoMensal = 12.00f;
//		}
//
//		if (saldo > 1000f && saldo <= 5000f) {
//			this.taxaManutencaoMensal = 8.00f;
//		}
//
//		if (saldo > 5000f) {
//			this.taxaManutencaoMensal = 0f;
//		}
	}

	@Override
	public String toString() {
		return "Taxas [id=" + id + ", version=" + version + ", categoria=" + categoria + ", tipoConta=" + tipoConta
				+ ", taxaAcrescRend=" + taxaAcrescRend + ", taxaMensal=" + taxaMensal + ", taxaManutencaoMensal="
				+ taxaManutencaoMensal + ", contas=" + contas + "]";
	}
	
	
}
