package com.example.devprojectlabstarter.exception;


import com.example.devprojectlabstarter.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.devprojectlabstarter.exception.Account.AccountException;

import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AccountException.class)
    ResponseEntity<ApiResponse> handlingAccountAppException(AccountException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Object> handleAccessDeniedException(AccessDeniedException ex) {
        return new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "You do not have permission to access this resource.", null);
    }

}
