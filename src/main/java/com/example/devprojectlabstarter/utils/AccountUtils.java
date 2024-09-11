package com.example.devprojectlabstarter.utils;


import com.example.devprojectlabstarter.entity.Account;
import com.example.devprojectlabstarter.exception.Account.AccountException;
import com.example.devprojectlabstarter.exception.ErrorCode;
import com.example.devprojectlabstarter.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AccountUtils {

    @Autowired
    private AccountRepository accountRepository;

    public Account getCurrentAccount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return accountRepository.findByEmail(email)
                    .orElseThrow(() -> new AccountException("Account not found with email: " + email, ErrorCode.USER_NOT_FOUND));
        } else {
            throw new AccountException("User not authenticated", ErrorCode.UNAUTHORIZED);
        }
    }
}