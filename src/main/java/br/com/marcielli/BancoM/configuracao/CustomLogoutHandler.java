package br.com.marcielli.BancoM.configuracao;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import br.com.marcielli.BancoM.entity.Token;
import br.com.marcielli.BancoM.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class CustomLogoutHandler implements LogoutHandler {

	private final TokenRepository tokenRepository;
	
	 // Definindo o logger corretamente
    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutHandler.class);


	public CustomLogoutHandler(TokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}

		String token = authHeader.substring(7);
		Token storedToken = tokenRepository.findByAccessToken(token).orElse(null);

		if (storedToken != null) {
			storedToken.setLoggedOut(true);
			tokenRepository.save(storedToken);
			logger.info("Token marcado como logout: " + storedToken.getAccessToken());
		}

	}

}
