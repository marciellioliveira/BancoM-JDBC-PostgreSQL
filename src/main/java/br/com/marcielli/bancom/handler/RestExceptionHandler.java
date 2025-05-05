package br.com.marcielli.bancom.handler;


import br.com.marcielli.bancom.exception.*;
import br.com.marcielli.bancom.service.UserClienteService;

import java.nio.file.AccessDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(UserClienteService.class);	
	
	//Global
	@ExceptionHandler(AcessoNegadoException.class)
    private ResponseEntity<RestErrorMessage> acessoNegadoHandler(AcessoNegadoException exception) {
        RestErrorMessage respostaTratada = new RestErrorMessage(HttpStatus.FORBIDDEN, exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respostaTratada);
    }
	
	@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
		logger.error("Erro ocorrido: {}", ex.getMessage());
        return new ResponseEntity<>("Erro: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
	
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
    
    

	
	
	//Cliente
    // Cliente não encontrado - 404 
    @ExceptionHandler(ClienteNaoEncontradoException.class)
    private ResponseEntity<RestErrorMessage> clientHandler(ClienteNaoEncontradoException exception) {        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RestErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    // Cliente já existe - 409
    @ExceptionHandler(ClienteEncontradoException.class)
    private ResponseEntity<RestErrorMessage> clientHandler(ClienteEncontradoException exception) {        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new RestErrorMessage(HttpStatus.CONFLICT, exception.getMessage()));
    }

    // Dados inválidos - 422
    @ExceptionHandler({ClienteNomeInvalidoException.class, ClienteCpfInvalidoException.class})
    private ResponseEntity<RestErrorMessage> handleClienteDadosInvalidos(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new RestErrorMessage(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage()));
    }

    // Saldo insuficiente 
    @ExceptionHandler(ClienteNaoTemSaldoSuficienteException.class)
    private ResponseEntity<RestErrorMessage> clientHandler(ClienteNaoTemSaldoSuficienteException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RestErrorMessage(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }
	
	
	
	
	//Conta
    // Conta não encontrada - 404
    @ExceptionHandler(ContaNaoEncontradaException.class)
    private ResponseEntity<RestErrorMessage> contaHandler(ContaNaoEncontradaException exception) {        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RestErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    // Tipo de conta inválido - 422
    @ExceptionHandler(ContaTipoContaNaoExisteException.class)
    private ResponseEntity<RestErrorMessage> contaHandler(ContaTipoContaNaoExisteException exception) {        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new RestErrorMessage(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage()));
    }

    // Conta já existe - 409
    @ExceptionHandler(ContaExisteNoBancoException.class)
    private ResponseEntity<RestErrorMessage> contaHandler(ContaExisteNoBancoException exception) {        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new RestErrorMessage(HttpStatus.CONFLICT, exception.getMessage()));
    }

    // Erros de operação - 400
    @ExceptionHandler({
        ContaNaoRealizouTransferenciaException.class,
        ContaTipoNaoPodeSerAlteradaException.class,
        ContaExibirSaldoErroException.class,
        ContaNaoFoiPossivelAlterarNumeroException.class
    })
    private ResponseEntity<RestErrorMessage> handleContaOperacaoInvalida(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RestErrorMessage(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }
	
	
	
	
	
    // Transferência - 400
    @ExceptionHandler(TransferenciaNaoRealizadaException.class)
    private ResponseEntity<RestErrorMessage> transferenciaHandler(TransferenciaNaoRealizadaException exception) {        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RestErrorMessage(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }
    
    
    
    //PIX
    @ExceptionHandler(ChavePixNaoEncontradaException.class)
    private ResponseEntity<RestErrorMessage> transferenciaHandler(ChavePixNaoEncontradaException exception) {        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RestErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage()));
    }
	
	
	
	//Cartão
    // Cartão não encontrado - 404
    @ExceptionHandler(CartaoNaoEncontradoException.class)
    private ResponseEntity<RestErrorMessage> cartaoHandler(CartaoNaoEncontradoException exception) {        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RestErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    // Permissão negada - 403
    @ExceptionHandler(PermissaoNegadaException.class)
    private ResponseEntity<RestErrorMessage> cartaoHandler(PermissaoNegadaException exception) {        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new RestErrorMessage(HttpStatus.FORBIDDEN, exception.getMessage()));
    }
    
    
	
	//Seguro
    @ExceptionHandler(SeguroNaoEncontradoException.class)
    private ResponseEntity<RestErrorMessage> seguroHandler(SeguroNaoEncontradoException exception) {        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RestErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage()));
    }
	
	
	
	//API Cambio
    @ExceptionHandler(TaxaDeCambioException.class)
    private ResponseEntity<RestErrorMessage> apiTaxaCambioHandler(TaxaDeCambioException exception) {        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new RestErrorMessage(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage()));
    }

    // Role Persistence - 500 
    @ExceptionHandler(RolePersistenceException.class)
    private ResponseEntity<RestErrorMessage> roleHandler(RolePersistenceException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()));
    }

	
	
	
}
