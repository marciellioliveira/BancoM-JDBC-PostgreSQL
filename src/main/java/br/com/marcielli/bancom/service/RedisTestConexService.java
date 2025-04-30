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

    @PostConstruct //Executa antes de uma aplicação do Spring ser parada
    public void testRedisConnection() {
        redisTemplate.opsForValue().set("redsKey", "Conectado!");
        String value = redisTemplate.opsForValue().get("redsKey");
        System.out.println("Conexão Redis: " + value);
    }

}
