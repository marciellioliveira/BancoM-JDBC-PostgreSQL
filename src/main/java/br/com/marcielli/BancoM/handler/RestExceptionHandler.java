package br.com.marcielli.BancoM.handler;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.marcielli.BancoM.exception.CartaoNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteCpfInvalidoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNaoTemSaldoSuficienteException;
import br.com.marcielli.BancoM.exception.ClienteNomeInvalidoException;
import br.com.marcielli.BancoM.exception.ContaExibirSaldoErroException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.ContaNaoFoiPossivelAlterarNumeroException;
import br.com.marcielli.BancoM.exception.ContaNaoRealizouTransferenciaException;
import br.com.marcielli.BancoM.exception.ContaTipoContaNaoExisteException;
import br.com.marcielli.BancoM.exception.ContaTipoNaoPodeSerAlteradaException;
import br.com.marcielli.BancoM.exception.TransferenciaNaoRealizadaException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	//Cliente

	@ExceptionHandler(ClienteNaoEncontradoException.class)
	private ResponseEntity<RestErrorMessage> clientHandler(ClienteNaoEncontradoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respostaTratada);
	}
	
	@ExceptionHandler(ClienteNomeInvalidoException.class)
	private ResponseEntity<RestErrorMessage> clientHandler(ClienteNomeInvalidoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ClienteCpfInvalidoException.class)
	private ResponseEntity<RestErrorMessage> clientHandler(ClienteCpfInvalidoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ClienteNaoTemSaldoSuficienteException.class)
	private ResponseEntity<RestErrorMessage> clientHandler(ClienteNaoTemSaldoSuficienteException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	
	
	
	//Conta
	@ExceptionHandler(ContaTipoContaNaoExisteException.class)
	private ResponseEntity<RestErrorMessage> contaHandler(ContaTipoContaNaoExisteException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ContaNaoEncontradaException.class)
	private ResponseEntity<RestErrorMessage> contaHandler(ContaNaoEncontradaException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ContaNaoRealizouTransferenciaException.class)
	private ResponseEntity<RestErrorMessage> contaHandler(ContaNaoRealizouTransferenciaException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ContaTipoNaoPodeSerAlteradaException.class)
	private ResponseEntity<RestErrorMessage> contaHandler(ContaTipoNaoPodeSerAlteradaException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ContaExibirSaldoErroException.class)
	private ResponseEntity<RestErrorMessage> contaHandler(ContaExibirSaldoErroException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ContaNaoFoiPossivelAlterarNumeroException.class)
	private ResponseEntity<RestErrorMessage> contaHandler(ContaNaoFoiPossivelAlterarNumeroException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	
	
	
	
	//Transferencia
	@ExceptionHandler(TransferenciaNaoRealizadaException.class)
	private ResponseEntity<RestErrorMessage> transferenciaHandler(TransferenciaNaoRealizadaException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	
	
	//Cartão
	@ExceptionHandler(CartaoNaoEncontradoException.class)
	private ResponseEntity<RestErrorMessage> cartaoHandler(CartaoNaoEncontradoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}

	
	
	
}
