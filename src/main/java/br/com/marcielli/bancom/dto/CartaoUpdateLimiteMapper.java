package br.com.marcielli.bancom.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Cartao;

@Component
public class CartaoUpdateLimiteMapper {

	@Autowired
	private ModelMapper mapper;

	public Cartao toEntity(CartaoUpdateLimiteDTO dto) {
		Cartao entity = mapper.map(dto, Cartao.class);
		return entity;
	}

	public CartaoUpdateLimiteResponseDTO toDTO(Cartao entity) {
		CartaoUpdateLimiteResponseDTO dto = mapper.map(entity, CartaoUpdateLimiteResponseDTO.class);
		return dto;
	}

	public List<CartaoUpdateLimiteResponseDTO> toDTO(List<Cartao> cartoes) {
		return cartoes.stream().map(cartao -> toDTO(cartao)).collect(Collectors.toList());
	}
}
