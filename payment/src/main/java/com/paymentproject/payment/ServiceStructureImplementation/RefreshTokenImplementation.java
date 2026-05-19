package com.paymentproject.payment.ServiceStructureImplementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import com.paymentproject.payment.ServiceStructure.RefreshTokenStructure;

@Service
public class RefreshTokenImplementation implements RefreshTokenStructure {

     @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    public final long refreshTokenValidity = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds

    @Override
    public String createRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(refreshToken, username, refreshTokenValidity);
        return refreshToken;
        
    }

    @Override
    public String validateRefreshToken(String refreshToken) {
        Object username = redisTemplate.opsForValue().get(refreshToken);
        if (username != null) {
            return username.toString();
        }
        return null; // Invalid or expired token
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(refreshToken);
    }

    @Override
    public boolean isRefreshTokenExpired(String refreshToken) {
        // Redis automatically handles expiration, so we just check if the token exists
        return redisTemplate.opsForValue().get(refreshToken) == null;
    }


    
    
    
}
