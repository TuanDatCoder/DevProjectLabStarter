package com.example.devprojectlabstarter.service;

import com.example.devprojectlabstarter.entity.Account;
import com.example.devprojectlabstarter.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(account.getEmail(), account.getPassword(), new ArrayList<>());
    }

    //@Override
//public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//    Account account = accountRepository.findByEmail(email)
//            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
//
//    // Chuyển đổi các quyền từ AccountRoleEnum thành quyền của Spring Security
//    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + account.getRole().name());
//
//    return new User(account.getEmail(), account.getPassword(), Collections.singletonList(authority));
//}

}