package br.com.marcielli.BancoM.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.BancoM.entity.Cartao;

@Component
public class CartaoUpdateStatusMapper {
	
	@Autowired
	private ModelMapper mapper;

	public Cartao toEntity(CartaoUpdateStatusDTO dto) {
		Cartao entity = mapper.map(dto, Cartao.class);
		return entity;
	}

	public CartaoUpdateStatusResponseDTO toDTO(Cartao entity) {
		CartaoUpdateStatusResponseDTO dto = mapper.map(entity, CartaoUpdateStatusResponseDTO.class);
		return dto;
	}

	public List<CartaoUpdateStatusResponseDTO> toDTO(List<Cartao> cartoes) {
		return cartoes.stream().map(cartao -> toDTO(cartao)).collect(Collectors.toList());
	}
}
