package br.com.marcielli.BancoM.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.BancoM.entity.Cartao;

@Component
public class CartaoMapper {

	@Autowired
	private ModelMapper mapper;

	public Cartao toEntity(CartaoCreateDTO dto) {
		Cartao entity = mapper.map(dto, Cartao.class);
		return entity;
	}

	public CartaoResponseDTO toDTO(Cartao entity) {
		CartaoResponseDTO dto = mapper.map(entity, CartaoResponseDTO.class);
		return dto;
	}

	public List<CartaoResponseDTO> toDTO(List<Cartao> cartoes) {
		return cartoes.stream().map(cartao -> toDTO(cartao)).collect(Collectors.toList());
	}
	
	
}
