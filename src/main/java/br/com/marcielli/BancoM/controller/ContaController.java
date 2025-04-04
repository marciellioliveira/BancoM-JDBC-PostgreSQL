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

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.service.ContaService;

@RestController
@RequestMapping("/conta")
public class ContaController {
	
	@Autowired
	private ContaService contaService;
	
	@PostMapping("/salvar")
	public ResponseEntity<String> adicionarCliente(@RequestBody Conta conta) {
			
		Conta contaAdicionada = contaService.save(conta);	
		
		if(contaAdicionada != null) {
			return new ResponseEntity<String>("A conta "+contaAdicionada.getNumeroConta()+" foi adicionada com sucesso", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
		
	}
	
	
	@GetMapping("/listar")
	public ResponseEntity<List<Conta>> getContas(){
		List<Conta> contas = contaService.getAll();
		return new ResponseEntity<List<Conta>>(contas, HttpStatus.OK);			
	}
	
	@GetMapping("/listar/{contaId}")
	public Optional<Conta> getContaById(@PathVariable("contaId") Long contaId){
		
		Optional<Conta> contaById = contaService.getContaById(contaId);
		
		if(!contaById.isPresent()) {
			throw new ClienteNaoEncontradoException("Cliente não existe no banco.");
		}
		
		return contaById;
		
	}
	
	@PutMapping("/atualizar/{contaId}")
    public Conta atualizar(@PathVariable("contaId") Long contaId, @RequestBody Conta conta) {        
		return contaService.update(conta, contaId);
	}
	
	@DeleteMapping("/deletar/{contaId}")
    public ResponseEntity<String> deletar(@PathVariable("contaId") Long contaId) {
		if(contaService.delete(contaId).equals("deletado")) {
			return new ResponseEntity<String>("Conta deletada com sucesso", HttpStatus.OK);
		}
		return null;
		//return contaService.delete(contaId);
    }

}
