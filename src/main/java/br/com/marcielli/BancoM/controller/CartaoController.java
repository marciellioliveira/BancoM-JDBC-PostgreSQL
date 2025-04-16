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

import br.com.marcielli.BancoM.dto.CartaoCreateDTO;
import br.com.marcielli.BancoM.dto.CartaoCreateTedDTO;
import br.com.marcielli.BancoM.dto.CartaoDeleteDTO;
import br.com.marcielli.BancoM.dto.CartaoMapper;
import br.com.marcielli.BancoM.dto.CartaoResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateLimiteDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateLimiteMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateLimiteResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateSenhaDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateSenhaMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateSenhaResponseDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateStatusDTO;
import br.com.marcielli.BancoM.dto.CartaoUpdateStatusMapper;
import br.com.marcielli.BancoM.dto.CartaoUpdateStatusResponseDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.service.CartaoService;

@RestController
@RequestMapping("/cartoes")
public class CartaoController {
	
	@Autowired
	private CartaoService cartaoService;	
	
	@Autowired
	private CartaoMapper cartaoMapper;
	
	@Autowired
	private CartaoUpdateMapper cartaoUpdateMapper;
	
	@Autowired
	private CartaoUpdateLimiteMapper cartaoUpdateLimiteMapper;
	
	@Autowired
	private CartaoUpdateStatusMapper cartaoUpdateStatusMapper;
	
	@Autowired
	private CartaoUpdateSenhaMapper cartaoUpdateSenhaMapper;
		
	@PostMapping("") //@PostMapping("/salvar") - salvar - Criar um novo cartão
	public ResponseEntity<CartaoResponseDTO> adicionarCartao(@RequestBody CartaoCreateDTO cartaoCreateDTO) {		

		//Cartao cartao = cartaoMapper.toEntity(cartaoCreateDTO);

		Cartao cartaoGravado = cartaoService.save(cartaoCreateDTO);

		CartaoResponseDTO cartaoResponseDTO = cartaoMapper.toDTO(cartaoGravado);

		return ResponseEntity.status(HttpStatus.CREATED).body(cartaoResponseDTO);
	}	
	
	@GetMapping("/{cartaoId}") //@GetMapping("/listar/{cartaoId}") - Obter detalhes de um cartão
	public Optional<Cartao> getCartaoById(@PathVariable("cartaoId") Long cartaoId) {

		Optional<Cartao> cartaoById = cartaoService.getCartaoById(cartaoId);

		if (!cartaoById.isPresent()) {
			throw new CartaoNaoEncontradoException("Cartão não existe no banco.");
		}

		return cartaoById;
	}
	
	@PutMapping("/{cartaoId}") //@PutMapping("/atualizar/{cartaoId}") - Atualizar
	public ResponseEntity<CartaoUpdateResponseDTO> atualizar(@PathVariable("cartaoId") Long cartaoId, @RequestBody CartaoUpdateDTO cartaoUpdateDTO) {

		//Conta conta = cartaoMapper.toEntity(cartaoCreateDTO);

		Cartao cartaoAtualizado = cartaoService.update(cartaoId, cartaoUpdateDTO);

		CartaoUpdateResponseDTO cartaoResponseDTO = cartaoUpdateMapper.toDTO(cartaoAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}
	
	@DeleteMapping("/{cartaoId}") //@DeleteMapping("/deletar/{cartaoId}") - Deletar
	public ResponseEntity<String> deletar(@PathVariable("cartaoId") Long cartaoId, @RequestBody CartaoDeleteDTO cartaoDeleteDTO) {
		
		boolean cartaoDeletado = cartaoService.deleteCartao(cartaoId, cartaoDeleteDTO);
		
		if (cartaoDeletado) {
			return new ResponseEntity<String>("Cartão deletado com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do cartão são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
		}
	}
		
	@GetMapping("") //@GetMapping("/listar")
	public ResponseEntity<List<Cartao>> getContas() {
		List<Cartao> cartoes = cartaoService.getAll();
		return new ResponseEntity<List<Cartao>>(cartoes, HttpStatus.OK);
	}

	
	@PostMapping("/{idContaReceber}/pagamento")
	public ResponseEntity<String> pagamentoCartao(@PathVariable("idContaReceber") Long idContaReceber, @RequestBody CartaoCreateTedDTO cartaoTransCreateDTO) {
		
		boolean pagamentoRealizado = cartaoService.pagCartao(idContaReceber, cartaoTransCreateDTO);
		
		if(pagamentoRealizado) {
			return new ResponseEntity<String>("Pagamento realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}	
	
	
	@PutMapping("/{cartaoId}/limite") //@PutMapping("/atualizar/{cartaoId}") - Alterar limite do cartão de crédito
	public ResponseEntity<CartaoUpdateLimiteResponseDTO> alterarLimiteCartaoCredito(@PathVariable("cartaoId") Long cartaoId, @RequestBody CartaoUpdateLimiteDTO cartaoUpdateLimiteDTO) {

		//Conta conta = cartaoMapper.toEntity(cartaoCreateDTO);
		System.out.println();
		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoCredito(cartaoId, cartaoUpdateLimiteDTO);

		CartaoUpdateLimiteResponseDTO cartaoResponseDTO = cartaoUpdateLimiteMapper.toDTO(limiteAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}
	
	@PutMapping("/{cartaoId}/status") //@PutMapping("/atualizar/{cartaoId}") - Alterar status do cartao
	public ResponseEntity<CartaoUpdateStatusResponseDTO> alterarStatusCartao(@PathVariable("cartaoId") Long cartaoId, @RequestBody CartaoUpdateStatusDTO cartaoUpdateStatusDTO) {

		//Conta conta = cartaoMapper.toEntity(cartaoCreateDTO);

		Cartao statusAtualizado = cartaoService.alterarStatusC(cartaoId, cartaoUpdateStatusDTO);

		CartaoUpdateStatusResponseDTO cartaoResponseDTO = cartaoUpdateStatusMapper.toDTO(statusAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}
	
	@PutMapping("/{cartaoId}/senha") //Alterar senha do cartao 
	public ResponseEntity<CartaoUpdateSenhaResponseDTO> alterarSenhaCartao(@PathVariable("cartaoId") Long cartaoId, @RequestBody CartaoUpdateSenhaDTO cartaoUpdateSenhaDTO) {

		//Conta conta = cartaoMapper.toEntity(cartaoCreateDTO);

		Cartao statusAtualizado = cartaoService.alterarSenhaC(cartaoId, cartaoUpdateSenhaDTO);

		CartaoUpdateSenhaResponseDTO cartaoResponseDTO = cartaoUpdateSenhaMapper.toDTO(statusAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}

	
	@PutMapping("/{cartaoId}/limite-diario") //@PutMapping("/atualizar/{cartaoId}") - Alterar limite diario do cartão de débito
	public ResponseEntity<CartaoUpdateLimiteResponseDTO> alterarLimiteCartaoDebito(@PathVariable("cartaoId") Long cartaoId, @RequestBody CartaoUpdateLimiteDTO cartaoUpdateLimiteDTO) {

		//Conta conta = cartaoMapper.toEntity(cartaoCreateDTO);

		Cartao limiteAtualizado = cartaoService.alterarLimiteCartaoCredito(cartaoId, cartaoUpdateLimiteDTO);

		CartaoUpdateLimiteResponseDTO cartaoResponseDTO = cartaoUpdateLimiteMapper.toDTO(limiteAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(cartaoResponseDTO);

	}
	

	
	
	
	
	
	

}
