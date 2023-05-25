package com.lazy.todo.controllers;

import com.lazy.todo.payload.request.NewPasswordRequest;
import com.lazy.todo.payload.request.PasswordResetRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/auth/reset")
public class PasswordResetController {

    @GetMapping("/{token}")
    public String updatePassword(@PathVariable(value = "token") String token, Model model){
        // need to get a passwordrewuest from frontend
        NewPasswordRequest newPasswordRequest = new NewPasswordRequest();
        newPasswordRequest.setToken(token);
        model.addAttribute("newPasswordRequest", newPasswordRequest);
        return "newPassword";
    }

 }
