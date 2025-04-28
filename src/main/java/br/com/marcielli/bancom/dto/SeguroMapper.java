package br.com.marcielli.bancom.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Seguro;

@Component
public class SeguroMapper {

	@Autowired
	private ModelMapper mapper;

	public Seguro toEntity(SeguroCreateDTO dto) {
		Seguro entity = mapper.map(dto, Seguro.class);
		return entity;
	}

	public SeguroResponseDTO toDTO(Seguro entity) {
		SeguroResponseDTO dto = mapper.map(entity, SeguroResponseDTO.class);
		return dto;
	}

	public List<SeguroResponseDTO> toDTO(List<Seguro> seguros) {
		return seguros.stream().map(seguro -> toDTO(seguro)).collect(Collectors.toList());
	}
}
