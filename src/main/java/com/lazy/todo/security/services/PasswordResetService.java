package com.lazy.todo.security.services;

import com.lazy.todo.exceptions.PasswordResetTokenExpiredException;
import com.lazy.todo.models.PasswordResetToken;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.NewPasswordRequest;
import com.lazy.todo.payload.request.PasswordResetRequest;
import com.lazy.todo.payload.response.EmailResponse;
import com.lazy.todo.repository.PasswordResetTokenRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import static java.lang.Math.abs;
import static org.apache.tomcat.util.http.fileupload.util.Streams.asString;

@Service
public class PasswordResetService {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    EmailService emailService;

    @Value("classpath:PasswordReset.txt")
    private Resource emailHtml;

    private SecureRandom secureRandom = new SecureRandom();

    public boolean generatePasswordResetToken(PasswordResetRequest passwordResetRequest) throws UsernameNotFoundException{
        String token = Integer.toString(abs(secureRandom.nextInt()));
        String emailBody = null;
        try {
            emailBody = asString(emailHtml.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(token);
        User user = userRepository.findByEmail(passwordResetRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + passwordResetRequest.getEmail()));
        passwordResetTokenRepository.save(new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(30)));
        emailBody = String.format(emailBody, user.getUsername(), token);
        System.out.println(emailBody);
        EmailResponse emailResponse = new EmailResponse(user.getEmail(), emailBody, "password reset");
        try {
            emailService.sendEmail(emailResponse);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(emailResponse.getRecipient());
            return false;
        }
        return true;
    }

    public boolean validatePasswordResetRequest(NewPasswordRequest passwordResetRequest)
            throws UsernameNotFoundException, PasswordResetTokenExpiredException, AccessDeniedException {

        User user = userRepository.findByEmail(passwordResetRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + passwordResetRequest.getEmail()));

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(passwordResetRequest.getToken())
                .orElseThrow(() -> new AccessDeniedException("Your password reset token is invalid"));

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpiredException("your password reset token has expired");
        }
        if (!passwordResetToken.getUser().equals(user)) {
            throw new AccessDeniedException("token is invalid");
        } else {
            user.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
            userRepository.save(user);
            return true;
        }
    }
}
