package com.example.devprojectlabstarter.controller;

import com.example.devprojectlabstarter.dto.Account.AccountResponseDTO;
import com.example.devprojectlabstarter.dto.ApiResponse;
import com.example.devprojectlabstarter.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AccountService accountService;

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    //@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponseDTO>>> getAllAccounts() {
        ApiResponse<List<AccountResponseDTO>> response = accountService.getAllAccounts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
