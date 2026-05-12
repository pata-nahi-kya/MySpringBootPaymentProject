package com.paymentproject.payment.ServiceStructureImplementation;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * JWT (JSON Web Token) Service
 * 
 * This service handles all JWT-related operations including:
 * - Token generation
 * - Token validation
 * - User information extraction
 * - Token expiration management
 * 
 * Security features:
 * - Uses HmacSHA256 for token signing
 * - Implements token expiration
 * - Validates token integrity
 * - Securely manages secret keys
 * 
 * @Service marks this as a Spring service component
 */
@Service
public class JWTService {

    /**
     * Secret key for JWT signing and verification
     * Generated once per service instance
     */
    private String secretkey = "";

    /**
     * Constructor - Initializes the service with a secure secret key
     * 
     * Generates a cryptographically secure key using HmacSHA256
     * and encodes it as a Base64 string
     * 
     * @throws RuntimeException if key generation fails
     */
    public JWTService() {
        try {
            // in this 3 line of code we are generating a secret key using HmacSHA256
            // algorithm and then encoding it to a string using Base64 encoding . here
            // sk.getEncoded() returns the byte array of the secret key
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            secretkey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a new JWT token for a user
     * 
     * Creates a token with:
     * - Username as subject
     * - Current timestamp as issuedAt
     * - 30 minutes expiration
     * - HMAC-SHA256 signature
     * 
     * @param username The username to include in the token
     * @return Signed JWT token string
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Get the secret key for JWT operations
     * 
     * Converts the Base64 encoded secret key into a SecretKey object
     * suitable for JWT signing and verification
     * 
     * @return SecretKey instance for JWT operations
     */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretkey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract username from JWT token
     * 
     * @param token JWT token to analyze
     * @return Username stored in the token's subject claim
     */
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract any claim from the token
     * 
     * @param <T>           Type of the claim to extract
     * @param token         JWT token to analyze
     * @param claimResolver Function to extract specific claim
     * @return Extracted claim value
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    /**
     * Extract all claims from a JWT token
     * 
     * Verifies the token signature and returns all claims
     * 
     * @param token JWT token to analyze
     * @return All claims from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
        // return Jwts.parserBuilder()
        //         .setSigningKey(getKey())
        //         .build()
        //         .parseClaimsJws(token)
        //         .getBody();
    }

    /**
     * Validate a JWT token
     * 
     * Checks if:
     * - Username in token matches UserDetails
     * - Token is not expired
     * 
     * @param token       JWT token to validate
     * @param userDetails User details to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Check if a token is expired
     * 
     * @param token JWT token to check
     * @return true if token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     * 
     * @param token JWT token to analyze
     * @return Expiration date of the token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
