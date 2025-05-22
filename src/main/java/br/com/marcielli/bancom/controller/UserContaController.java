package br.com.marcielli.bancom.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

	// ADMIN pode criar conta pra ele e pra todos
	// BASIC só pode criar conta pra ele mesmo
	@PostMapping("/contas")
	public ResponseEntity<String> createConta(@RequestBody ContaCreateDTO dto, Authentication authentication) {
		
		
		Conta contaAdicionada = contaService.save(dto, authentication);

		if (contaAdicionada != null) {
			return new ResponseEntity<>(
					"Conta adicionada com sucesso. Número da conta: " + contaAdicionada.getNumeroConta(),
					HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	// ADMIN pode ver todas as contas
	// BASIC pode ver apenas a conta com o id dele
	@GetMapping("/contas")
	public ResponseEntity<List<Conta>> listContas(Authentication authentication) {
		var contas = contaService.getContas(authentication);
		return ResponseEntity.status(HttpStatus.OK).body(contas);
	}

	// ADMIN pode ver todas as contas por id, dele e de qualquer usuario
	// BASIC só pode ver a conta dele
	@GetMapping("/contas/{id}")
	public ResponseEntity<?> getContaById(@PathVariable("id") Long id, Authentication authentication) {
		Conta conta = contaService.getContasById(id, authentication);
		return ResponseEntity.ok(conta);
	}

	// ADMIN pode deletar a conta de todos, menos a dele
	// BASIC só pode deletar a própria conta por id
	@DeleteMapping("/contas/{id}")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id, @RequestBody ContaUpdateDTO dto,
			Authentication authentication) {
		boolean conta = contaService.delete(id, dto, authentication);

		if (conta) {
			return ResponseEntity.status(HttpStatus.OK).body("Conta desativada com sucesso!");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro. Tente novamente mais tarde.");
		}
	}

	// ADMIN pode atualizar a conta dele e de todos os outros
	// BASIC só pode atualizar a conta dele próprio por id
	@PutMapping("/contas/{id}")
	public ResponseEntity<Conta> atualizar(@PathVariable("id") Long id, @RequestBody ContaUpdateDTO dto,
			Authentication authentication) {
		Conta conta = contaService.update(id, dto, authentication);
		return ResponseEntity.ok(conta); // Retorna a entidade pura
	}

	// Ativar conta
	@PutMapping("/contas/{id}/ativar")
	public ResponseEntity<String> ativarConta(@PathVariable("id") Long id, Authentication authentication) {
		boolean contaAtivada = contaService.ativarConta(id, authentication);
		if (contaAtivada) {
			return ResponseEntity.status(HttpStatus.OK).body("Conta ativada com sucesso!");
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conta não encontrada!");
	}

	// Transferências

	// ADMIN pode fazer transferencia da conta dele para outras, e de outros para
	// outros mas não pode fazer de outros para ele mesmo quando tiver logado
	// BASIC só pode transferir da propria conta.
	@PostMapping("/contas/{idContaReceber}/transferencia")
	public ResponseEntity<String> transferirTED(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaTedDTO dto, Authentication authentication) {

		boolean tedRealizada = contaService.transferirTED(idContaReceber, dto, authentication);
		return tedRealizada ? ResponseEntity.ok("Transferência realizada com sucesso.")
				: ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Dados da transferência são inválidos.");

	}

	// ADMIN pode ver saldo de todos
	// BASIC só pode ver saldo dele mesmo.
	@GetMapping("/contas/{contaId}/saldo")
	public Map<String, BigDecimal> exibirSaldoConvertido(@PathVariable("contaId") Long contaId,
			Authentication authentication) {
		return contaService.exibirSaldoConvertido(contaId, authentication);
	}

	// ADMIN pode fazer pix da conta dele para outras, e de outros para outros mas
	// não pode fazer de outros para ele mesmo quando tiver logado
	// BASIC só pode fazer pix da propria conta.
	@PostMapping("/contas/{chaveOuIdDestino}/pix")
	public ResponseEntity<String> transferirPIX(@PathVariable("chaveOuIdDestino") String chaveOuIdDestino,
			@RequestBody UserContaPixDTO dto, Authentication authentication) {
		boolean pixRealizado = contaService.transferirPIX(chaveOuIdDestino, dto, authentication);

		if (pixRealizado) {
			return new ResponseEntity<String>("Pix realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do pix são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	// ADMIN pode fazer deposito na propria conta e de outras pessoas
	// BASIC só pode fazer deposito na propria conta.
	@PostMapping("/contas/{idContaReceber}/deposito")
	public ResponseEntity<String> transferirDEPOSITO(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaDepositoDTO dto, Authentication authentication) {

		boolean depositoRealizado = contaService.transferirDEPOSITO(idContaReceber, dto, authentication);

		if (depositoRealizado) {
			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do depósito são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	// ADMIN pode fazer deposito na propria conta e de outras pessoas
	// BASIC sacar só da propria conta
	@PostMapping("/contas/{idContaReceber}/saque")
	public ResponseEntity<String> transferirSAQUE(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaSaqueDTO dto, Authentication authentication) {
		boolean saqueRealizado = contaService.transferirSAQUE(idContaReceber, dto, authentication);

		if (saqueRealizado) {
			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do saque são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	// ROTAS MANUAIS
	@PutMapping("/contas/{idConta}/manutencao") // SOMENTE ADMIN/BANCO
	public ResponseEntity<String> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta,
			Authentication authentication) {

		boolean sucesso = contaService.manutencaoTaxaCC(idConta, authentication);

		if (sucesso) {
			return ResponseEntity.ok("Taxa de manutenção aplicada com sucesso");
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao aplicar a taxa de manutenção");
		}
	}

	// ROTAS MANUAIS
	@PutMapping("/contas/{idConta}/rendimentos") // SOMENTE ADMIN/BANCO
	public ResponseEntity<String> rendimentoTaxaContaPoupanca(@PathVariable("idConta") Long idConta,
			Authentication authentication) {

		boolean sucesso = contaService.rendimentoTaxaCP(idConta, authentication);
		if (sucesso) {

			return ResponseEntity.ok("Taxa de rendimento aplicada com sucesso");
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao aplicar a taxa de rendimento");
		}
	}
}
