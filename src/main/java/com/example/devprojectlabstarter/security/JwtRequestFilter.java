package com.example.devprojectlabstarter.security;


import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.devprojectlabstarter.entity.Account;
import com.example.devprojectlabstarter.exception.Token.ErrorResponseUtil;
import com.example.devprojectlabstarter.exception.Token.InvalidToken;
import com.example.devprojectlabstarter.service.AccountService;
import com.example.devprojectlabstarter.service.JwtUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    @Lazy
    private AccountService accountService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;
        String uri = request.getRequestURI();

        // Bypass filter for login and register endpoints
        if (uri.contains("/auth/login") || uri.contains("/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token and email
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtTokenUtil.extractEmail(token);
            } catch (TokenExpiredException e) {
                // Handle expired token
                ResponseEntity<String> responseEntity = ErrorResponseUtil.createErrorResponse(HttpStatus.UNAUTHORIZED, "Token has expired");
                response.setStatus(responseEntity.getStatusCodeValue());
                response.getWriter().write(responseEntity.getBody());
                return;
            } catch (InvalidToken e) {
                // Handle invalid token
                ResponseEntity<String> responseEntity = ErrorResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid token");
                response.setStatus(responseEntity.getStatusCodeValue());
                response.getWriter().write(responseEntity.getBody());
                return;
            }
        }

        // Authenticate user if email is extracted and there's no existing authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Account account = accountService.getAccountByEmail(email);

            // Validate the token with the account (which implements UserDetails)
            if (jwtTokenUtil.validateToken(token, account)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

}