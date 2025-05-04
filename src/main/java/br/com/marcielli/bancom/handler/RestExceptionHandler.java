package br.com.marcielli.bancom.handler;


import java.nio.file.AccessDeniedException;

import br.com.marcielli.bancom.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	 // Acesso negado (Ainda não funcionou)
//    @ExceptionHandler(AcessoNegadoException.class)
//    public ResponseEntity<RestErrorMessage> handleAccessDeniedException(AcessoNegadoException exception) {
//        RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.");
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respostaTratada);
//    }
//    
//    @ExceptionHandler(AutenticacaoEntryPointException.class)
//    public ResponseEntity<RestErrorMessage> handleAccessDeniedException(AutenticacaoEntryPointException exception) {
//        RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.");
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respostaTratada);
//    }
	
	

	//Cliente

	@ExceptionHandler(ClienteNaoEncontradoException.class)
	private ResponseEntity<RestErrorMessage> clientHandler(ClienteNaoEncontradoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respostaTratada);
	}
	
	@ExceptionHandler(ClienteEncontradoException.class)
	private ResponseEntity<RestErrorMessage> clientHandler(ClienteEncontradoException exception) {		
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


	@ExceptionHandler(RolePersistenceException.class)
	private ResponseEntity<RestErrorMessage> roleHandler(RolePersistenceException exception) {
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	
	
	
	//Conta
	@ExceptionHandler(ContaTipoContaNaoExisteException.class)
	private ResponseEntity<RestErrorMessage> contaHandler(ContaTipoContaNaoExisteException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	@ExceptionHandler(ContaExisteNoBancoException.class)
	private ResponseEntity<RestErrorMessage> contaHandler(ContaExisteNoBancoException exception) {		
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
	
	@ExceptionHandler(PermissaoNegadaException.class)
	private ResponseEntity<RestErrorMessage> cartaoHandler(PermissaoNegadaException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	//Seguro
	@ExceptionHandler(SeguroNaoEncontradoException.class)
	private ResponseEntity<RestErrorMessage> seguroHandler(SeguroNaoEncontradoException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	
	
	//API Cambio
	@ExceptionHandler(TaxaDeCambioException.class)
	private ResponseEntity<RestErrorMessage> apiTaxaCambioHandler(TaxaDeCambioException exception) {		
		RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());		
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(respostaTratada);
	}
	
	

	
	
	
}
