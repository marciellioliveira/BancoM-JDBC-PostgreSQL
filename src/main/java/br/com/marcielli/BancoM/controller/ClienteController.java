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
import br.com.marcielli.BancoM.dto.EnderecoCreateDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.service.ClienteService;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private ClienteMapper clienteMapper;
	
	@PostMapping("/salvar")
	public ResponseEntity<ClienteResponseDTO> adicionarCliente(@RequestBody ClienteCreateDTO clienteCreateDTO) { //Pega o ClienteCreateDTO do Json
		
		//Pegar o clienteCreateDTO e transformá-lo em uma entidade
		Cliente cliente = clienteMapper.toEntity(clienteCreateDTO);
		
		if(cliente.getEndereco() == null) {
			
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
		
		//Pegar o objeto/entidade e pedir para salvar no BD usando clienteService.
		Cliente clienteGravado = clienteService.save(cliente);
				
		//Pegar o objeto clienteGravado já com ID e devolver para o cliente informando que foi gravado no Banco, mas primeiro transformando em DTO
		ClienteResponseDTO clienteResponseDTO = clienteMapper.toDTO(clienteGravado);
		
		//Agora que já está mapeado, retornamos "clienteResponseDTO" para o cliente.
		return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponseDTO);
	
	}	
	
//	@PostMapping("/salvar")
//	public ResponseEntity<String> adicionarCliente(@RequestBody Cliente cliente) {
//		
//		Cliente clienteAdicionado = clienteService.save(cliente);
//		
//		if(clienteAdicionado != null) {
//			return new ResponseEntity<String>("Cliente "+cliente.getNome()+" adicionado com sucesso", HttpStatus.CREATED);
//		} else {
//			return new ResponseEntity<String>("Dados do "+cliente.getNome()+" inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}		
//	}	
	
	@GetMapping("/listar")
	public ResponseEntity<List<Cliente>> getClientes(){
		List<Cliente> clientes = clienteService.getAll();
		return new ResponseEntity<List<Cliente>>(clientes, HttpStatus.OK);			
	}
	
	@GetMapping("/listar/{clienteId}")
	public Optional<Cliente> getClienteById(@PathVariable("clienteId") Long clienteId){
		
		Optional<Cliente> clienteById = clienteService.getClienteById(clienteId);
		
		if(!clienteById.isPresent()) {
			throw new ClienteNaoEncontradoException("Cliente não existe no banco.");
		}
		
		return clienteById;
		
	}
	
	@PutMapping("/atualizar/{clienteId}")
    public ResponseEntity<String>  atualizar(@PathVariable("clienteId") Long clienteId, @RequestBody Cliente cliente) {
		
		Cliente clienteAtualizado = clienteService.update(cliente, clienteId);
		
		if(clienteAtualizado != null) {
			return new ResponseEntity<String>("O cliente " +clienteAtualizado.getNome() + " foi atualizado com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
		
	}
	
	@DeleteMapping("/deletar/{clienteId}")
    public ResponseEntity<String> deletar(@PathVariable("clienteId") Long clienteId) {
		
		boolean clienteDeletado = clienteService.delete(clienteId); 
		
		if(clienteDeletado) {
			return new ResponseEntity<String>("Cliente deletado com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
    }
}
