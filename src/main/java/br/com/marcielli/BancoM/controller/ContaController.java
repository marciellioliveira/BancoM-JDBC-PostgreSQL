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

import br.com.marcielli.BancoM.dto.ContaCorrenteTaxaManutencaoDTO;
import br.com.marcielli.BancoM.dto.ContaCreateDTO;
import br.com.marcielli.BancoM.dto.ContaCreateDepositoDTO;
import br.com.marcielli.BancoM.dto.ContaCreatePixDTO;
import br.com.marcielli.BancoM.dto.ContaCreateSaqueDTO;
import br.com.marcielli.BancoM.dto.ContaCreateTedDTO;
import br.com.marcielli.BancoM.dto.ContaMapper;
import br.com.marcielli.BancoM.dto.ContaResponseDTO;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.service.ContaService;

@RestController
@RequestMapping("/conta")
public class ContaController {

	@Autowired
	private ContaService contaService;

	@Autowired
	private ContaMapper contaMapper;
	
	@PostMapping("/salvar")
	public ResponseEntity<ContaResponseDTO> adicionarConta(@RequestBody ContaCreateDTO contaCreateDTO) {		

		Conta conta = contaMapper.toEntity(contaCreateDTO);

		Conta contaGravada = contaService.save(conta);

		ContaResponseDTO contaResponseDTO = contaMapper.toDTO(contaGravada);

		return ResponseEntity.status(HttpStatus.CREATED).body(contaResponseDTO);

	}	
	
	@PutMapping("/atualizar/{contaId}")
	public ResponseEntity<ContaResponseDTO> atualizar(@PathVariable("contaId") Long contaId, @RequestBody ContaCreateDTO contaCreateDTO) {

		Conta conta = contaMapper.toEntity(contaCreateDTO);

		Conta contaAtualizado = contaService.update(contaId, conta);

		ContaResponseDTO contaResponseDTO = contaMapper.toDTO(contaAtualizado);

		return ResponseEntity.status(HttpStatus.OK).body(contaResponseDTO);

	}

	@GetMapping("/listar")
	public ResponseEntity<List<Conta>> getContas() {
		List<Conta> contas = contaService.getAll();
		return new ResponseEntity<List<Conta>>(contas, HttpStatus.OK);
	}

	@GetMapping("/listar/{contaId}")
	public Optional<Conta> getContaById(@PathVariable("contaId") Long contaId) {

		Optional<Conta> contaById = contaService.getContaById(contaId);

		if (!contaById.isPresent()) {
			throw new ClienteNaoEncontradoException("Cliente não existe no banco.");
		}

		return contaById;
	}
	
	@DeleteMapping("/deletar/{contaId}")
	public ResponseEntity<String> deletar(@PathVariable("contaId") Long contaId) {

		boolean contaDeletada = contaService.deleteConta(contaId);

		if (contaDeletada) {
			return new ResponseEntity<String>("Conta deletada com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	

	@PostMapping("/transferir/{idContaReceber}/ted")
	public ResponseEntity<String> transferirTED(@PathVariable("idContaReceber") Long idContaReceber, @RequestBody ContaCreateTedDTO contaTransCreateDTO) {
		
		boolean tedRealizada = contaService.transferirTED(idContaReceber, contaTransCreateDTO);
		
		if(tedRealizada) {
			return new ResponseEntity<String>("Transferência realizada com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}	
	
	@PostMapping("/transferir/{idContaReceber}/pix")
	public ResponseEntity<String> transferirPIX(@PathVariable("idContaReceber") Long idContaReceber, @RequestBody ContaCreatePixDTO contaPixCreateDTO) {
		
		boolean pixRealizado = contaService.transferirPIX(idContaReceber, contaPixCreateDTO);
		
		if(pixRealizado) {
			return new ResponseEntity<String>("Pix realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do pix são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@PostMapping("/transferir/{idContaReceber}/deposito")
	public ResponseEntity<String> transferirDEPOSITO(@PathVariable("idContaReceber") Long idContaReceber, @RequestBody ContaCreateDepositoDTO contaDepositoCreateDTO) {
		
		boolean depositoRealizado = contaService.transferirDEPOSITO(idContaReceber, contaDepositoCreateDTO);
		
		if(depositoRealizado) {
			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do depósito são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@PostMapping("/transferir/{idContaReceber}/saque")
	public ResponseEntity<String> transferirSAQUE(@PathVariable("idContaReceber") Long idContaReceber, @RequestBody ContaCreateSaqueDTO contaSaqueCreateDTO) {
		
		boolean saqueRealizado = contaService.transferirSAQUE(idContaReceber, contaSaqueCreateDTO);
		
		if(saqueRealizado) {
			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do saque são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@PutMapping("/{idConta}/manutencao")
	public ResponseEntity<String> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta, @RequestBody ContaCorrenteTaxaManutencaoDTO contaCorrenteTaxaCreateDTO) {
		
		boolean manutencaoCCRealizada = contaService.manutencaoTaxaCC(idConta, contaCorrenteTaxaCreateDTO);
		
		if(manutencaoCCRealizada) {
			return new ResponseEntity<String>("Taxas aplicadas com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Taxas inválidas.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	



//	// Transferencia Depositar
//	@PostMapping("/depositar/{idClienteReceber}/{idContaReceber}/deposito")
//	public ResponseEntity<String> transferirDEPOSITAR(@PathVariable("idClienteReceber") Long idClienteReceber,
//			@PathVariable("idContaReceber") Long idContaReceber, @RequestBody Transferencia valorDepositar) {
//
//		boolean depositar = contaService.transferirDEPOSITAR(idClienteReceber, idContaReceber, valorDepositar);
//
//		if (depositar) {
//
//			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);
//
//		} else {
//
//			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//
//	// Transferencia Sacar
//	@PostMapping("/sacar/{idClientePegar}/{idContaPegar}/saque")
//	public ResponseEntity<String> transferirSACAR(@PathVariable("idClientePegar") Long idClientePegar,
//			@PathVariable("idContaPegar") Long idContaPegar, @RequestBody Transferencia valorSacar) {
//
//		boolean sacar = contaService.transferirSACAR(idClientePegar, idContaPegar, valorSacar);
//
//		if (sacar) {
//
//			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);
//
//		} else {
//
//			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}

	// Aplicar Taxa de Manutenção Mensal (Conta Corrente)
//	@PutMapping("/atualizar/{idConta}/manutencao")
//	public ResponseEntity<String> aplicarTaxaManutencaoMensal(@PathVariable("idConta") Long contaIdParaAtualizar, @RequestBody Conta contaDadosInserir) {
//			
//		Conta taxasAplicadas = contaService.aplicarTaxas(contaIdParaAtualizar, contaDadosInserir);
//		
//		if (taxasAplicadas != null) {
//			
//			return new ResponseEntity<String>("A conta " + taxasAplicadas.getNumeroConta() + " foi atualizada com sucesso.", HttpStatus.OK);
//			
//		} else {
//			
//			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
//		}
//
//	}

	// Aplicar Rendimentos (Conta Poupança)

	// Ver saldo
//	@GetMapping("/{clienteId}/saldo")
//	public ResponseEntity<String> exibirSaldo(@PathVariable("clienteId") Long clienteId) {
//
//		float[] saldoAtual = contaService.exibirSaldo(clienteId);
//
//		if (saldoAtual.length >= 0) {
//			return new ResponseEntity<String>("Saldo Total: " + saldoAtual[2] + "\nSaldo Conta Corrente: "
//					+ saldoAtual[0] + "\nSaldo Conta Poupança: " + saldoAtual[1] + ".", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}

}
