package com.paymentproject.payment.ServiceStructureImplementation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

    /**
     * Token validity = 30 minutes
     */
    private static final long ACCESS_TOKEN_VALIDITY_MS = 1000L * 60 ;

    /**
     * Fixed secret key
     *
     * IMPORTANT:
     * HS256 requires at least 32 bytes key length.
     * "123456789" alone is too short and will throw WeakKeyException.
     *
     * So we expand it to a valid 32+ byte secret.
     */
    private static final String SECRET =
            "12345678912345678912345678912345";

    /**
     * Single reusable signing key.
     * No regeneration on restart.
     */
    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * Generate JWT token
     */
    public String generateToken(String username) {

        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + ACCESS_TOKEN_VALIDITY_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from token
     */
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract any claim
     */
    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimResolver) {

        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    /**
     * Parse and validate token
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate token
     */
    public boolean validateToken(
            String token,
            UserDetails userDetails) {

        final String username = extractUserName(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Check expiration
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}