package br.com.marcielli.bancom.enuns;

public enum CategoriaConta {
	
	COMUM(1, "COMUM"),
	SUPER(2, "SUPER"),
	PREMIUM(3, "PREMIUM");
	
	private final int cod;
	private final String descricao;
	
	private CategoriaConta(int cod, String descricao) {
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
