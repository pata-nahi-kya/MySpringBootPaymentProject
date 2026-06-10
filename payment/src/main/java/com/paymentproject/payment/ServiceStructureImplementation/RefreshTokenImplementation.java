package com.paymentproject.payment.ServiceStructureImplementation;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.paymentproject.payment.ServiceStructure.RefreshTokenStructure;

/**
 * Refresh Token Implementation backed by Redis.
 *
 * --- Bug fixed: incorrect TTL type ---
 * The original code called:
 *     redisTemplate.opsForValue().set(key, value, refreshTokenValidity);
 * where refreshTokenValidity was a long representing milliseconds.
 * The three-argument overload of set() that accepts a long interprets it as
 * seconds by default via the deprecated API, or it requires an explicit
 * TimeUnit. In practice, passing 604800000 (7 days in ms) as seconds would
 * set a TTL of ~19 years. Using java.time.Duration eliminates this ambiguity
 * entirely — Duration.ofDays(7) is unambiguous regardless of method overload.
 *
 * --- Design note: isRefreshTokenExpired ---
 * Redis automatically removes keys when their TTL elapses, so the absence of
 * a key in Redis is the canonical signal that the token has expired or never
 * existed. No separate expiry timestamp is stored or checked.
 */
@Service
public class RefreshTokenImplementation implements RefreshTokenStructure {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Create a new refresh token for the given username and store it in Redis with
     * a 7-day TTL.
     *
     * The token is a UUID (128-bit random value), which is unpredictable and
     * unique. For higher security, consider using a cryptographically secure random
     * byte array encoded as Base64URL instead.
     *
     * @param username the authenticated user's username
     * @return the generated refresh token string
     */
    @SuppressWarnings("null")
    @Override
    public String createRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(refreshToken, username, REFRESH_TOKEN_TTL);
        return refreshToken;
    }

    /**
     * Look up the username associated with a refresh token.
     *
     * Returns null if the token does not exist (either never created, already used,
     * or expired and evicted by Redis).
     *
     * @param refreshToken the token to look up
     * @return the associated username, or null if not found
     */
    @Override
    public String validateRefreshToken(String refreshToken) {
        @SuppressWarnings("null")
        Object username = redisTemplate.opsForValue().get(refreshToken);
        return username != null ? username.toString() : null;
    }

    /**
     * Delete a refresh token from Redis (used on logout and on token rotation).
     *
     * @param refreshToken the token to invalidate
     */
    @SuppressWarnings("null")
    @Override
    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(refreshToken);
    }

    /**
     * Check whether a refresh token has expired.
     *
     * Because Redis automatically evicts keys after their TTL, an absent key means
     * the token is expired (or invalid). A present key means it is still valid.
     *
     * @param refreshToken the token to check
     * @return true if the token is absent from Redis (expired or invalid)
     */
    @SuppressWarnings("null")
    @Override
    public boolean isRefreshTokenExpired(String refreshToken) {
        return redisTemplate.opsForValue().get(refreshToken) == null;
    }

    
}
