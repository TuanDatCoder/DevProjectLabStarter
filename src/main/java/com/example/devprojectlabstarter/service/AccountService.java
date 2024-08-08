package com.example.devprojectlabstarter.service;

import com.example.devprojectlabstarter.dto.Account.Response.AccountResponseDTO;
import com.example.devprojectlabstarter.dto.Auth.LoginRequest;
import com.example.devprojectlabstarter.dto.Auth.RegisterRequest;
import com.example.devprojectlabstarter.entity.Account;
import com.example.devprojectlabstarter.entity.Enum.AccountProviderEnum;
import com.example.devprojectlabstarter.entity.Enum.AccountStatusEnum;
import com.example.devprojectlabstarter.exception.Account.AccountException;
import com.example.devprojectlabstarter.exception.ErrorCode;
import com.example.devprojectlabstarter.repository.AccountRepository;
import com.example.devprojectlabstarter.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public Account registerNewAccount(RegisterRequest registerRequest) {
        if (accountRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new AccountException("User existed", ErrorCode.USER_EXISTED);
        }

        Account account = new Account();
        account.setEmail(registerRequest.getEmail());
        account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        account.setName(registerRequest.getName());
        account.setProvider(AccountProviderEnum.LOCAL);
        account.setRole(registerRequest.getRole());
        account.setStatus(AccountStatusEnum.UNVERIFIED);
        account.setCreateAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    public void verifyAccountByToken(String token) {
        try {
            String email = jwtTokenUtil.getUsernameFromToken(token);
            verifyAccountByEmail(email);
        } catch (Exception e) {
            throw new AccountException("Invalid token", ErrorCode.TOKEN_INVALID);
        }
    }

    public void verifyAccountByEmail(String email) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            account.setStatus(AccountStatusEnum.VERIFIED);
            accountRepository.save(account);
        } else {
            throw new AccountException("Account not found", ErrorCode.ACCOUNT_NOT_FOUND);
        }
    }

    public Map<String, Object> login(LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        Account account = accountRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AccountException("User not found with email: " + loginRequest.getEmail(), ErrorCode.USER_NOT_FOUND));

        if (account.getProvider() != AccountProviderEnum.LOCAL) {
            response.put("message", "Please log in using Google");
            return response;
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(loginRequest.getEmail());
            String token = jwtTokenUtil.generateToken(userDetails);

            response.put("token", token);
            response.put("message", "Login successful");
        } catch (BadCredentialsException e) {
            throw new AccountException("Invalid email or password", ErrorCode.USERNAME_PASSWORD_NOT_CORRECT);
        }

        return response;
    }

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public List<AccountResponseDTO> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AccountResponseDTO convertToDto(Account account) {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setId(account.getId());
        dto.setUsername(account.getName());
        dto.setEmail(account.getEmail());
        // Chuyển đổi các trường khác nếu cần
        return dto;
    }
}
