package br.com.marcielli.bancom.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.com.marcielli.bancom.dto.security.ContaCreateDTO;
import br.com.marcielli.bancom.dto.security.ContaUpdateDTO;
import br.com.marcielli.bancom.dto.security.UserContaDepositoDTO;
import br.com.marcielli.bancom.dto.security.UserContaPixDTO;
import br.com.marcielli.bancom.dto.security.UserContaSaqueDTO;
import br.com.marcielli.bancom.dto.security.UserContaTedDTO;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.service.UserContaService;


@RestController
public class UserContaController {

	private final UserContaService contaService;

	public UserContaController(UserContaService contaService) {

		this.contaService = contaService;
	}

	//ADMIN pode criar conta pra ele e pra todos
	//BASIC só pode criar conta pra ele mesmo
	@PostMapping("/contas")
	public ResponseEntity<String> createConta(@RequestBody ContaCreateDTO dto) {
		Conta contaAdicionada = contaService.save(dto);

		if (contaAdicionada != null) {
			return new ResponseEntity<>("Conta adicionada com sucesso. Número da conta: "+ contaAdicionada.getNumeroConta(), HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}
		

	//ADMIN pode ver todas as contas
	//BASIC pode ver apenas a conta com o id dele
	@GetMapping("/contas")
	public ResponseEntity<List<Conta>> listContas() {
		return null;
//		var contas = contaService.getContas();
//		return ResponseEntity.status(HttpStatus.OK).body(contas);
	}

	//ADMIN pode ver todas as contas por id, dele e de qualquer usuario
	//BASIC só pode ver a conta dele
	@GetMapping("/contas/{id}")
	public ResponseEntity<?> getContaById(@PathVariable("id") Long id) {
		return null; 

//		Conta conta = contaService.getContasById(id);
//
//		UserContaResponseDTO response = new UserContaResponseDTO();
//		response.setId(conta.getId());
//		response.setTipoConta(conta.getTipoConta());
//		response.setCategoriaConta(conta.getCategoriaConta());
//		response.setSaldoConta(conta.getSaldoConta());
//		response.setNumeroConta(conta.getNumeroConta());
//		response.setPixAleatorio(conta.getPixAleatorio());
//		response.setStatus(conta.getStatus());
//
//		if (conta instanceof ContaCorrente) {
//			response.setTaxaManutencaoMensal(((ContaCorrente) conta).getTaxaManutencaoMensal());
//		} else if (conta instanceof ContaPoupanca) {
//			response.setTaxaAcrescRend(((ContaPoupanca) conta).getTaxaAcrescRend());
//			response.setTaxaMensal(((ContaPoupanca) conta).getTaxaMensal());
//		}
//
//		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	//ADMIN pode atualizar a conta dele e de todos os outros
	//BASIC só pode atualizar a conta dele próprio por id
	@PutMapping("/contas/{id}")
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody ContaUpdateDTO dto) {
		return null;

//		Conta conta = contaService.update(id, dto);
//
//		UserContaResponseDTO response = new UserContaResponseDTO();
//
//		if (conta instanceof ContaCorrente contaCorrente) {
//			response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
//		}
//
//		if (conta instanceof ContaPoupanca contaPoupanca) {
//			response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
//			response.setTaxaMensal(contaPoupanca.getTaxaMensal());
//		}
//
//		response.setId(id);
//		response.setTipoConta(conta.getTipoConta());
//		response.setCategoriaConta(conta.getCategoriaConta());
//		response.setSaldoConta(conta.getSaldoConta());
//		response.setNumeroConta(conta.getNumeroConta());
//		response.setPixAleatorio(conta.getPixAleatorio());
//
//		return ResponseEntity.ok(response);
	}

	//ADMIN pode deltar a conta de todos, menos a dele
	//BASIC só pode deletar a própria conta por id
	@DeleteMapping("/contas/{id}")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, @RequestBody ContaUpdateDTO dto) {
		return null;

//		boolean conta = contaService.delete(id, dto);
//
//		if (conta) {
//			return ResponseEntity.status(HttpStatus.OK).body("Conta deletado com sucesso!");
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro. Tente novamente mais tarde.");
//		}
	}

	
	
	// Transferências
	
	
	//ADMIN pode fazer transferencia da conta dele para outras, e de outros para outros mas não pode fazer de outros para ele mesmo quando tiver logado
	//BASIC só pode transferir da propria conta.
	@PostMapping("/contas/{idContaReceber}/transferencia")
	public ResponseEntity<String> transferirTED(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaTedDTO dto) {
				return null;

//		boolean tedRealizada = contaService.transferirTED(idContaReceber, dto);
//		return tedRealizada ? ResponseEntity.ok("Transferência realizada com sucesso.")
//				: ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Dados da transferência são inválidos.");
	}

	//ADMIN pode ver saldo de todos
	//BASIC só pode ver saldo dele mesmo.
	@GetMapping("/contas/{contaId}/saldo")
	public Map<String, BigDecimal> exibirSaldoConvertido(@PathVariable("contaId") Long contaId) {
		return null;
//		return contaService.exibirSaldoConvertido(contaId);
	}

	//ADMIN pode fazer pix da conta dele para outras, e de outros para outros mas não pode fazer de outros para ele mesmo quando tiver logado
	//BASIC só pode fazer pix da propria conta.
	@PostMapping("/contas/{idContaReceber}/pix")
	public ResponseEntity<String> transferirPIX(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaPixDTO dto) {
				return null;

//		boolean pixRealizado = contaService.transferirPIX(idContaReceber, dto);
//
//		if (pixRealizado) {
//			return new ResponseEntity<String>("Pix realizado com sucesso.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<String>("Dados do pix são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
	}

	//ADMIN pode fazer deposito apenas na propria conta
	//BASIC só pode fazer deposito na propria conta.
	@PostMapping("/contas/{idContaReceber}/deposito")
	public ResponseEntity<String> transferirDEPOSITO(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaDepositoDTO dto) {
				return null;

//		boolean depositoRealizado = contaService.transferirDEPOSITO(idContaReceber, dto);
//
//		if (depositoRealizado) {
//			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<String>("Dados do depósito são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
	}

	//ADMIN sacar só da propria conta
	//BASIC sacar só da propria conta
	@PostMapping("/contas/{idContaReceber}/saque")
	public ResponseEntity<String> transferirSAQUE(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaSaqueDTO dto) {
				return null;

//		boolean saqueRealizado = contaService.transferirSAQUE(idContaReceber, dto);
//
//		if (saqueRealizado) {
//			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<String>("Dados do saque são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
	}

	// ROTAS MANUAIS - FUNCIONAM estou programando para elas serem cobradas
	// automaticamente com o cron do spring	
	@PutMapping("/contas/{idConta}/manutencao") //SOMENTE ADMIN/BANCO
	public ResponseEntity<?> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta) {
		return null;

//		Conta contaAtualizada = contaService.manutencaoTaxaCC(idConta);
//
//		if (contaAtualizada != null) {
//
//			UserContaResponseDTO response = new UserContaResponseDTO();
//			response.setId(contaAtualizada.getId());
//			response.setTipoConta(contaAtualizada.getTipoConta());
//			response.setCategoriaConta(contaAtualizada.getCategoriaConta());
//			if (contaAtualizada instanceof ContaCorrente contaCorrente) {
//				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
//			}
//
//			if (contaAtualizada instanceof ContaPoupanca contaPoupanca) {
//				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
//				response.setTaxaMensal(contaPoupanca.getTaxaMensal());
//			}
//
//			response.setSaldoConta(contaAtualizada.getSaldoConta());
//			response.setNumeroConta(contaAtualizada.getNumeroConta());
//			response.setPixAleatorio(contaAtualizada.getPixAleatorio());
//			response.setStatus(contaAtualizada.getStatus());
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
//		}
	}

