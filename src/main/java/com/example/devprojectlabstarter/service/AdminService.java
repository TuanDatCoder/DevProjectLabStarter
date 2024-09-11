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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;


    public ApiResponse<List<AccountResponseDTO>> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        List<AccountResponseDTO> accountDtos = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ApiResponse<>(HttpStatus.OK.value(), "Success", accountDtos);
    }
    private AccountResponseDTO convertToDto(Account account) {
        return objectMapper.convertValue(account, AccountResponseDTO.class);
    }
}