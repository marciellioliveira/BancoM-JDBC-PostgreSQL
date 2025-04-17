package br.com.marcielli.BancoM.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.BancoM.entity.Cartao;

@Component
public class CartaoConsultarFaturaMapper {
	
	@Autowired
	private ModelMapper mapper;

	public Cartao toEntity(CartaoConsultarFaturaDTO dto) {
		Cartao entity = mapper.map(dto, Cartao.class);
		return entity;
	}

	public CartaoConsultarFaturaResponseDTO toDTO(Cartao entity) {
		CartaoConsultarFaturaResponseDTO dto = mapper.map(entity, CartaoConsultarFaturaResponseDTO.class);
		return dto;
	}

	public List<CartaoConsultarFaturaResponseDTO> toDTO(List<Cartao> cartoes) {
		return cartoes.stream().map(cartao -> toDTO(cartao)).collect(Collectors.toList());
	}

}
