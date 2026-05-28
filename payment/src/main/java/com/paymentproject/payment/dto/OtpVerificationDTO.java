package com.paymentproject.payment.dto;



import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Data;

@RedisHash(value = "OtpVerification", timeToLive = 300) // 5 minutes standard
@Data
public class OtpVerificationDTO {
    @Id
    private String email; // Redis Key
    private String otp; // The hashed or raw OTP
    
}
