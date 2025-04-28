package br.com.marcielli.bancom.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final String jwtSecret;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate, 
                                    @Value("${jwt.private.key}") String jwtSecret) {
        this.redisTemplate = redisTemplate;
        this.jwtSecret = jwtSecret;
    }

    public void invalidateToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(
            token, 
            "invalid", 
            ttl, 
            TimeUnit.MILLISECONDS
        );
    }

    public boolean isTokenInvalid(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}