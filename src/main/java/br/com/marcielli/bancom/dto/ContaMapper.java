package br.com.marcielli.bancom.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.bancom.entity.Conta;

@Component
public class ContaMapper {

	@Autowired
	private ModelMapper mapper;

	public Conta toEntity(ContaCreateDTO dto) {
		Conta entity = mapper.map(dto, Conta.class);
		return entity;
	}

	public ContaResponseDTO toDTO(Conta entity) {
		ContaResponseDTO dto = mapper.map(entity, ContaResponseDTO.class);
		return dto;
	}

	public List<ContaResponseDTO> toDTO(List<Conta> contas) {
		return contas.stream().map(conta -> toDTO(conta)).collect(Collectors.toList());
	}
	
}
