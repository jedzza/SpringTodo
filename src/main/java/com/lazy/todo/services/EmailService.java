package com.lazy.todo.services;

import com.lazy.todo.payload.response.EmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {

    @Autowired
    JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendEmail(EmailResponse emailResponse) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailResponse.getRecipient());
        message.setSubject(emailResponse.getSubject());
        message.setText(emailResponse.getMsgBody());


        javaMailSender.send(message);
    }

}
