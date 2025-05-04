package br.com.marcielli.bancom.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.access.AccessDeniedException accessDeniedException)
			throws IOException, ServletException {
		
		 logger.warn("Acesso negado: {}", accessDeniedException.getMessage());
		
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType("application/json");
		response.getWriter().write("{\"message\": \"Acesso negado para usuário comum\"}");
		
		Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", "Você não tem permissão para acessar este recurso");
        body.put("path", request.getRequestURI());
        
        new ObjectMapper().writeValue(response.getOutputStream(), body);

	}
}
