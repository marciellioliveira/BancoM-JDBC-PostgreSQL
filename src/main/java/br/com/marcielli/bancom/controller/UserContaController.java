//package br.com.marcielli.bancom.controller;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import br.com.marcielli.bancom.dto.security.ContaCreateDTO;
//import br.com.marcielli.bancom.dto.security.ContaUpdateDTO;
//import br.com.marcielli.bancom.dto.security.UserContaDepositoDTO;
//import br.com.marcielli.bancom.dto.security.UserContaPixDTO;
//import br.com.marcielli.bancom.dto.security.UserContaResponseDTO;
//import br.com.marcielli.bancom.dto.security.UserContaSaqueDTO;
//import br.com.marcielli.bancom.dto.security.UserContaTedDTO;
//import br.com.marcielli.bancom.entity.Conta;
//import br.com.marcielli.bancom.entity.ContaCorrente;
//import br.com.marcielli.bancom.entity.ContaPoupanca;
//import br.com.marcielli.bancom.service.UserContaService;
//
//@RestController
//public class UserContaController {
//
//	private final UserContaService contaService;
//
//	public UserContaController(UserContaService contaService) {
//		this.contaService = contaService;
//	}
//
//	@PostMapping("/contas")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	public ResponseEntity<?> createConta(@RequestBody ContaCreateDTO dto) { // , JwtAuthenticationToken token
//
//		Conta contaAdicionada = contaService.save(dto);
//
//		if (contaAdicionada != null) {
//
//			UserContaResponseDTO response = new UserContaResponseDTO();
//			response.setId(contaAdicionada.getId());
//			response.setTipoConta(contaAdicionada.getTipoConta());
//			response.setCategoriaConta(contaAdicionada.getCategoriaConta());
//			if (contaAdicionada instanceof ContaCorrente contaCorrente) {
//				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
//			}
//
//			if (contaAdicionada instanceof ContaPoupanca contaPoupanca) {
//				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
//				response.setTaxaMensal(contaPoupanca.getTaxaMensal());
//
//			}
//			response.setSaldoConta(contaAdicionada.getSaldoConta());
//			response.setNumeroConta(contaAdicionada.getNumeroConta());
//			response.setPixAleatorio(contaAdicionada.getPixAleatorio());
//			response.setStatus(contaAdicionada.getStatus());
//			return ResponseEntity.status(HttpStatus.CREATED).body(response);
//		} else {
//			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//
//	@GetMapping("/contas")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//	public ResponseEntity<List<Conta>> listContas() {
//		var contas = contaService.getContas();
//		return ResponseEntity.status(HttpStatus.OK).body(contas);
//	}
//
//	@GetMapping("/contas/{id}")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	public ResponseEntity<?> getContaById(@PathVariable("id") Long id) { // , JwtAuthenticationToken token
//
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
//	}
//
//	@PutMapping("/contas/{id}")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody ContaUpdateDTO dto) {
//
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
//	}
//
//	@DeleteMapping("/contas/{id}")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//	public ResponseEntity<?> deletar(@PathVariable("id") Long id, @RequestBody ContaUpdateDTO dto) {
//
//		boolean conta = contaService.delete(id, dto);
//
//		if (conta) {
//			return ResponseEntity.status(HttpStatus.OK).body("Conta deletado com sucesso!");
//		} else {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro. Tente novamente mais tarde.");
//		}
//	}
//
//	// Transferências
//	@PostMapping("/contas/{idContaReceber}/transferencia")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	public ResponseEntity<String> transferirTED(@PathVariable("idContaReceber") Long idContaReceber,
//			@RequestBody UserContaTedDTO dto) {
//
//		boolean tedRealizada = contaService.transferirTED(idContaReceber, dto);
//		return tedRealizada ? ResponseEntity.ok("Transferência realizada com sucesso.")
//				: ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Dados da transferência são inválidos.");
//	}
//
//	@GetMapping("/contas/{contaId}/saldo")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	public Map<String, BigDecimal> exibirSaldoConvertido(@PathVariable("contaId") Long contaId) {
//		return contaService.exibirSaldoConvertido(contaId);
//	}
//
//	@PostMapping("/contas/{idContaReceber}/pix")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	public ResponseEntity<String> transferirPIX(@PathVariable("idContaReceber") Long idContaReceber,
//			@RequestBody UserContaPixDTO dto) {
//
//		boolean pixRealizado = contaService.transferirPIX(idContaReceber, dto);
//
//		if (pixRealizado) {
//			return new ResponseEntity<String>("Pix realizado com sucesso.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<String>("Dados do pix são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	@PostMapping("/contas/{idContaReceber}/deposito")
//	public ResponseEntity<String> transferirDEPOSITO(@PathVariable("idContaReceber") Long idContaReceber,
//			@RequestBody UserContaDepositoDTO dto) {
//
//		boolean depositoRealizado = contaService.transferirDEPOSITO(idContaReceber, dto);
//
//		if (depositoRealizado) {
//			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<String>("Dados do depósito são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//
//	@PostMapping("/contas/{idContaReceber}/saque")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
//	public ResponseEntity<String> transferirSAQUE(@PathVariable("idContaReceber") Long idContaReceber,
//			@RequestBody UserContaSaqueDTO dto) {
//
//		boolean saqueRealizado = contaService.transferirSAQUE(idContaReceber, dto);
//
//		if (saqueRealizado) {
//			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);
//		} else {
//			return new ResponseEntity<String>("Dados do saque são inválidos.", HttpStatus.NOT_ACCEPTABLE);
//		}
//	}
//
//	// ROTAS MANUAIS - FUNCIONAM estou programando para elas serem cobradas
//	// automaticamente com o cron do spring
//	@PutMapping("/contas/{idConta}/manutencao")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//	public ResponseEntity<?> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta) {
//
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
//	}
//
//	@PutMapping("/contas/{idConta}/rendimentos")
//	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
//	public ResponseEntity<?> rendimentoTaxaContaPoupanca(@PathVariable("idConta") Long idConta) {
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
//	}
//
////	@PutMapping("/contas/{idConta}/manutencao")
////	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
////	public ResponseEntity<?> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta) {
////		return processarOperacaoConta(idConta, contaService::manutencaoTaxaCC);
////	}
////
////	@PutMapping("/contas/{idConta}/rendimentos")
////	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
////	public ResponseEntity<?> rendimentoTaxaContaPoupanca(@PathVariable("idConta") Long idConta) {
////		return processarOperacaoConta(idConta, contaService::rendimentoTaxaCP);
////	}
////
//
//}
