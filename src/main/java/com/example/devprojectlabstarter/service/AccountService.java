package com.example.devprojectlabstarter.service;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.devprojectlabstarter.dto.Account.AccountResponseDTO;
import com.example.devprojectlabstarter.dto.ApiResponse;
import com.example.devprojectlabstarter.dto.Auth.Login.LoginRequestDTO;
import com.example.devprojectlabstarter.dto.Auth.Login.LoginResponseDTO;
import com.example.devprojectlabstarter.dto.Auth.Register.RegisterRequestDTO;
import com.example.devprojectlabstarter.entity.Account;
import com.example.devprojectlabstarter.entity.Enum.AccountProviderEnum;
import com.example.devprojectlabstarter.entity.Enum.AccountStatusEnum;
import com.example.devprojectlabstarter.exception.Account.AccountException;
import com.example.devprojectlabstarter.exception.Admin.AdminException;
import com.example.devprojectlabstarter.exception.ErrorCode;
import com.example.devprojectlabstarter.exception.Token.InvalidToken;
import com.example.devprojectlabstarter.repository.AccountRepository;
import com.example.devprojectlabstarter.security.JwtTokenUtil;
import com.example.devprojectlabstarter.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountUtils accountUtils;

    public ApiResponse<String> registerNewAccount(RegisterRequestDTO registerRequestDTO) {
        if (accountRepository.findByEmail(registerRequestDTO.getEmail()).isPresent()) {
            throw new AccountException("User existed", ErrorCode.USER_EXISTED);
        }

        Account account = new Account();
        account.setEmail(registerRequestDTO.getEmail());
        account.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        account.setName(registerRequestDTO.getName());
        account.setGender(registerRequestDTO.getGender());
        account.setProvider(AccountProviderEnum.LOCAL);
        account.setRole(registerRequestDTO.getRole());
        account.setStatus(AccountStatusEnum.UNVERIFIED);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);

        String verificationToken = jwtTokenUtil.generateToken(new org.springframework.security.core.userdetails.User(account.getEmail(), "", new ArrayList<>()));
        String verificationLink = "http://localhost:8080/auth/verify/" + verificationToken;
        emailService.sendVerificationEmail(account.getEmail(), account.getName(), verificationLink);

        return new ApiResponse<>(201, "Registration successful, please check your email to verify your account.", null);
    }

    public ApiResponse<String> verifyAccountByToken(String token) {
        try {
            String email = jwtTokenUtil.getEmailFromToken(token); // Sửa tên phương thức
            verifyAccountByEmail(email);
            return new ApiResponse<>(200, "Account verification successful.", null);
        } catch (InvalidToken | TokenExpiredException e) {
            throw new AccountException("Invalid token", ErrorCode.TOKEN_INVALID);
        } catch (Exception e) {
            throw new AccountException("Error verifying account: " + e.getMessage(), ErrorCode.INTERNAL_ERROR);
        }
    }

    public void verifyAccountByEmail(String email) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            if (account.getStatus() == AccountStatusEnum.UNVERIFIED) {
                account.setStatus(AccountStatusEnum.VERIFIED);
                accountRepository.save(account);
            }
        } else {
            throw new AccountException("Account not found", ErrorCode.ACCOUNT_NOT_FOUND);
        }
    }
    public ApiResponse<String> requestPasswordReset(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountException("User not found with email: " + email, ErrorCode.USER_NOT_FOUND));

        String resetToken = jwtTokenUtil.generateToken(new org.springframework.security.core.userdetails.User(email, "", new ArrayList<>()));
        String resetLink = "http://localhost:8080/auth/reset-password?token=" + resetToken;

        emailService.sendResetPasswordEmail(email, account.getName(), resetLink);
        return new ApiResponse<>(200, "Password reset link sent to your email.", null);
    }

    public ApiResponse<String> resetPassword(String token, String newPassword) {
        try {
            String email = jwtTokenUtil.getEmailFromToken(token); // Sửa tên phương thức
            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new AccountException("User not found with email: " + email, ErrorCode.USER_NOT_FOUND));

            account.setPassword(passwordEncoder.encode(newPassword));
            accountRepository.save(account);
            return new ApiResponse<>(200, "Password reset successful.", null);
        } catch (InvalidToken | TokenExpiredException e) {
            throw new AccountException("Invalid or expired token", ErrorCode.TOKEN_INVALID);
        } catch (Exception e) {
            throw new AccountException("Error resetting password: " + e.getMessage(), ErrorCode.INTERNAL_ERROR);
        }
    }

    public ApiResponse<String> logout() {
        return new ApiResponse<>(200, "Logout successful", null);
    }



    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        Account account = accountRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new AccountException("User not found with email: " + loginRequestDTO.getEmail(), ErrorCode.USER_NOT_FOUND));

        if (account.getProvider() != AccountProviderEnum.LOCAL) {
            return new LoginResponseDTO("Please log in using Google", null, null, null);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())
            );

            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(loginRequestDTO.getEmail());
            String accessToken = jwtTokenUtil.generateToken(userDetails);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            return new LoginResponseDTO("Login successful", null, accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            throw new AccountException("Invalid email or password", ErrorCode.USERNAME_PASSWORD_NOT_CORRECT);
        }
    }



    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }


    public Account getAccountByEmail(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);
        return account.orElse(null);
    }


    public ApiResponse<AccountResponseDTO> getCurrentAccount() {
        try {
            Account curAccount = accountUtils.getCurrentAccount();
            AccountResponseDTO accountResponseDTO = convertToDto(curAccount);

            return new ApiResponse<>(HttpStatus.OK.value(), "Account retrieved successfully", accountResponseDTO);

        } catch (AccountException e) {
            return new ApiResponse<>(e.getErrorCode().getHttpStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }
    private AccountResponseDTO convertToDto(Account account) {
        return objectMapper.convertValue(account, AccountResponseDTO.class);
    }
}
