package br.com.marcielli.bancom.service;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestConexService {
    private final StringRedisTemplate redisTemplate;

    public RedisTestConexService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void testRedisConnection() {
        try {
            String pingResponse = redisTemplate.getConnectionFactory().getConnection().ping();
            System.out.println("Redis conectado com sucesso: " + pingResponse);
        } catch (Exception e) {
            System.err.println("AVISO: Redis não está disponível. Aplicação continuará em modo sem Redis.");
            // Você pode adicionar uma flag para operar em modo fallback
        }
    }
}