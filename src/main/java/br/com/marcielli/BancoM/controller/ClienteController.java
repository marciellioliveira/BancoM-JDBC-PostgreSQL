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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.service.ClienteService;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

	@Autowired
	private ClienteService clienteService;
	
	@PostMapping("/salvar")
	public ResponseEntity<String> adicionarCliente(@RequestBody Cliente cliente) {
		
		Cliente clienteAdicionado = clienteService.save(cliente);
		
		if(clienteAdicionado != null) {
			return new ResponseEntity<String>("Cliente "+cliente.getNome()+" adicionado com sucesso", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>("Dados do "+cliente.getNome()+" inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}		
	}	
	
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
		
		if(clienteService.delete(clienteId).equals("deletado")) {
			return new ResponseEntity<String>("Cliente deletado com sucesso", HttpStatus.OK);
		} 
		//return clienteService.delete(clienteId);
		return null;
    }
}
