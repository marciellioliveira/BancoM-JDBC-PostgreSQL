package br.com.marcielli.bancom.service;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.marcielli.bancom.entity.User;

@Service
public class UserSecurityService {	

	@Autowired
    private UserClienteService userClienteService; // Serviço que pode obter o usuário pelo ID

	// Verifica se o usuário autenticado tem o mesmo ID do usuário que está tentando realizar a ação
    public boolean checkUserId(Authentication authentication, Long userId) {
        String username = authentication.name(); // Nome do usuário autenticado
        User authenticatedUser = userClienteService.findByUsername(username);  // Obtém o usuário pelo nome de usuário

        // Verifica se o usuário autenticado existe e se o ID dele corresponde ao userId passado
        return authenticatedUser != null && authenticatedUser.getId().equals(userId);
    }

}
