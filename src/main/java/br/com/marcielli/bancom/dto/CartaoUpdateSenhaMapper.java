package br.com.marcielli.bancom.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Cartao;

@Component
public class CartaoUpdateSenhaMapper {
	
	@Autowired
	private ModelMapper mapper;

	public Cartao toEntity(CartaoUpdateSenhaDTO dto) {
		Cartao entity = mapper.map(dto, Cartao.class);
		return entity;
	}

	public CartaoUpdateSenhaResponseDTO toDTO(Cartao entity) {
		CartaoUpdateSenhaResponseDTO dto = mapper.map(entity, CartaoUpdateSenhaResponseDTO.class);
		return dto;
	}

	public List<CartaoUpdateSenhaResponseDTO> toDTO(List<Cartao> cartoes) {
		return cartoes.stream().map(cartao -> toDTO(cartao)).collect(Collectors.toList());
	}

}
