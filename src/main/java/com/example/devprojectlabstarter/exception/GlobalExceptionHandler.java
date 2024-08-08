package com.example.devprojectlabstarter.exception;


import com.example.devprojectlabstarter.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.devprojectlabstarter.exception.Account.AccountException;

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


}
