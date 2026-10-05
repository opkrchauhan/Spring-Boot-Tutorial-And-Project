package com.opkrchauhan.OrderService.exception;

import com.opkrchauhan.OrderService.external.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException exception) {

        ErrorResponse errorResponse = new ErrorResponse()
                .builder()
                .errorMessage(exception.getMessage())
                .errorCode(exception.getErrorCode())
                .build();

        return new ResponseEntity<>(
                errorResponse,
                HttpStatus.valueOf(exception.getStatus())
        );
    }
}