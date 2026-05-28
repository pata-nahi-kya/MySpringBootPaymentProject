package com.paymentproject.payment.ServiceStructure;

public interface EmailService {
    
    void sendSimpleEmail(String to, String subject, String body);
}
