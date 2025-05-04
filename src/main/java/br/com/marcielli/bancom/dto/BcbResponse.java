package br.com.marcielli.bancom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record BcbResponse() {
	
	public record ConversaoResponse(
	        BigDecimal valor,
	        BigDecimal taxa,
	        LocalDateTime ultimaAtualizacao,
	        LocalDateTime validoAte,
	        String variacao24h,
	        Limite limites
	    ) {}

	    public record Limite(
	        BigDecimal min,
	        BigDecimal max
	    ) {}

	    public record MercadoResponse(
	        String status,
	        String aviso
	    ) {}

	    public record CambioResponse(
	        BigDecimal saldoOriginal,
	        Map<String, ConversaoResponse> conversoes,
	        MercadoResponse mercado
	    ) {}

}
