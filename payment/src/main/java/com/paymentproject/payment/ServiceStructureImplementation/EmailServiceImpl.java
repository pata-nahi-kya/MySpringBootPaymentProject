package com.paymentproject.payment.ServiceStructureImplementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.paymentproject.payment.ServiceStructure.EmailService;



@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender jms ;

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setTo(to);
        smm.setSubject(subject);
        smm.setText(body);
        jms.send(smm);
        
    }
    
}