	@PutMapping("/contas/{idConta}/rendimentos") //SOMENTE ADMIN/BANCO
	public ResponseEntity<?> rendimentoTaxaContaPoupanca(@PathVariable("idConta") Long idConta) {
		return null;
//		Conta contaAtualizada = contaService.rendimentoTaxaCP(idConta);
//		if (contaAtualizada != null) {
//
//			UserContaResponseDTO response = new UserContaResponseDTO();
//			response.setId(contaAtualizada.getId());
//			response.setTipoConta(contaAtualizada.getTipoConta());
//			response.setCategoriaConta(contaAtualizada.getCategoriaConta());
//			if (contaAtualizada instanceof ContaCorrente contaCorrente) {
//				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
//			}
//
//			if (contaAtualizada instanceof ContaPoupanca contaPoupanca) {
//				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
//				response.setTaxaMensal(contaPoupanca.getTaxaMensal());
//			}
//
//			response.setSaldoConta(contaAtualizada.getSaldoConta());
//			response.setNumeroConta(contaAtualizada.getNumeroConta());
//			response.setPixAleatorio(contaAtualizada.getPixAleatorio());
//			response.setStatus(contaAtualizada.getStatus());
//
//			return ResponseEntity.status(HttpStatus.OK).body(response);
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
//		}
	}

//	@PutMapping("/contas/{idConta}/manutencao")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//	public ResponseEntity<?> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta) {
//		return processarOperacaoConta(idConta, contaService::manutencaoTaxaCC);
//	}
//
//	@PutMapping("/contas/{idConta}/rendimentos")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//	public ResponseEntity<?> rendimentoTaxaContaPoupanca(@PathVariable("idConta") Long idConta) {
//		return processarOperacaoConta(idConta, contaService::rendimentoTaxaCP);
//	}
//

}
