package br.com.marcielli.BancoM.handler;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.marcielli.BancoM.exception.ClienteCpfInvalidoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNomeInvalidoException;
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.ContaTipoContaNaoExisteException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	//Cliente

	@ExceptionHandler(ClienteNaoEncontradoException.class)
	private ResponseEntity<RestErrorMessage> clientNotCreatedHandler(ClienteNaoEncontradoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respostaTratada);
	}
	
	@ExceptionHandler(ClienteNomeInvalidoException.class)
	private ResponseEntity<RestErrorMessage> clientNotCreatedHandler(ClienteNomeInvalidoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ClienteCpfInvalidoException.class)
	private ResponseEntity<RestErrorMessage> clientNotCreatedHandler(ClienteCpfInvalidoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	
	//Conta
	@ExceptionHandler(ContaTipoContaNaoExisteException.class)
	private ResponseEntity<RestErrorMessage> clientNotCreatedHandler(ContaTipoContaNaoExisteException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ContaNaoEncontradaException.class)
	private ResponseEntity<RestErrorMessage> clientNotCreatedHandler(ContaNaoEncontradaException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	
	
	
}
