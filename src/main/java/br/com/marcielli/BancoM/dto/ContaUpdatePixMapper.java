package br.com.marcielli.BancoM.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.marcielli.BancoM.entity.Conta;

@Component
public class ContaUpdatePixMapper {

	@Autowired
	private ModelMapper mapper;

	public Conta toEntity(ContaUpdatePixDTO dto) {
		Conta entity = mapper.map(dto, Conta.class);
		return entity;
	}

	public ContaUpdatePixResponseDTO toDTO(Conta entity) {
		ContaUpdatePixResponseDTO dto = mapper.map(entity, ContaUpdatePixResponseDTO.class);
		return dto;
	}

	public List<ContaUpdatePixResponseDTO> toDTO(List<Conta> contas) {
		return contas.stream().map(conta -> toDTO(conta)).collect(Collectors.toList());
	}
}
