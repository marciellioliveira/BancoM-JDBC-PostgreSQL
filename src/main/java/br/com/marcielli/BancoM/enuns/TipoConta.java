package br.com.marcielli.BancoM.enuns;

public enum TipoConta {
	
	CORRENTE(1, "CORRENTE"),
	POUPANCA(2, "POUPANCA");
	
	private final int cod;
	private final String descricao;
	
	private TipoConta(int cod, String descricao) {
		this.cod = cod;
		this.descricao = descricao;
	}

	public int getCod() {
		return cod;
	}

	public String getDescricao() {
		return descricao;
	}
	
	
}
