package br.com.marcielli.bancom.filter;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.marcielli.bancom.service.RedisTokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtTokenFilter extends OncePerRequestFilter {


    private final RedisTokenBlacklistService tokenBlacklistService;

    public JwtTokenFilter(RedisTokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null && tokenBlacklistService.isTokenInvalid(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalidado");
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}