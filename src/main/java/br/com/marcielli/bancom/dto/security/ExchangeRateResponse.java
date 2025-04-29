package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonFormat(shape = JsonFormat.Shape.STRING)
public class ExchangeRateResponse {

	 private BigDecimal conversionRate;

}
