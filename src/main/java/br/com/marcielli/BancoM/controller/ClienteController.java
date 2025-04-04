package br.com.marcielli.BancoM.controller;

import java.util.List;

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
	
	@PutMapping("/atualizar/{clienteId}")
    public Cliente atualizar(@PathVariable("clienteId") Long clienteId, @RequestBody Cliente cliente) {        
		return clienteService.update(cliente, clienteId);
	}
	
	@DeleteMapping("/deletar/{clienteId}")
    public String deletar(@PathVariable("clienteId") Long clienteId) {
		return clienteService.delete(clienteId);
    }
}
