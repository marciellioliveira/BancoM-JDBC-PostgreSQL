package br.com.marcielli.BancoM.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.CartaoMapper;
import br.com.marcielli.BancoM.dto.CartaoResponseDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.service.CartaoService;

@RestController
@RequestMapping("/cartao")
public class CartaoController {
	
	@Autowired
	private CartaoService cartaoService;	
	
	@Autowired
	private CartaoMapper cartaoMapper;
		
	@PostMapping("/salvar")
	public ResponseEntity<CartaoResponseDTO> adicionarCartao(@RequestBody CartaoCreateDTO cartaoCreateDTO) {		

		//Cartao cartao = cartaoMapper.toEntity(cartaoCreateDTO);

		Cartao cartaoGravado = cartaoService.save(cartaoCreateDTO);

		CartaoResponseDTO cartaoResponseDTO = cartaoMapper.toDTO(cartaoGravado);

		return ResponseEntity.status(HttpStatus.CREATED).body(cartaoResponseDTO);
	}	
	
	
	
	
	
	
	
	
	
//	@PostMapping("/salvar")
//	public ResponseEntity<String> adicionarCartao(@RequestBody Cartao cartao) {
//		
//		Cartao cartaoAdicionado = cartaoService.saveCartao(cartao);		
//		
//		if (cartaoAdicionado != null) {
//			
//			return new ResponseEntity<String>("O cartão foi criado com sucesso.", HttpStatus.CREATED);
//			
//		} else {
//			
//			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//	
//	@GetMapping("/listar")
//	public ResponseEntity<List<Cartao>> getContas() {
//		List<Cartao> cartoes = cartaoService.getAll();
//		return new ResponseEntity<List<Cartao>>(cartoes, HttpStatus.OK);
//	}
//
//	@GetMapping("/listar/{cartaoId}")
//	public Optional<Cartao> getCartaoById(@PathVariable("cartaoId") Long cartaoId) {
//
//		Optional<Cartao> cartaoById = cartaoService.getCartaoById(cartaoId);
//
//		if (!cartaoById.isPresent()) {
//			throw new CartaoNaoEncontradoException("Cartão não existe no banco.");
//		}
//
//		return cartaoById;
//	}
//	
//	@PutMapping("/atualizar/{cartaoAtualizar}")
//	public ResponseEntity<String> atualizar(@PathVariable("cartaoAtualizar") Long cartaoAtualizar, @RequestBody Cartao cartaoDadosInserir) {
//			
//		Cartao cartaoAtualizado = cartaoService.update(cartaoAtualizar, cartaoDadosInserir);
//		
//		if (cartaoAtualizado != null) {
//			
//			return new ResponseEntity<String>("O cartão " + cartaoAtualizado.getNumeroCartao() + " foi atualizado com sucesso.", HttpStatus.OK);
//			
//		} else {
//			
//			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
//		}
//
//	}
//	
//	@DeleteMapping("/deletar/{cartaoId}")
//	public ResponseEntity<String> deletar(@RequestBody Cartao cartaoDeletar, @PathVariable("cartaoId") Long cartaoId) {
//		
//		boolean cartaoDeletado = cartaoService.deleteCartao(cartaoDeletar, cartaoId);
//		
//		if (cartaoDeletado) {
//			return new ResponseEntity<String>("Cartão deletado com sucesso", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
//		}
//	}
//		
//
//	// Pagamento Cartão
//	@PostMapping("/pagamento/{idClienteReceber}/{idContaReceber}")
//	public ResponseEntity<String> pagamentoCartao(@PathVariable("idClienteReceber") Long idClienteReceber, @PathVariable("idContaReceber") Long idContaReceber, @RequestBody Transferencia contaEnviar) {
//		
//		boolean pagamento = cartaoService.pagarCartao(idClienteReceber,idContaReceber, contaEnviar);
//
//		if (pagamento) {
//
//			return new ResponseEntity<String>("Pagamento realizado com sucesso.", HttpStatus.CREATED);
//
//		} else {
//
//			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//	
//	
//	
//	
	
	
	
//	@PostMapping("/pagamento/{idClienteReceber}/{idContaReceber}")
//	public ResponseEntity<String> transferirPagarCartao(@PathVariable("idClienteReceber") Long idClienteReceber, @PathVariable("idContaReceber") Long idContaReceber, @RequestBody Transferencia valorDepositar) {
//				
//		boolean pagamento = cartaoService.pagarCartao(idClienteReceber, idContaReceber, valorDepositar);
//
//		if (pagamento) {
//
//			return new ResponseEntity<String>("Pagamento realizado com sucesso.", HttpStatus.OK);
//
//		} else {
//
//			return new ResponseEntity<String>("Dados inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
	
	
	
	
	
	
	
	
	
	
	
	

}
