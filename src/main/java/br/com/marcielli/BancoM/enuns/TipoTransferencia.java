package br.com.marcielli.BancoM.enuns;

public enum TipoTransferencia {
	
	TED(1, "TED"),
	PIX(2, "PIX"),
	DEPOSITO(3, "DEPOSITO"),
	SAQUE(4, "SAQUE"),
	CARTAO_DEBITO(1, "DEBITO"),
	CARTAO_CREDITO(1, "CREDITO");
		
	private final int cod;
	private final String descricao;
	
	private TipoTransferencia(int cod, String descricao) {
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
