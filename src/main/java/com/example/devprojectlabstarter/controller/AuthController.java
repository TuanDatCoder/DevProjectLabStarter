package com.example.devprojectlabstarter.controller;

import com.example.devprojectlabstarter.dto.ApiResponse;
import com.example.devprojectlabstarter.dto.Auth.Login.LoginRequestDTO;
import com.example.devprojectlabstarter.dto.Auth.Login.LoginResponseDTO;
import com.example.devprojectlabstarter.dto.Auth.Register.RegisterRequestDTO;
import com.example.devprojectlabstarter.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        ApiResponse<String> response = accountService.registerNewAccount(registerRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }
    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        ApiResponse<String> response = accountService.logout();
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO response = accountService.login(loginRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/verify/{token}")
    public ResponseEntity<ApiResponse<String>> verifyAccount(@PathVariable String token) {
        ApiResponse<String> response = accountService.verifyAccountByToken(token);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        ApiResponse<String> response = accountService.requestPasswordReset(email);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        accountService.resetPassword(token, newPassword);
        return new ResponseEntity<>("Password reset successful.", HttpStatus.OK);
    }


}
