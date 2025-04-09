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

import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.TaxasManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.service.ContaService;

@RestController
@RequestMapping("/conta")
public class ContaController {

	@Autowired
	private ContaService contaService;

	@PostMapping("/salvar")
	public ResponseEntity<String> adicionarConta(@RequestBody Conta conta) {
		
		Conta contaAdicionada = contaService.save(conta);
		
		if (contaAdicionada != null) {
			
			return new ResponseEntity<String>("A conta " + contaAdicionada.getNumeroConta() + " foi criada com sucesso.\nPix gerado: "+contaAdicionada.getPixAleatorio()+".", HttpStatus.CREATED);
			
		} else {
			
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
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

	@PutMapping("/atualizar/{contaIdParaAtualizar}")
	public ResponseEntity<String> atualizar(@PathVariable("contaIdParaAtualizar") Long contaIdParaAtualizar, @RequestBody Conta contaDadosInserir) {
			
		Conta contaAtualizada = contaService.update(contaIdParaAtualizar, contaDadosInserir);
		
		if (contaAtualizada != null) {
			
			return new ResponseEntity<String>("A conta " + contaAtualizada.getNumeroConta() + " foi atualizada com sucesso.", HttpStatus.OK);
			
		} else {
			
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
		}

	}

	@DeleteMapping("/deletar/{clienteId}/{contaId}")
	public ResponseEntity<String> deletar(@PathVariable("clienteId") Long clienteId, @PathVariable("contaId") Long contaId) {
		
		boolean contaDeletada = contaService.deleteConta(clienteId, contaId);
		
		if (contaDeletada) {
			return new ResponseEntity<String>("Conta deletada com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);			
		}
	}
	

	// Transferencia TED
	@PostMapping("/transferir/{idClienteReceber}/{idContaReceber}")
	public ResponseEntity<String> transferirTED(@PathVariable("idClienteReceber") Long idClienteReceber, @PathVariable("idContaReceber") Long idContaReceber, @RequestBody Transferencia contaEnviar) {
		
		boolean transferencias = contaService.transferirTED(idClienteReceber,idContaReceber, contaEnviar);

		if (transferencias) {

			return new ResponseEntity<String>("Transferência realizada com sucesso.", HttpStatus.CREATED);

		} else {

			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	
	// Transferencia PIX
	@PostMapping("/transferir/{pixAleatorio}/pix")
	public ResponseEntity<String> transferirPIX(@PathVariable("pixAleatorio") String pixAleatorio, @RequestBody Transferencia contaEnviar) {
		
		boolean transferenciaPIX = contaService.transferirPIX(pixAleatorio, contaEnviar);

		if (transferenciaPIX) {
			
			return new ResponseEntity<String>("PIX de "+contaEnviar.getValor()+" realizado com sucesso para a chave "+pixAleatorio+".", HttpStatus.CREATED);

		} else {

			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);

		}
	}
	
	
	// Transferencia Depositar
	@PostMapping("/depositar/{idClienteReceber}/{idContaReceber}/deposito")
	public ResponseEntity<String> transferirDEPOSITAR(@PathVariable("idClienteReceber") Long idClienteReceber, @PathVariable("idContaReceber") Long idContaReceber, @RequestBody Transferencia valorDepositar) {
				
		boolean depositar = contaService.transferirDEPOSITAR(idClienteReceber, idContaReceber, valorDepositar);

		if (depositar) {

			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);

		} else {

			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	// Transferencia Sacar
	@PostMapping("/sacar/{idClientePegar}/{idContaPegar}/saque")
	public ResponseEntity<String> transferirSACAR(@PathVariable("idClientePegar") Long idClientePegar, @PathVariable("idContaPegar") Long idContaPegar, @RequestBody Transferencia valorSacar) {
		
		boolean sacar = contaService.transferirSACAR(idClientePegar, idContaPegar, valorSacar);

		if (sacar) {

			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);

		} else {

			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	
	//Aplicar Taxa de Manutenção Mensal (Conta Corrente)
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
	
	
	//Aplicar Rendimentos (Conta Poupança)
	
	
	
	
		

	// Ver saldo
	@GetMapping("/{clienteId}/saldo")
	public ResponseEntity<String> exibirSaldo(@PathVariable("clienteId") Long clienteId) {
		
		float[] saldoAtual = contaService.exibirSaldo(clienteId);

		if (saldoAtual.length >= 0) {
			return new ResponseEntity<String>("Saldo Total: " + saldoAtual[2]+"\nSaldo Conta Corrente: "+saldoAtual[0]+"\nSaldo Conta Poupança: "+saldoAtual[1]+".", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

}
