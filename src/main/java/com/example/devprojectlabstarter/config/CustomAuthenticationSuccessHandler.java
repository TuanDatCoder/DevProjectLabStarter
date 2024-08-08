package com.example.devprojectlabstarter.config;

import com.example.devprojectlabstarter.entity.*;
import com.example.devprojectlabstarter.entity.Enum.AccountProviderEnum;
import com.example.devprojectlabstarter.entity.Enum.AccountRoleEnum;
import com.example.devprojectlabstarter.entity.Enum.AccountStatusEnum;
import com.example.devprojectlabstarter.repository.AccountRepository;
import com.example.devprojectlabstarter.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public CustomAuthenticationSuccessHandler(AccountRepository accountRepository, JwtTokenUtil jwtTokenUtil) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.accountRepository = accountRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");

        try {
            Optional<Account> existingAccount = accountRepository.findByEmail(email);
            Account account;

            if (existingAccount.isEmpty()) {
                account = new Account();
                account.setEmail(email);
                account.setName((String) attributes.get("name"));
                account.setPicture((String) attributes.get("picture"));
                account.setGoogleId((String) attributes.get("sub"));
                account.setRole(AccountRoleEnum.USER);
                account.setProvider(AccountProviderEnum.GOOGLE);
                account.setStatus(AccountStatusEnum.VERIFIED);
                account.setCreateAt(LocalDateTime.now());
                accountRepository.save(account);
            } else {
                account = existingAccount.get();
            }

            UserDetails userDetails = new org.springframework.security.core.userdetails.User(account.getEmail(), "", new ArrayList<>());
            String token = jwtTokenUtil.generateToken(userDetails);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "token", token,
                    "message", existingAccount.isEmpty() ? "Account created and login successful" : "Login successful"
            )));
            response.getWriter().flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "message", "An error occurred during authentication"
            )));
        }
    }
}
