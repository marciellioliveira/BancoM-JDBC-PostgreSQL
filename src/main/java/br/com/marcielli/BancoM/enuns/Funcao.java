package br.com.marcielli.BancoM.enuns;

public enum Funcao {
	
	PAGADOR(1, "PAGADOR"),
	RECEBEDOR(2, "RECEBEDOR");
		
	private final int cod;
	private final String descricao;
	
	private Funcao(int cod, String descricao) {
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
