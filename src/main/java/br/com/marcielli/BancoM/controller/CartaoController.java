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

import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.CartaoDTO;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.service.CartaoService;

@RestController
@RequestMapping("/cartao")
public class CartaoController {
	
	@Autowired
	private CartaoService cartaoService;
	
	@PostMapping("/salvar")
	public ResponseEntity<String> adicionarCartao(@RequestBody Cartao cartao) {
		
		Cartao cartaoAdicionado = cartaoService.saveCartao(cartao);		
		
		if (cartaoAdicionado != null) {
			
			return new ResponseEntity<String>("O cartão foi criado com sucesso.", HttpStatus.CREATED);
			
		} else {
			
			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@GetMapping("/listar")
	public ResponseEntity<List<Cartao>> getContas() {
		List<Cartao> cartoes = cartaoService.getAll();
		return new ResponseEntity<List<Cartao>>(cartoes, HttpStatus.OK);
	}

	@GetMapping("/listar/{cartaoId}")
	public Optional<Cartao> getCartaoById(@PathVariable("cartaoId") Long cartaoId) {

		Optional<Cartao> cartaoById = cartaoService.getCartaoById(cartaoId);

		if (!cartaoById.isPresent()) {
			throw new CartaoNaoEncontradoException("Cartão não existe no banco.");
		}

		return cartaoById;
	}
	
	@PutMapping("/atualizar/{cartaoAtualizar}")
	public ResponseEntity<String> atualizar(@PathVariable("cartaoAtualizar") Long cartaoAtualizar, @RequestBody Cartao cartaoDadosInserir) {
			
		Cartao cartaoAtualizado = cartaoService.update(cartaoAtualizar, cartaoDadosInserir);
		
		if (cartaoAtualizado != null) {
			
			return new ResponseEntity<String>("O cartão " + cartaoAtualizado.getNumeroCartao() + " foi atualizado com sucesso.", HttpStatus.OK);
			
		} else {
			
			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
		}

	}
	
	@DeleteMapping("/deletar/{cartaoId}")
	public ResponseEntity<String> deletar(@RequestBody Cartao cartaoDeletar, @PathVariable("cartaoId") Long cartaoId) {
		
		boolean cartaoDeletado = cartaoService.deleteCartao(cartaoDeletar, cartaoId);
		
		if (cartaoDeletado) {
			return new ResponseEntity<String>("Cartão deletado com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
		}
	}
		

	// Pagamento Cartão

	// Transferencia TED
	
	
	
	
	
	
	
	

}
