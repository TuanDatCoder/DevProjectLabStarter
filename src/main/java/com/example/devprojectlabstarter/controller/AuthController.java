package com.example.devprojectlabstarter.controller;

import com.example.devprojectlabstarter.dto.Auth.LoginRequest;
import com.example.devprojectlabstarter.dto.Auth.RegisterRequest;
import com.example.devprojectlabstarter.entity.Account;
import com.example.devprojectlabstarter.entity.Enum.AccountProviderEnum;
import com.example.devprojectlabstarter.entity.Enum.AccountStatusEnum;
import com.example.devprojectlabstarter.security.JwtTokenUtil;
import com.example.devprojectlabstarter.service.AccountService;
import com.example.devprojectlabstarter.service.EmailService;
import com.example.devprojectlabstarter.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        Account newAccount = accountService.registerNewAccount(registerRequest);
        if (newAccount.getStatus() == AccountStatusEnum.UNVERIFIED) {
            String verificationToken = jwtTokenUtil.generateToken(new org.springframework.security.core.userdetails.User(newAccount.getEmail(), "", new ArrayList<>()));
            String verificationLink = "http://localhost:8080/auth/verify/" + verificationToken;
            emailService.sendVerificationEmail(newAccount.getEmail(), newAccount.getName(), verificationLink);
        }
        return new ResponseEntity<>("Đăng ký thành công, vui lòng kiểm tra email để xác thực tài khoản.", HttpStatus.CREATED);
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        accountService.verifyAccountByToken(token);
        return new ResponseEntity<>("Xác thực tài khoản thành công.", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = accountService.login(loginRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        accountService.requestPasswordReset(email);
        return new ResponseEntity<>("Password reset link sent to your email.", HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        accountService.resetPassword(token, newPassword);
        return new ResponseEntity<>("Password reset successful.", HttpStatus.OK);
    }


    @GetMapping("/logout")
    public String logout() {
        return "Logout successful";
    }
}
