package br.com.marcielli.bancom.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PagamentoFatura {
    private Long id;
    private Long faturaId;
    private Long cartaoId;
    private Long contaId;
    private BigDecimal valorPago;
    private LocalDateTime dataPagamento;
    
    public PagamentoFatura() {}
    
    public PagamentoFatura(Long faturaId, Long cartaoId, Long contaId, BigDecimal valorPago) {
        this.faturaId = faturaId;
        this.cartaoId = cartaoId;
        this.contaId = contaId;
        this.valorPago = valorPago;
        this.dataPagamento = LocalDateTime.now();
    }
    
}