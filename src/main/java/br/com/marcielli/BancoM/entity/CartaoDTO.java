package br.com.marcielli.BancoM.entity;

import br.com.marcielli.BancoM.enuns.TipoCartao;

public class CartaoDTO {
	
	private Long clienteId;
    private TipoCartao tipoCartao;
    private Long contaId;
    
	public CartaoDTO(Long clienteId, TipoCartao tipoCartao, Long contaId) {
		super();
		this.clienteId = clienteId;
		this.tipoCartao = tipoCartao;
		this.contaId = contaId;
	}
	
	public Long getClienteId() {
		return clienteId;
	}
	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}
	public TipoCartao getTipoCartao() {
		return tipoCartao;
	}
	public void setTipoCartao(TipoCartao tipoCartao) {
		this.tipoCartao = tipoCartao;
	}
	public Long getContaId() {
		return contaId;
	}
	public void setContaId(Long contaId) {
		this.contaId = contaId;
	} 
    
    
    
    


}
