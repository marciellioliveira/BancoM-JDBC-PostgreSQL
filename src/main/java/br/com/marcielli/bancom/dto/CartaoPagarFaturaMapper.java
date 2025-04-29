package br.com.marcielli.BancoM.dto;

import java.util.List;
import java.util.stream.Collectors;

import br.com.marcielli.bancom.dto.CartaoPagarFaturaDTO;
import br.com.marcielli.bancom.dto.CartaoPagarFaturaResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Cartao;

@Component
public class CartaoPagarFaturaMapper {

	private ModelMapper mapper;

	public CartaoPagarFaturaMapper(ModelMapper mapper) {
		this.mapper = mapper;
	}

	public Cartao toEntity(CartaoPagarFaturaDTO dto) {
		Cartao entity = mapper.map(dto, Cartao.class);
		return entity;
	}

	public CartaoPagarFaturaResponseDTO toDTO(Cartao entity) {
		CartaoPagarFaturaResponseDTO dto = mapper.map(entity, CartaoPagarFaturaResponseDTO.class);
		return dto;
	}

	public List<CartaoPagarFaturaResponseDTO> toDTO(List<Cartao> cartoes) {
		return cartoes.stream().map(cartao -> toDTO(cartao)).collect(Collectors.toList());
	}

}
