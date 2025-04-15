package br.com.marcielli.BancoM.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.ClienteCreateDTO;
import br.com.marcielli.BancoM.dto.ClienteMapper;
import br.com.marcielli.BancoM.dto.ClienteResponseDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.service.ClienteService;

@RestController
@RequestMapping("/clientes") //cliente
public class ClienteController {

	@Autowired
	private ClienteService clienteService;

	@Autowired
	private ClienteMapper clienteMapper;

	@PostMapping("") //salvar - Criar um novo cliente
	public ResponseEntity<ClienteResponseDTO> adicionarCliente(@RequestBody ClienteCreateDTO clienteCreateDTO) { 
		// Pega o Cliente do JSON

		// Pegar o clienteCreateDTO e transformá-lo em uma entidade
		Cliente cliente = clienteMapper.toEntity(clienteCreateDTO);

		if (cliente.getEndereco() == null) {

			Endereco endereco = new Endereco();
			endereco.setCep(clienteCreateDTO.getCep());
			endereco.setEstado(clienteCreateDTO.getEstado());
			endereco.setCidade(clienteCreateDTO.getCidade());
			endereco.setBairro(clienteCreateDTO.getBairro());
			endereco.setRua(clienteCreateDTO.getRua());
			endereco.setNumero(clienteCreateDTO.getNumero());
			endereco.setComplemento(clienteCreateDTO.getComplemento());

			cliente.setEndereco(endereco);
		}

		// Pegar o objeto/entidade e pedir para salvar no BD usando clienteService.
		Cliente clienteGravado = clienteService.save(cliente);

		// Pegar o objeto clienteGravado já com ID e devolver para o cliente informando
		// que foi gravado no Banco, mas primeiro transformando em DTO
		ClienteResponseDTO clienteResponseDTO = clienteMapper.toDTO(clienteGravado);

		// Agora que já está mapeado, retornamos "clienteResponseDTO" para o cliente.
		return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponseDTO);

	}
	
	@GetMapping("/{clienteId}") //listar/{clienteId} - Obter detalhes de um cliente
	public Optional<Cliente> getClienteById(@PathVariable("clienteId") Long clienteId) {

		Optional<Cliente> clienteById = clienteService.getClienteById(clienteId);

		if (!clienteById.isPresent()) {
			throw new ClienteNaoEncontradoException("Cliente não existe no banco.");
		}

		return clienteById;

	}

	@PutMapping("/{clienteId}") //atualizar/{clienteId} - Atualizar informações de um cliente
	public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable("clienteId") Long clienteId,
			@RequestBody ClienteCreateDTO clienteCreateDTO) {

		Cliente cliente = clienteMapper.toEntity(clienteCreateDTO);

		Endereco endereco = new Endereco();
		endereco.setCep(clienteCreateDTO.getCep());
		endereco.setEstado(clienteCreateDTO.getEstado());
		endereco.setCidade(clienteCreateDTO.getCidade());
		endereco.setBairro(clienteCreateDTO.getBairro());
		endereco.setRua(clienteCreateDTO.getRua());
		endereco.setNumero(clienteCreateDTO.getNumero());
		endereco.setComplemento(clienteCreateDTO.getComplemento());

		cliente.setEndereco(endereco);

		Cliente clienteAtualizado = clienteService.update(clienteId, cliente);

		ClienteResponseDTO clienteResponseDTO = clienteMapper.toDTO(clienteAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(clienteResponseDTO);

	}
	
	@DeleteMapping("/{clienteId}") //deletar/{clienteId} - Remover um cliente
	public ResponseEntity<String> deletar(@PathVariable("clienteId") Long clienteId) {

		boolean clienteDeletado = clienteService.delete(clienteId);

		if (clienteDeletado) {
			return new ResponseEntity<String>("Cliente deletado com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("") //listar
	public ResponseEntity<List<Cliente>> getClientes() {
		List<Cliente> clientes = clienteService.getAll();
		return new ResponseEntity<List<Cliente>>(clientes, HttpStatus.OK);
	}	
}
