package br.com.marcielli.BancoM.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.BancoM.entity.Cartao;

@Component
public class CartaoUpdateMapper {
	
	@Autowired
	private ModelMapper mapper;

	public Cartao toEntity(CartaoUpdateDTO dto) {
		Cartao entity = mapper.map(dto, Cartao.class);
		return entity;
	}

	public CartaoUpdateResponseDTO toDTO(Cartao entity) {
		CartaoUpdateResponseDTO dto = mapper.map(entity, CartaoUpdateResponseDTO.class);
		return dto;
	}

	public List<CartaoUpdateResponseDTO> toDTO(List<Cartao> cartoes) {
		return cartoes.stream().map(cartao -> toDTO(cartao)).collect(Collectors.toList());
	}

}
