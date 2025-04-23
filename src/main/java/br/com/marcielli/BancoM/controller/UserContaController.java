package br.com.marcielli.BancoM.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import br.com.marcielli.BancoM.dto.security.ContaCreateDTO;
import br.com.marcielli.BancoM.dto.security.ContaUpdateDTO;
import br.com.marcielli.BancoM.dto.security.UserContaDepositoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaPixDTO;
import br.com.marcielli.BancoM.dto.security.UserContaRendimentoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaResponseDTO;
import br.com.marcielli.BancoM.dto.security.UserContaSaqueDTO;
import br.com.marcielli.BancoM.dto.security.UserContaTaxaManutencaoDTO;
import br.com.marcielli.BancoM.dto.security.UserContaTedDTO;
import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaCorrente;
import br.com.marcielli.BancoM.entity.ContaPoupanca;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.repository.UserRepository;
import br.com.marcielli.BancoM.service.UserContaService;
import jakarta.transaction.Transactional;

@RestController
public class UserContaController {

	private final UserContaService contaService;
	private final UserRepository userRepository;

	public UserContaController(UserContaService contaService, UserRepository userRepository) {
		this.contaService = contaService;
		this.userRepository = userRepository;
	}

	@PostMapping("/contas")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<?> createConta(@RequestBody ContaCreateDTO dto, JwtAuthenticationToken token) {

		// Pegar o clienteCreateDTO e transformá-lo em uma entidade
		Conta contaAdicionada = contaService.save(dto, token);

		if (contaAdicionada != null) {
			
			UserContaResponseDTO response = new UserContaResponseDTO();
			response.setId(contaAdicionada.getId());
			response.setTipoConta(contaAdicionada.getTipoConta());
			response.setCategoriaConta(contaAdicionada.getCategoriaConta());
			if (contaAdicionada instanceof ContaCorrente contaCorrente) {
				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
			}

			if (contaAdicionada instanceof ContaPoupanca contaPoupanca) {
				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
				response.setTaxaMensal(contaPoupanca.getTaxaMensal());

			}
			response.setSaldoConta(contaAdicionada.getSaldoConta());
			response.setNumeroConta(contaAdicionada.getNumeroConta());
			response.setPixAleatorio(contaAdicionada.getPixAleatorio());
			response.setStatus(contaAdicionada.getStatus());
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
			//return new ResponseEntity<String>("Conta adicionada com sucesso", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<String>("Tente novamente mais tarde.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("/contas")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<User>> listUsers() {
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}

	@GetMapping("/contas/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> getContasById(@PathVariable("id") Long id) {

		Conta conta = contaService.getContasById(id);

		if (conta == null || conta.getCliente() == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}

		boolean isAdmin = conta.getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserContaResponseDTO response = new UserContaResponseDTO();

			response.setId(id);
			response.setTipoConta(conta.getTipoConta());
			response.setCategoriaConta(conta.getCategoriaConta());

			if (conta instanceof ContaCorrente contaCorrente) {
				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
			}

			if (conta instanceof ContaPoupanca contaPoupanca) {
				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
				response.setTaxaMensal(contaPoupanca.getTaxaMensal());

			}
			response.setSaldoConta(conta.getSaldoConta());
			response.setNumeroConta(conta.getNumeroConta());
			response.setPixAleatorio(conta.getPixAleatorio());

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}
	}

	@PutMapping("/contas/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody ContaUpdateDTO dto) {

		Conta conta = contaService.update(id, dto);

		if (conta == null || conta.getCliente().getUser() == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}

		boolean isAdmin = conta.getCliente().getUser().getRoles().stream()
				.anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

		if (!isAdmin) {
			UserContaResponseDTO response = new UserContaResponseDTO();

			if (conta instanceof ContaCorrente contaCorrente) {
				response.setTaxaManutencaoMensal(contaCorrente.getTaxaManutencaoMensal());
			}

			if (conta instanceof ContaPoupanca contaPoupanca) {
				response.setTaxaAcrescRend(contaPoupanca.getTaxaAcrescRend());
				response.setTaxaMensal(contaPoupanca.getTaxaMensal());

			}

			response.setId(id);
			response.setTipoConta(conta.getTipoConta());
			response.setCategoriaConta(conta.getCategoriaConta());
			response.setSaldoConta(conta.getSaldoConta());
			response.setNumeroConta(conta.getNumeroConta());
			response.setPixAleatorio(conta.getPixAleatorio());

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A conta não existe!");
		}
	}

	@DeleteMapping("/contas/{id}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	@Transactional
	public ResponseEntity<?> deletar(@PathVariable("id") Long id) {

		boolean conta = contaService.delete(id);

		if (conta) {
			return ResponseEntity.status(HttpStatus.OK).body("Conta deletado com sucesso!");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro. Tente novamente mais tarde.");
		}
	}

	// Transferências
	@PostMapping("/contas/{idContaReceber}/transferencia")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> transferirTED(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaTedDTO dto) {

		boolean tedRealizada = contaService.transferirTED(idContaReceber, dto);

		if (tedRealizada) {
			return new ResponseEntity<String>("Transferência realizada com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados da transferência são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping("/contas/{contaId}/saldo")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> exibirSaldo(@PathVariable("contaId") Long contaId) {

		BigDecimal saldoAtual = contaService.exibirSaldo(contaId);

		if (saldoAtual.compareTo(BigDecimal.ZERO) >= 0) {
			return ResponseEntity.ok("Saldo Total: " + saldoAtual);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Dados da conta são inválidos.");
		}
	}

	@PostMapping("/contas/{idContaReceber}/pix")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> transferirPIX(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaPixDTO dto) {

		boolean pixRealizado = contaService.transferirPIX(idContaReceber, dto);

		if (pixRealizado) {
			return new ResponseEntity<String>("Pix realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do pix são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@PostMapping("/contas/{idContaReceber}/deposito")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> transferirDEPOSITO(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaDepositoDTO dto) {

		boolean depositoRealizado = contaService.transferirDEPOSITO(idContaReceber, dto);

		if (depositoRealizado) {
			return new ResponseEntity<String>("Depósito realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do depósito são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@PostMapping("/contas/{idContaReceber}/saque")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> transferirSAQUE(@PathVariable("idContaReceber") Long idContaReceber,
			@RequestBody UserContaSaqueDTO dto) {

		boolean saqueRealizado = contaService.transferirSAQUE(idContaReceber, dto);

		if (saqueRealizado) {
			return new ResponseEntity<String>("Saque realizado com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Dados do saque são inválidos.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@PutMapping("/contas/{idConta}/manutencao")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> manutencaoTaxaContaCorrente(@PathVariable("idConta") Long idConta,
			@RequestBody UserContaTaxaManutencaoDTO dto) {

		Conta manutencaoCCRealizada = contaService.manutencaoTaxaCC(idConta, dto);

		if (manutencaoCCRealizada != null) {
			return new ResponseEntity<String>("Taxas aplicadas com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Taxas inválidas.", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@PutMapping("/contas/{idConta}/rendimentos")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_BASIC')")
	public ResponseEntity<String> rendimentoTaxaContaPoupanca(@PathVariable("idConta") Long idConta,
			@RequestBody UserContaRendimentoDTO dto) {

		Conta manutencaoCPRealizada = contaService.rendimentoTaxaCP(idConta, dto);

		if (manutencaoCPRealizada != null) {
			return new ResponseEntity<String>("Taxas aplicadas com sucesso.", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Taxas inválidas.", HttpStatus.NOT_ACCEPTABLE);
		}

	}

}
