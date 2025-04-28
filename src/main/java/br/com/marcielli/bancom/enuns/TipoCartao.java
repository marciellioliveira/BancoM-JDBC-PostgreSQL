package br.com.marcielli.bancom.enuns;

public enum TipoCartao {

	CREDITO(1, "CRÉDITO"),
	DEBITO(2, "DÉBITO");
	
	private final int cod;
	private final String descricao;
	
	private TipoCartao(int cod, String descricao) {
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
