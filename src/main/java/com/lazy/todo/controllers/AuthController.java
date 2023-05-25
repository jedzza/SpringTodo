package com.lazy.todo.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.lazy.todo.exceptions.PasswordResetTokenExpiredException;
import com.lazy.todo.models.Role;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.NewPasswordRequest;
import com.lazy.todo.payload.request.PasswordResetRequest;
import com.lazy.todo.repository.PasswordResetTokenRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.security.services.PasswordResetService;
import com.lazy.todo.security.services.UserDetailsImpl;
import com.lazy.todo.security.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.lazy.todo.models.ERole;
import com.lazy.todo.payload.request.LoginRequest;
import com.lazy.todo.payload.request.SignupRequest;
import com.lazy.todo.payload.response.JwtResponse;
import com.lazy.todo.payload.response.MessageResponse;
import com.lazy.todo.repository.RoleRepository;
import com.lazy.todo.repository.UserRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;


  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  PasswordResetService passwordResetService;

  //return a JWT token if authentication is successful
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt, 
                         userDetails.getId(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(), 
                         roles));
  }

  //register a new accoung
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
               signUpRequest.getEmail(),
               encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  //requesst a new password reset token which will allow you to access "/resetpassword" (see below)
  @PostMapping("/requestresetpassword")
  public ResponseEntity<?> requestResetPassword (@RequestBody PasswordResetRequest passwordResetRequest){
    try {
      if (passwordResetService.generatePasswordResetToken(passwordResetRequest)) {
        return ResponseEntity.ok("Please check your email for a reset code");
      }
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("email not recognized");
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal server error");
  }

  //Reset passwords if token is valid
  @PostMapping("/resetPassword")
  public ResponseEntity<?> resetPassword (@ModelAttribute NewPasswordRequest passwordResetRequest){
    try {
      if (passwordResetService.validatePasswordResetRequest(passwordResetRequest)) {
        return ResponseEntity.ok("Password successfully reset");
      }
    } catch (UsernameNotFoundException | PasswordResetTokenExpiredException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal server error");
  }
}
