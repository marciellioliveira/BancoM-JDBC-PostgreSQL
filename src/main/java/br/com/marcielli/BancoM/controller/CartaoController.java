package br.com.marcielli.BancoM.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.service.CartaoService;

@RestController
@RequestMapping("/cartao")
public class CartaoController {
	
	@Autowired
	private CartaoService cartaoService;
	
	@PostMapping("/salvar/{idCartao}")
	public ResponseEntity<String> adicionarCartao(@PathVariable("idCartao") Long idCartao, @RequestBody Cartao cartao) {
		
		Cartao cartaoAdicionado = cartaoService.saveCartao(idCartao, cartao);
		
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

//	@GetMapping("/listar/{cartaoId}")
//	public Optional<Conta> getContaById(@PathVariable("cartaoId") Long cartaoId) {
//
//		Optional<Cartao> cartaoById = cartaoService.getCartaoById(cartaoId);
//
//		if (!cartaoById.isPresent()) {
//			throw new ClienteNaoEncontradoException("Cliente não existe no banco.");
//		}
//
//		return cartaoById;
//	}

}
