package com.paymentproject.payment.Model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Data;

@RedisHash(value = "OtpVerification", timeToLive = 300) // 5 minutes standard
@Data
public class OtpVerification {
    @Id
    private String email; // Redis Key
    private String otpCode; // The hashed or raw OTP
    private int attemptsCount = 0; // Track incorrect entries
    private long generatedAt; // To enforce the 60-second resend cooldown

    private String customerName;
    private double money;
    private Set<String> role;

    // Helper method to increment attempts
    public void incrementAttempts() {
        this.attemptsCount++;
    }
}
