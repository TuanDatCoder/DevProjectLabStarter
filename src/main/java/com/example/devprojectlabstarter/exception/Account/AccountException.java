package com.example.devprojectlabstarter.exception.Account;

import com.example.devprojectlabstarter.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AccountException extends RuntimeException {
    private final ErrorCode errorCode;

    public AccountException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
